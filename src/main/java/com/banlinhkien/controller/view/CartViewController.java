package com.banlinhkien.controller.view;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.service.CartService;
import com.banlinhkien.service.ShippingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping({"/gio-hang", "/cart"})
@RequiredArgsConstructor
public class CartViewController {

    private final CartService cartService;
    private final ShippingService shippingService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal shippingFee = shippingService.calculateFee(null, subtotal, cart.getTotalItems());
        BigDecimal voucherDiscount = cart.getVoucherDiscount() != null ? cart.getVoucherDiscount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(voucherDiscount).max(BigDecimal.ZERO);

        model.addAttribute("cart", cart);
        model.addAttribute("items", cart.getItemList());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("voucherCode", cart.getAppliedVoucher());
        model.addAttribute("voucherDiscount", voucherDiscount);
        model.addAttribute("totalAmount", totalAmount);

        return "cart/index";
    }

    @PostMapping({"/add", "/cart/add"})
    public String addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            @RequestParam(value = "buyNow", defaultValue = "false") boolean buyNow,
            HttpSession session
    ) {
        cartService.addItem(session, productId, quantity);
        if (buyNow) {
            return "redirect:/checkout";
        }
        return "redirect:/cart";
    }

    @PostMapping({"/update", "/cart/update"})
    public String updateCart(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            HttpSession session
    ) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping({"/remove", "/cart/remove"})
    public String removeCartItem(
            @RequestParam("productId") Long productId,
            HttpSession session
    ) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    @PostMapping({"/clear", "/cart/clear"})
    public String clearCart(HttpSession session) {
        cartService.clear(session);
        return "redirect:/cart";
    }
}
