package com.banlinhkien.controller.view.admin;

import com.banlinhkien.entity.*;
import com.banlinhkien.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminFeaturesController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final BannerRepository bannerRepository;
    private final ShippingZoneRepository shippingZoneRepository;
    private final ProductCommentRepository productCommentRepository;
    private final ShopSettingRepository shopSettingRepository;
    private final PasswordEncoder passwordEncoder;


    // 1. Người dùng
    @GetMapping("/users")
    public String users(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        model.addAttribute("activeNav", "users");
        PageRequest pageable = PageRequest.of(Math.max(0, page), size, Sort.by("id").descending());
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        return "admin/features/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản #" + id + " thành công!");
        });
        return "redirect:/admin/users";
    }

    // 2. Tích điểm KH
    @GetMapping("/loyalty")
    public String loyalty(Model model) {
        model.addAttribute("activeNav", "loyalty");
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/features/loyalty";
    }

    // 3. Voucher
    @GetMapping("/vouchers")
    public String vouchers(Model model) {
        model.addAttribute("activeNav", "vouchers");
        List<Voucher> vouchers = voucherRepository.findAll();
        model.addAttribute("vouchers", vouchers);
        return "admin/features/vouchers";
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@ModelAttribute Voucher voucher, RedirectAttributes redirectAttributes) {
        voucherRepository.save(voucher);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu mã giảm giá thành công!");
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        voucherRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa voucher #" + id + " thành công!");
        return "redirect:/admin/vouchers";
    }

    // 4. Giảm giá
    @GetMapping("/discounts")
    public String discounts(Model model) {
        model.addAttribute("activeNav", "discounts");
        List<Product> discountedProducts = productRepository.findTop12ByIsActiveTrueAndDiscountPercentGreaterThanOrderByDiscountPercentDesc(BigDecimal.ZERO);
        model.addAttribute("discountedProducts", discountedProducts);
        return "admin/features/discounts";
    }

    // 5. Kho hàng
    @GetMapping("/warehouse")
    public String warehouse(Model model) {
        model.addAttribute("activeNav", "warehouse");
        List<Product> lowStockProducts = productRepository.findTop6LowStock();
        List<Product> allProducts = productRepository.findAll();
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("allProducts", allProducts);
        return "admin/features/warehouse";
    }

    // 6. Banner Slideshow
    @GetMapping("/banners")
    public String banners(Model model) {
        model.addAttribute("activeNav", "banners");
        List<Banner> banners = bannerRepository.findAll();
        model.addAttribute("banners", banners);
        return "admin/features/banners";
    }

    @PostMapping("/banners/save")
    public String saveBanner(@ModelAttribute Banner banner, RedirectAttributes redirectAttributes) {
        bannerRepository.save(banner);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu banner thành công!");
        return "redirect:/admin/banners";
    }

    @PostMapping("/banners/{id}/delete")
    public String deleteBanner(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bannerRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa banner #" + id + " thành công!");
        return "redirect:/admin/banners";
    }

    // 7. Phí vận chuyển
    @GetMapping("/shipping")
    public String shipping(Model model) {
        model.addAttribute("activeNav", "shipping");
        List<ShippingZone> zones = shippingZoneRepository.findAll();
        model.addAttribute("zones", zones);
        return "admin/features/shipping";
    }

    // 8. Theo dõi vận đơn
    @GetMapping("/tracking")
    public String tracking(Model model) {
        model.addAttribute("activeNav", "tracking");
        return "admin/features/tracking";
    }

    // 9. Nhà cung cấp
    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        model.addAttribute("activeNav", "suppliers");
        return "admin/features/suppliers";
    }

    // 10. Flash Sale
    @GetMapping("/flash-sales")
    public String flashSales(Model model) {
        model.addAttribute("activeNav", "flash-sales");
        return "admin/features/flash_sales";
    }

    // 11. Giỏ hàng bỏ quên
    @GetMapping("/abandoned-carts")
    public String abandonedCarts(Model model) {
        model.addAttribute("activeNav", "abandoned-carts");
        return "admin/features/abandoned_carts";
    }

    // 12. Combo sản phẩm
    @GetMapping("/combos")
    public String combos(Model model) {
        model.addAttribute("activeNav", "combos");
        return "admin/features/combos";
    }

    // 13. Thông báo
    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("activeNav", "notifications");
        return "admin/features/notifications";
    }

    // 14. Chat
    @GetMapping("/chat")
    public String chat(Model model) {
        model.addAttribute("activeNav", "chat");
        return "admin/features/chat";
    }

    // 15. Bình luận
    @GetMapping("/comments")
    public String comments(Model model) {
        model.addAttribute("activeNav", "comments");
        List<ProductComment> comments = productCommentRepository.findAll();
        model.addAttribute("comments", comments);
        return "admin/features/comments";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productCommentRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa bình luận #" + id + " thành công!");
        return "redirect:/admin/comments";
    }

    // 16. Báo có hàng
    @GetMapping("/back-in-stock")
    public String backInStock(Model model) {
        model.addAttribute("activeNav", "back-in-stock");
        return "admin/features/back_in_stock";
    }

    // 17. Đổi trả / Bảo hành
    @GetMapping("/returns")
    public String returns(Model model) {
        model.addAttribute("activeNav", "returns");
        return "admin/features/returns";
    }

    // 18. Phân quyền Admin
    @GetMapping("/roles")
    public String roles(Model model) {
        model.addAttribute("activeNav", "roles");
        List<User> admins = userRepository.findByIsAdminTrue();
        model.addAttribute("admins", admins);
        return "admin/features/roles";
    }


    // 19. Cài đặt shop
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activeNav", "settings");
        List<ShopSetting> settingsList = shopSettingRepository.findAll();
        java.util.Map<String, String> settingsMap = new java.util.HashMap<>();
        for (ShopSetting s : settingsList) {
            settingsMap.put(s.getSettingKey(), s.getSettingValue());
        }
        model.addAttribute("settings", settingsMap);
        return "admin/features/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(
            @RequestParam java.util.Map<String, String> allParams,
            RedirectAttributes redirectAttributes
    ) {
        for (java.util.Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if ("_csrf".equals(key)) continue;
            String val = entry.getValue();

            ShopSetting setting = shopSettingRepository.findBySettingKey(key)
                    .orElse(ShopSetting.builder().settingKey(key).settingGroup("general").build());
            setting.setSettingValue(val != null ? val.trim() : "");
            shopSettingRepository.save(setting);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã lưu và cập nhật cấu hình cửa hàng thành công!");
        return "redirect:/admin/settings";
    }


    // 20. Email Queue
    @GetMapping("/email-queue")
    public String emailQueue(Model model) {
        model.addAttribute("activeNav", "email-queue");
        return "admin/features/email_queue";
    }

    // 21. Đổi mật khẩu
    @GetMapping("/change-password")
    public String changePassword(Model model) {
        model.addAttribute("activeNav", "change-password");
        return "admin/features/change_password";
    }

    @PostMapping("/change-password")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/admin/change-password";
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/admin/change-password";
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        return "redirect:/admin/change-password";
    }
}
