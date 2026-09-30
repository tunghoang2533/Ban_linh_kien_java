package com.banlinhkien.controller.view;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.CheckoutRequestDto;
import com.banlinhkien.entity.Order;
import com.banlinhkien.enums.PaymentMethod;
import com.banlinhkien.entity.User;
import com.banlinhkien.entity.UserAddress;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.UserAddressRepository;
import com.banlinhkien.repository.UserRepository;
import com.banlinhkien.service.CartService;
import com.banlinhkien.service.CheckoutService;
import com.banlinhkien.service.LocationService;
import com.banlinhkien.service.ShippingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CheckoutViewController {

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final ShippingService shippingService;
    private final LocationService locationService;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;

    @GetMapping({"/thanh-toan", "/checkout"})
    public String checkoutPage(
            @ModelAttribute("checkoutRequest") CheckoutRequestDto requestDto,
            HttpSession session,
            Authentication authentication,
            Model model
    ) {
        CartDto cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        populateCheckoutModel(model, cart, authentication, requestDto);
        return "checkout/index";
    }

    @PostMapping({"/thanh-toan", "/checkout"})
    public String processCheckout(
            @Valid @ModelAttribute("checkoutRequest") CheckoutRequestDto requestDto,
            BindingResult bindingResult,
            HttpSession session,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        CartDto cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống.");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            populateCheckoutModel(model, cart, authentication, requestDto);
            return "checkout/index";
        }

        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        try {
            Order order = checkoutService.placeOrder(requestDto, cart, currentUser);
            if (order.getPaymentMethod() == PaymentMethod.vnpay) {
                return "redirect:/vnpay/payment/" + order.getId()
                        + "?token=" + order.getAccessToken();
            }
            return "redirect:/dat-hang-thanh-cong/" + order.getId()
                    + "?token=" + order.getAccessToken();
        } catch (Exception e) {
            log.warn("Checkout failed: {}", e.getMessage());
            model.addAttribute("checkoutError", e.getMessage());
            populateCheckoutModel(model, cart, authentication, requestDto);
            return "checkout/index";
        }
    }

    @GetMapping({"/dat-hang-thanh-cong/{orderId}", "/checkout/success/{orderId}"})
    public String orderSuccess(
            @PathVariable("orderId") Long orderId,
            @RequestParam(name = "token", required = false) String token,
            Model model,
            RedirectAttributes redirectAttributes) {

        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId));

        if (token == null || !token.equals(order.getAccessToken())) {
            redirectAttributes.addFlashAttribute("error", "Liên kết không hợp lệ hoặc đã hết hạn.");
            return "redirect:/";
        }

        model.addAttribute("order", order);
        model.addAttribute("items", order.getItems());
        return "checkout/success";
    }

    private void populateCheckoutModel(Model model, CartDto cart, Authentication authentication, CheckoutRequestDto requestDto) {
        BigDecimal subtotal = cart.getSubtotal();
        String province = requestDto != null ? requestDto.getProvince() : null;
        BigDecimal shippingFee = shippingService.calculateFee(province, subtotal, cart.getTotalItems());
        BigDecimal voucherDiscount = cart.getVoucherDiscount() != null ? cart.getVoucherDiscount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(voucherDiscount).max(BigDecimal.ZERO);

        List<UserAddress> addresses = Collections.emptyList();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                addresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
                if (requestDto.getCustomerName() == null && user.getFullName() != null) {
                    requestDto.setCustomerName(user.getFullName());
                }
                if (requestDto.getCustomerEmail() == null && user.getEmail() != null) {
                    requestDto.setCustomerEmail(user.getEmail());
                }
                if (requestDto.getCustomerPhone() == null && user.getPhone() != null) {
                    requestDto.setCustomerPhone(user.getPhone());
                }
                if (!addresses.isEmpty()) {
                    UserAddress def = addresses.get(0);
                    if (requestDto.getCustomerAddress() == null) {
                        requestDto.setCustomerAddress(def.getAddressDetail());
                    }
                    if (requestDto.getProvince() == null) {
                        requestDto.setProvince(def.getProvince());
                    }
                    if (requestDto.getDistrict() == null) {
                        requestDto.setDistrict(def.getDistrict());
                    }
                    if (requestDto.getWard() == null) {
                        requestDto.setWard(def.getWard());
                    }
                }
            }
        }

        model.addAttribute("cart", cart);
        model.addAttribute("items", cart.getItemList());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("voucherCode", cart.getAppliedVoucher());
        model.addAttribute("voucherDiscount", voucherDiscount);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("addresses", addresses);
        model.addAttribute("provinces", locationService.getAllProvinces());
    }
}
