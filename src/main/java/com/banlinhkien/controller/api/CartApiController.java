package com.banlinhkien.controller.api;

import com.banlinhkien.dto.CartApiResponse;
import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.CartItemDto;
import com.banlinhkien.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartApiController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartApiResponse> addToCart(
            @RequestParam("product_id") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session
    ) {
        try {
            CartItemDto item = cartService.addItem(session, productId, quantity);
            CartDto cart = cartService.getCart(session);

            return ResponseEntity.ok(CartApiResponse.builder()
                    .success(true)
                    .message("Đã thêm sản phẩm vào giỏ hàng!")
                    .cartCount(cart.getTotalItems())
                    .subtotal(cart.getSubtotal())
                    .item(item)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to add to cart: {}", e.getMessage());
            return ResponseEntity.badRequest().body(CartApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<CartApiResponse> updateQuantity(
            @RequestParam("product_id") Long productId,
            @RequestParam("quantity") int quantity,
            HttpSession session
    ) {
        try {
            cartService.updateQuantity(session, productId, quantity);
            CartDto cart = cartService.getCart(session);

            return ResponseEntity.ok(CartApiResponse.builder()
                    .success(true)
                    .message("Cập nhật giỏ hàng thành công!")
                    .cartCount(cart.getTotalItems())
                    .subtotal(cart.getSubtotal())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to update cart quantity: {}", e.getMessage());
            return ResponseEntity.badRequest().body(CartApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<CartApiResponse> removeItem(
            @RequestParam("product_id") Long productId,
            HttpSession session
    ) {
        cartService.removeItem(session, productId);
        CartDto cart = cartService.getCart(session);

        return ResponseEntity.ok(CartApiResponse.builder()
                .success(true)
                .message("Đã xóa sản phẩm khỏi giỏ hàng.")
                .cartCount(cart.getTotalItems())
                .subtotal(cart.getSubtotal())
                .build());
    }

    @PostMapping("/clear")
    public ResponseEntity<CartApiResponse> clearCart(HttpSession session) {
        cartService.clear(session);
        return ResponseEntity.ok(CartApiResponse.builder()
                .success(true)
                .message("Đã làm trống giỏ hàng.")
                .cartCount(0)
                .build());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartCount(HttpSession session) {
        return ResponseEntity.ok(Map.of("cart_count", cartService.getTotalCount(session)));
    }
}
