package com.banlinhkien.controller.view;

import com.banlinhkien.entity.User;
import com.banlinhkien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping({"/tai-khoan", "/account", "/profile"})
@RequiredArgsConstructor
public class AccountViewController {

    private final UserRepository userRepository;
    private final com.banlinhkien.repository.OrderRepository orderRepository;

    @GetMapping({"", "/"})
    public String accountIndex(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin tài khoản."));

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Tài Khoản - " + user.getUsername());
        return "account/index";
    }

    @GetMapping({"/orders", "/don-hang"})
    public String accountOrders(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thông tin tài khoản."));

        model.addAttribute("user", user);
        model.addAttribute("orders", orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 50)).getContent());
        model.addAttribute("pageTitle", "Đơn Hàng Của Tôi - " + user.getUsername());
        return "account/index";
    }
}
