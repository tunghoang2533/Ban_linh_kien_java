package com.banlinhkien.controller.view;

import com.banlinhkien.entity.User;
import com.banlinhkien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model
    ) {
        model.addAttribute("pageTitle", "Đăng Nhập - Bán Linh Kiện");
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("pageTitle", "Đăng Ký - Bán Linh Kiện");
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String email,
            @RequestParam(required = false) String fullName,
            RedirectAttributes redirectAttributes
    ) {
        // Validate password length
        if (password == null || password.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu phải có ít nhất 8 ký tự.");
            return "redirect:/register";
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            return "redirect:/register";
        }

        // Check username uniqueness
        if (userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập đã được sử dụng.");
            return "redirect:/register";
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email đã được sử dụng.");
            return "redirect:/register";
        }

        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName)
                .role("user")
                .build();

        userRepository.save(newUser);
        return "redirect:/login?registered=true";
    }
}
