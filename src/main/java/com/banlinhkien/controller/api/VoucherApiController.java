package com.banlinhkien.controller.api;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.VoucherResultDto;
import com.banlinhkien.entity.User;
import com.banlinhkien.repository.UserRepository;
import com.banlinhkien.service.CartService;
import com.banlinhkien.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherApiController {

    private final VoucherService voucherService;
    private final CartService cartService;
    private final UserRepository userRepository;

    @PostMapping("/apply")
    public ResponseEntity<VoucherResultDto> applyVoucher(
            @RequestParam("code") String code,
            HttpSession session,
            Authentication authentication
    ) {
        CartDto cart = cartService.getCart(session);
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                userId = user.getId();
            }
        }

        VoucherResultDto result = voucherService.validate(code, cart.getSubtotal(), userId);
        if (result.isSuccess()) {
            cart.setAppliedVoucher(result.getVoucherCode());
            cart.setVoucherDiscount(result.getDiscount());
            session.setAttribute(CartService.SESSION_CART_KEY, cart);
        }

        return ResponseEntity.ok(result);
    }
}
