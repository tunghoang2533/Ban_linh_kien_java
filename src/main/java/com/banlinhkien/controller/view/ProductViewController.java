package com.banlinhkien.controller.view;

import com.banlinhkien.entity.Category;
import com.banlinhkien.entity.Product;
import com.banlinhkien.entity.ProductComment;
import com.banlinhkien.entity.User;
import com.banlinhkien.repository.ProductCommentRepository;
import com.banlinhkien.repository.UserRepository;
import com.banlinhkien.service.CategoryService;
import com.banlinhkien.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductCommentRepository productCommentRepository;
    private final UserRepository userRepository;

    @GetMapping({"/san-pham", "/products"})
    public String index(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "category_id", required = false) Long categoryId,
            Model model
    ) {
        Page<Product> productPage = productService.getProducts(page, size, search, categoryId);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("pageTitle", "Danh Sách Linh Kiện - Bán Linh Kiện");
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("categories", categories);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategoryId", categoryId);

        return "products/index";
    }

    @GetMapping({"/danh-muc/{id}", "/categories/{id}"})
    public String category(
            @PathVariable("id") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Model model
    ) {
        return index(page, size, null, categoryId, model);
    }

    @GetMapping({"/san-pham/{id}", "/products/{id}"})
    public String show(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        List<Product> relatedProducts = productService.getRelatedProducts(categoryId, id);
        List<ProductComment> comments = productCommentRepository.findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(id);

        model.addAttribute("pageTitle", product.getName() + " - Bán Linh Kiện");
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", comments.size());

        // Calculate average rating
        double avgRating = 5.0;
        if (!comments.isEmpty()) {
            double sum = 0;
            for (ProductComment c : comments) {
                sum += (c.getRating() != null ? c.getRating() : 5);
            }
            avgRating = Math.round((sum / comments.size()) * 10.0) / 10.0;
        }
        model.addAttribute("avgRating", avgRating);

        return "products/show";
    }

    @PostMapping({"/san-pham/{id}/comment", "/products/{id}/comment"})
    public String submitComment(
            @PathVariable("id") Long productId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "rating", defaultValue = "5") int rating,
            @RequestParam("comment") String commentText,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (commentText == null || commentText.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập nội dung đánh giá!");
            return "redirect:/products/" + productId;
        }

        Long userId = null;
        String reviewerName = name != null && !name.trim().isEmpty() ? name.trim() : "Khách hàng đã mua";
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                userId = user.getId();
                reviewerName = user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : user.getUsername();
            }
        }

        ProductComment comment = ProductComment.builder()
                .productId(productId)
                .userId(userId)
                .name(reviewerName)
                .rating(Math.min(5, Math.max(1, rating)))
                .comment(commentText.trim())
                .isHidden(false)
                .build();

        productCommentRepository.save(comment);
        redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá sản phẩm!");
        return "redirect:/products/" + productId;
    }
}

