package com.banlinhkien.controller.view;

import com.banlinhkien.dto.*;
import com.banlinhkien.service.CartService;
import com.banlinhkien.service.PcBuilderAiService;
import com.banlinhkien.service.PcBuilderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping({"/build-pc", "/buildpc"})
@RequiredArgsConstructor
@Slf4j
public class PcBuilderViewController {

    private final PcBuilderService pcBuilderService;
    private final PcBuilderAiService pcBuilderAiService;
    private final CartService cartService;

    @GetMapping
    public String index(@RequestParam(value = "action", required = false) String action,
                        @RequestParam(value = "cat_id", required = false) Integer catId,
                        @RequestParam(value = "product_id", required = false) Long productId,
                        HttpSession session,
                        Model model) {

        // Support direct product detail CTA "Build PC với linh kiện này"
        if ("add".equalsIgnoreCase(action) && catId != null && productId != null) {
            pcBuilderService.selectComponent(session, catId, productId, 1);
            return "redirect:/build-pc";
        }

        PcBuilderSessionDto build = pcBuilderService.getBuildSession(session);

        model.addAttribute("buildCategories", PcBuilderService.CATEGORIES);
        model.addAttribute("build", build.getItems());
        model.addAttribute("totalPrice", build.getTotalPrice());
        model.addAttribute("selectedCount", build.getSelectedCount());

        return "buildpc/index";
    }

    @GetMapping("/components")
    @ResponseBody
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
    public Object select(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "category_id", required = false) Integer paramCatId,
            @RequestParam(value = "cat_id", required = false) Integer paramCatId2,
            @RequestParam(value = "product_id", required = false) Long paramProdId,
            @RequestParam(value = "id", required = false) Long paramProdId2,
            @RequestParam(value = "quantity", defaultValue = "1") Integer paramQty,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer categoryId = paramCatId != null ? paramCatId : paramCatId2;
        Long productId = paramProdId != null ? paramProdId : paramProdId2;
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

        boolean isJson = isJsonRequest(request);

        if (categoryId == null || productId == null) {
            if (isJson) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Thông tin linh kiện không hợp lệ."
                ));
            }
            redirectAttributes.addFlashAttribute("error", "Dữ liệu chọn linh kiện không hợp lệ.");
            return "redirect:/build-pc";
        }

        PcBuilderService.SelectResult result = pcBuilderService.selectComponent(session, categoryId, productId, quantity);

        if (isJson) {
            if (!result.success()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", result.message()
                ));
            }
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", true);
            res.put("message", result.message());
            if (result.warning() != null) {
                res.put("warning", result.warning());
            }
            res.put("build", result.build().getItems());
            return ResponseEntity.ok(res);
        }

        if (result.success()) {
            if (result.warning() != null) {
                redirectAttributes.addFlashAttribute("warning", result.warning());
            }
            redirectAttributes.addFlashAttribute("success", result.message());
        } else {
            redirectAttributes.addFlashAttribute("error", result.message());
        }

        return "redirect:/build-pc";
    }

    @PostMapping("/remove")
    public Object remove(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "category_id", required = false) Integer paramCatId,
            @RequestParam(value = "cat_id", required = false) Integer paramCatId2,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer categoryId = paramCatId != null ? paramCatId : paramCatId2;
        if (body != null) {
            if (body.containsKey("category_id")) {
                categoryId = Integer.valueOf(body.get("category_id").toString());
            } else if (body.containsKey("cat_id")) {
                categoryId = Integer.valueOf(body.get("cat_id").toString());
            }
        }

        PcBuilderSessionDto build = pcBuilderService.removeComponent(session, categoryId);

        if (isJsonRequest(request)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã gỡ linh kiện khỏi cấu hình.",
                    "build", build.getItems()
            ));
        }

        redirectAttributes.addFlashAttribute("success", "Đã gỡ linh kiện khỏi cấu hình.");
        return "redirect:/build-pc";
    }

    @PostMapping("/clear")
    public Object clear(HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        pcBuilderService.clearBuild(session);

        if (isJsonRequest(request)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã làm mới cấu hình."
            ));
        }

        redirectAttributes.addFlashAttribute("success", "Đã làm mới toàn bộ cấu hình PC.");
        return "redirect:/build-pc";
    }

    @PostMapping("/add-to-cart")
    public Object addToCart(
            @RequestParam(value = "action", required = false) String action,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        PcBuilderService.BatchAddResult result = pcBuilderService.batchAddToCart(session, cartService);

        if (isJsonRequest(request)) {
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

        if (!result.success()) {
            redirectAttributes.addFlashAttribute("error", result.message());
            return "redirect:/build-pc";
        }

        if ("buy_now".equalsIgnoreCase(action)) {
            return "redirect:/thanh-toan";
        }

        redirectAttributes.addFlashAttribute("success", result.message());
        return "redirect:/gio-hang";
    }

    @PostMapping("/ai")
    @ResponseBody
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

    private boolean isJsonRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        String requestedWith = request.getHeader("X-Requested-With");

        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"))
                || (contentType != null && contentType.contains("application/json"));
    }
}
