package com.banlinhkien.controller.view;

import com.banlinhkien.service.BannerService;
import com.banlinhkien.service.CategoryService;
import com.banlinhkien.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BannerService bannerService;

    @GetMapping("/")
    public String index(
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "section", required = false) String section,
            Model model
    ) {
        model.addAttribute("pageTitle", "Trang Chủ - Bán Linh Kiện Máy Tính");
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("banners", bannerService.getActiveBanners());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("section", section);

        if (categoryId != null && categoryId > 0) {
            categoryService.getAllCategories().stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .findFirst()
                    .ifPresent(c -> {
                        model.addAttribute("categoryName", c.getName());
                        model.addAttribute("pageTitle", "Danh mục: " + c.getName());
                    });
            model.addAttribute("listProducts", productService.getProducts(0, 100, null, categoryId).getContent());
        } else if (section != null && !section.trim().isEmpty()) {
            switch (section) {
                case "latest":
                    model.addAttribute("pageTitle", "🆕 Linh kiện mới nhất");
                    model.addAttribute("listProducts", productService.getLatestProducts());
                    break;
                case "top_selling":
                    model.addAttribute("pageTitle", "🔥 Top lượt mua");
                    model.addAttribute("listProducts", productService.getTopSellingProducts());
                    break;
                case "featured":
                    model.addAttribute("pageTitle", "⭐ Nổi bật");
                    model.addAttribute("listProducts", productService.getFeaturedProducts());
                    break;
                case "on_sale":
                    model.addAttribute("pageTitle", "🏷️ Đang giảm giá");
                    model.addAttribute("listProducts", productService.getOnSaleProducts());
                    break;
                case "recommended":
                default:
                    model.addAttribute("pageTitle", "✨ Có thể bạn cũng thích");
                    model.addAttribute("listProducts", productService.getRecommendedProducts());
                    break;
            }
        } else {
            model.addAttribute("recommendedProducts", productService.getRecommendedProducts());
            model.addAttribute("onSaleProducts", productService.getOnSaleProducts());
            model.addAttribute("latestProducts", productService.getLatestProducts());
            model.addAttribute("topSellingProducts", productService.getTopSellingProducts());
            model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        }

        return "home";
    }

    @GetMapping({"/contact", "/lien-he"})
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Liên Hệ & Hỗ Trợ Khách Hàng - Bán Linh Kiện");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "contact";
    }

    @GetMapping({"/about", "/gioi-thieu"})
    public String about(Model model) {
        model.addAttribute("pageTitle", "Giới Thiệu Về Bán Linh Kiện");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "about";
    }

    @GetMapping({"/policy", "/terms", "/shipping", "/return", "/chinh-sach"})
    public String policy(Model model) {
        model.addAttribute("pageTitle", "Chính Sách & Điều Khoản");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "policy";
    }
}
