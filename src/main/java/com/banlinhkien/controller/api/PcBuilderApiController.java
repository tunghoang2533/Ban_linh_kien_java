package com.banlinhkien.controller.api;

import com.banlinhkien.dto.*;
import com.banlinhkien.service.CartService;
import com.banlinhkien.service.PcBuilderAiService;
import com.banlinhkien.service.PcBuilderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/build-pc")
@RequiredArgsConstructor
@Slf4j
public class PcBuilderApiController {

    private final PcBuilderService pcBuilderService;
    private final PcBuilderAiService pcBuilderAiService;
    private final CartService cartService;

    @GetMapping("/components")
    public ResponseEntity<Map<String, Object>> getComponents(
            @RequestParam(value = "category_id", required = false) Integer categoryId,
            @RequestParam(value = "cat_id", required = false) Integer catId,
            HttpSession session) {

        Integer cId = categoryId != null ? categoryId : catId;
        if (cId == null || !PcBuilderService.CATEGORIES.containsKey(cId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Danh mục không hợp lệ.",
                    "products", Collections.emptyList()
            ));
        }

        PcBuilderSessionDto build = pcBuilderService.getBuildSession(session);
        String requiredSocket = null;

        // Bidirectional Socket Filter Constraint
        if (cId == 3 && build.getItems().containsKey(1)) {
            requiredSocket = build.getItems().get(1).getSocket();
        } else if (cId == 1 && build.getItems().containsKey(3)) {
            requiredSocket = build.getItems().get(3).getSocket();
        }

        List<PcComponentDto> components = pcBuilderService.getComponentsForCategory(cId, requiredSocket);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("products", components);
        response.put("req_sock", requiredSocket != null ? requiredSocket : "");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/select")
    public ResponseEntity<Map<String, Object>> selectComponent(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "category_id", required = false) Integer paramCatId,
            @RequestParam(value = "product_id", required = false) Long paramProdId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer paramQty,
            HttpSession session) {

        Integer categoryId = paramCatId;
        Long productId = paramProdId;
        Integer quantity = paramQty;

        if (body != null) {
            if (body.containsKey("category_id")) {
                categoryId = Integer.valueOf(body.get("category_id").toString());
            } else if (body.containsKey("cat_id")) {
                categoryId = Integer.valueOf(body.get("cat_id").toString());
            }

            if (body.containsKey("product_id")) {
                productId = Long.valueOf(body.get("product_id").toString());
            } else if (body.containsKey("id")) {
                productId = Long.valueOf(body.get("id").toString());
            }

            if (body.containsKey("quantity")) {
                quantity = Integer.valueOf(body.get("quantity").toString());
            }
        }

        if (categoryId == null || productId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Thông tin linh kiện không hợp lệ."
            ));
        }

        PcBuilderService.SelectResult result = pcBuilderService.selectComponent(session, categoryId, productId, quantity);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.message()
            ));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", result.message());
        if (result.warning() != null) {
            response.put("warning", result.warning());
        }
        response.put("build", result.build().getItems());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeComponent(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "category_id", required = false) Integer paramCatId,
            HttpSession session) {

        Integer categoryId = paramCatId;
        if (body != null) {
            if (body.containsKey("category_id")) {
                categoryId = Integer.valueOf(body.get("category_id").toString());
            } else if (body.containsKey("cat_id")) {
                categoryId = Integer.valueOf(body.get("cat_id").toString());
            }
        }

        PcBuilderSessionDto build = pcBuilderService.removeComponent(session, categoryId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã gỡ linh kiện khỏi cấu hình.",
                "build", build.getItems()
        ));
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearBuild(HttpSession session) {
        pcBuilderService.clearBuild(session);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã làm mới cấu hình."
        ));
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<Map<String, Object>> addToCart(HttpSession session) {
        PcBuilderService.BatchAddResult result = pcBuilderService.batchAddToCart(session, cartService);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.message(),
                    "stock_errors", result.stockErrors()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result.message(),
                "cart_count", result.cartCount()
        ));
    }

    @PostMapping("/ai")
    public ResponseEntity<AiRecommendationResponseDto> getAiRecommendation(
            @RequestBody(required = false) AiRecommendationRequestDto request,
            @RequestParam(value = "budget", required = false) BigDecimal paramBudget,
            @RequestParam(value = "purpose", required = false) String paramPurpose,
            @RequestParam(value = "message", required = false) String paramMessage) {

        AiRecommendationRequestDto req = request != null ? request : new AiRecommendationRequestDto();
        if (req.getBudget() == null && paramBudget != null) {
            req.setBudget(paramBudget);
        }
        if (req.getPurpose() == null && paramPurpose != null) {
            req.setPurpose(paramPurpose);
        }
        if (req.getMessage() == null && paramMessage != null) {
            req.setMessage(paramMessage);
        }

        AiRecommendationResponseDto response = pcBuilderAiService.getRecommendation(req);
        return ResponseEntity.ok(response);
    }
}
