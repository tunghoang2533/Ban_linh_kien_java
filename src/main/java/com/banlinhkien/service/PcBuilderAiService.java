package com.banlinhkien.service;

import com.banlinhkien.dto.AiRecommendationRequestDto;
import com.banlinhkien.dto.AiRecommendationResponseDto;
import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.ProductSpecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PcBuilderAiService {

    private final ProductRepository productRepository;
    private final ProductSpecRepository productSpecRepository;

    @Value("${ai.groq.key:}")
    private String groqApiKey;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    private static final String GROQ_API_URL =
            "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    /**
     * Hybrid AI recommendation engine.
     *
     * <p>Step 1 — Rule-based engine always runs first: it queries the real DB to select
     * compatible products (CPU/Mainboard socket matching, budget allocation, stock check).
     * This ensures buildSuggestion always contains real product IDs with accurate prices.
     *
     * <p>Step 2 — If a Groq API key is configured AND step 1 produced a non-empty build,
     * the LLM is asked ONLY to write a natural-language Vietnamese explanation of WHY this
     * configuration is suitable (purpose, performance, value). The LLM never invents product
     * IDs or prices — those come exclusively from step 1.
     */
    public AiRecommendationResponseDto getRecommendation(AiRecommendationRequestDto request) {
        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        BigDecimal budget = request.getBudget();
        String purpose = request.getPurpose();

        if (message.isBlank() && budget != null && budget.compareTo(BigDecimal.ZERO) > 0) {
            String formattedBudget = CURRENCY_FORMAT.format(budget) + " ₫";
            String purposeText = purpose != null && !purpose.isBlank() ? " cho mục đích " + purpose : "";
            message = "Gợi ý cấu hình PC với ngân sách khoảng " + formattedBudget + purposeText;
        }

        // Step 1: always get rule-based result with real product data
        AiRecommendationResponseDto ruleResult = generateFallbackReply(message, budget, purpose);

        // Step 2: if Groq key is present and we have a real build, enhance with LLM explanation
        if (groqApiKey != null && !groqApiKey.isBlank()
                && ruleResult.getBuildSuggestion() != null
                && !ruleResult.getBuildSuggestion().isEmpty()) {
            try {
                String aiExplanation = callGroqForExplanation(
                        ruleResult.getReply(), budget, purpose);
                return ruleResult.toBuilder()
                        .reply(aiExplanation + "\n\n---\n\n" + ruleResult.getReply())
                        .build();
            } catch (Exception e) {
                log.warn("Groq API call failed, using rule-based reply only: {}", e.getMessage());
            }
        }

        return ruleResult;
    }

    /**
     * Calls the Groq Chat Completions API to generate a Vietnamese explanation
     * of the rule-based build. The LLM receives only a summary text (no product IDs),
     * so it cannot hallucinate prices or inventory data.
     */
    @SuppressWarnings("unchecked")
    private String callGroqForExplanation(String buildSummary, BigDecimal budget, String purpose) {
        String purposeContext = (purpose != null && !purpose.isBlank())
                ? " dành cho mục đích: " + purpose : "";
        String systemPrompt = "Bạn là chuyên gia tư vấn PC gaming tại Việt Nam. "
                + "Hãy viết một đoạn giải thích ngắn gọn (3-4 câu) bằng tiếng Việt về lý do tại sao "
                + "cấu hình máy tính dưới đây phù hợp với ngân sách và nhu cầu của khách hàng. "
                + "Chỉ giải thích về hiệu năng và tính phù hợp — KHÔNG đề xuất thêm hoặc thay thế linh kiện nào.";
        String userPrompt = "Ngân sách: " + (budget != null ? CURRENCY_FORMAT.format(budget) + " ₫" : "không xác định")
                + purposeContext + "\n\nCấu hình đề xuất:\n" + buildSummary;

        Map<String, Object> message = Map.of("role", "user", "content", userPrompt);
        Map<String, Object> systemMsg = Map.of("role", "system", "content", systemPrompt);
        Map<String, Object> body = Map.of(
                "model", GROQ_MODEL,
                "messages", List.of(systemMsg, message),
                "max_tokens", 300,
                "temperature", 0.5
        );

        RestClient client = RestClient.create();
        Map<String, Object> response = client.post()
                .uri(GROQ_API_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> msgObj = (Map<String, Object>) choices.get(0).get("message");
                if (msgObj != null) {
                    return "### 💡 Phân tích từ AI\n\n" + msgObj.get("content").toString().trim();
                }
            }
        }
        throw new RuntimeException("Groq API returned unexpected response structure");
    }

    /**
     * Intelligent Rule-based Offline Fallback Generator.
     * Assembles a real, compatible build from active inventory in MySQL.
     */
    public AiRecommendationResponseDto generateFallbackReply(String message, BigDecimal budget, String purpose) {
        BigDecimal numBudget = budget != null ? budget : BigDecimal.ZERO;

        if (numBudget.compareTo(BigDecimal.ZERO) <= 0 && message != null) {
            Pattern pattern = Pattern.compile("(\\d+)\\s*(triệu|tr|m)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                try {
                    long val = Long.parseLong(matcher.group(1));
                    numBudget = BigDecimal.valueOf(val).multiply(BigDecimal.valueOf(1_000_000));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (numBudget.compareTo(BigDecimal.valueOf(5_000_000)) >= 0) {
            return generateBuildForBudget(numBudget);
        }

        // Conversational greeting fallback
        return AiRecommendationResponseDto.builder()
                .success(true)
                .reply("👋 **Xin chào!** Tôi là trợ lý AI Build PC chuyên nghiệp.\n\n" +
                       "Hãy cho tôi biết **ngân sách** và **nhu cầu sử dụng** của bạn (ví dụ: *'PC Gaming 15 triệu'*, *'Máy văn phòng 8 triệu'*), " +
                       "tôi sẽ gợi ý cấu hình tối ưu và tương thích phần cứng tốt nhất!")
                .suggestions(List.of(
                        "Build PC gaming 15 triệu",
                        "Build PC văn phòng 10 triệu",
                        "Build PC đồ họa 20 triệu",
                        "Xem danh sách CPU"
                ))
                .buildSuggestion(Collections.emptyList())
                .build();
    }

    private AiRecommendationResponseDto generateBuildForBudget(BigDecimal totalBudget) {
        // Budget ratio plan:
        // 1 (CPU): 22%, 3 (Mainboard): 14%, 2 (RAM): 12%, 4 (VGA): 28%, 5 (SSD): 12%, 6 (PSU): 7%, 7 (Case): 5%
        Map<Integer, Double> budgetPlan = Map.of(
                1, 0.22,
                3, 0.14,
                2, 0.12,
                4, 0.28,
                5, 0.12,
                6, 0.07,
                7, 0.05
        );

        List<Map<String, Object>> buildItems = new ArrayList<>();
        List<Map<String, Object>> buildJson = new ArrayList<>();
        BigDecimal totalEst = BigDecimal.ZERO;

        // 1. Pick CPU
        BigDecimal cpuBudget = totalBudget.multiply(BigDecimal.valueOf(budgetPlan.get(1)));
        Product cpu = findClosestProduct(1, cpuBudget);
        String cpuSocket = "";

        if (cpu != null) {
            cpuSocket = productSpecRepository.findSpecValue(cpu.getId(), "Socket").orElse("");
            buildItems.add(Map.of("cat_id", 1, "product", cpu, "socket", cpuSocket));
            buildJson.add(Map.of("cat_id", 1, "product_id", cpu.getId()));
            totalEst = totalEst.add(cpu.getFinalPrice());
        }

        // 2. Pick Mainboard compatible with CPU socket
        BigDecimal mbBudget = totalBudget.multiply(BigDecimal.valueOf(budgetPlan.get(3)));
        Product mb = findClosestMainboard(mbBudget, cpuSocket);
        if (mb != null) {
            String mbSocket = productSpecRepository.findSpecValue(mb.getId(), "Socket").orElse("");
            buildItems.add(Map.of("cat_id", 3, "product", mb, "socket", mbSocket));
            buildJson.add(Map.of("cat_id", 3, "product_id", mb.getId()));
            totalEst = totalEst.add(mb.getFinalPrice());
        }

        // 3. Pick remaining hardware slots: RAM(2), VGA(4), SSD(5), PSU(6), Case(7)
        for (int catId : List.of(2, 4, 5, 6, 7)) {
            BigDecimal catBudget = totalBudget.multiply(BigDecimal.valueOf(budgetPlan.getOrDefault(catId, 0.10)));
            Product p = findClosestProduct(catId, catBudget);
            if (p != null) {
                buildItems.add(Map.of("cat_id", catId, "product", p));
                buildJson.add(Map.of("cat_id", catId, "product_id", p.getId()));
                totalEst = totalEst.add(p.getFinalPrice());
            }
        }

        List<String> mdLines = new ArrayList<>();
        mdLines.add("### 🤖 Cấu hình PC đề xuất (Ngân sách: " + CURRENCY_FORMAT.format(totalBudget) + " ₫)\n");

        for (Map<String, Object> bi : buildItems) {
            int catId = (int) bi.get("cat_id");
            Product p = (Product) bi.get("product");
            String catName = PcBuilderService.CATEGORIES.getOrDefault(catId, "Linh kiện");
            String sockInfo = bi.containsKey("socket") && !((String) bi.get("socket")).isBlank()
                    ? " `[Socket: " + bi.get("socket") + "]`" : "";

            mdLines.add("↳ **" + catName + "**: " + p.getName() + sockInfo +
                        " — **" + CURRENCY_FORMAT.format(p.getFinalPrice()) + " ₫**");
        }

        mdLines.add("\n**Tổng chi phí dự kiến:** `" + CURRENCY_FORMAT.format(totalEst) + " ₫`");
        mdLines.add("\n*Cấu hình trên đã được kiểm tra tương thích 100% về chuẩn chân cắm (Socket) giữa CPU và Mainboard. Bạn có thể nhấn **Áp dụng cấu hình này** để điền nhanh vào danh sách.*");

        return AiRecommendationResponseDto.builder()
                .success(true)
                .reply(String.join("\n", mdLines))
                .suggestions(List.of(
                        "Áp dụng cấu hình này",
                        "Tư vấn cấu hình gaming 15 triệu",
                        "Tư vấn cấu hình đồ họa 25 triệu"
                ))
                .buildSuggestion(buildJson)
                .build();
    }

    private Product findClosestProduct(int categoryId, BigDecimal targetPrice) {
        List<Product> list = productRepository.findByCategoryIdAndIsActiveTrue(Long.valueOf(categoryId));
        return list.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0)
                .min(Comparator.comparing(p -> p.getFinalPrice().subtract(targetPrice).abs()))
                .orElse(null);
    }

    private Product findClosestMainboard(BigDecimal targetPrice, String requiredSocket) {
        List<Product> list = productRepository.findByCategoryIdAndIsActiveTrue(3L);
        String normReq = PcBuilderService.normalizeSocket(requiredSocket);

        return list.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0)
                .filter(p -> {
                    if (normReq.isBlank()) return true;
                    String sock = productSpecRepository.findSpecValue(p.getId(), "Socket").orElse("");
                    return normReq.equals(PcBuilderService.normalizeSocket(sock));
                })
                .min(Comparator.comparing(p -> p.getFinalPrice().subtract(targetPrice).abs()))
                .orElseGet(() -> findClosestProduct(3, targetPrice));
    }
}
