package com.banlinhkien.controller.view.admin;

import com.banlinhkien.dto.AdminProductFormDto;
import com.banlinhkien.entity.Product;
import com.banlinhkien.service.AdminCatalogService;
import com.banlinhkien.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final AdminCatalogService adminCatalogService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String stock,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), size, Sort.by("id").descending());
        Page<Product> products = adminProductService.getProducts(q, categoryId, brandId, stock, isActive, pageable);

        model.addAttribute("activeNav", "products");
        model.addAttribute("products", products);
        model.addAttribute("categories", adminCatalogService.getAllCategories());
        model.addAttribute("brands", adminCatalogService.getAllBrands());

        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("stock", stock);
        model.addAttribute("isActive", isActive);

        return "admin/products/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("activeNav", "products");
        model.addAttribute("productForm", new AdminProductFormDto());
        model.addAttribute("categories", adminCatalogService.getAllCategories());
        model.addAttribute("brands", adminCatalogService.getAllBrands());
        model.addAttribute("isEdit", false);
        return "admin/products/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        AdminProductFormDto formDto = adminProductService.getProductForm(id);
        model.addAttribute("activeNav", "products");
        model.addAttribute("productForm", formDto);
        model.addAttribute("categories", adminCatalogService.getAllCategories());
        model.addAttribute("brands", adminCatalogService.getAllBrands());
        model.addAttribute("isEdit", true);
        return "admin/products/form";
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("productForm") AdminProductFormDto form,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "products");
            model.addAttribute("categories", adminCatalogService.getAllCategories());
            model.addAttribute("brands", adminCatalogService.getAllBrands());
            model.addAttribute("isEdit", form.getId() != null);
            return "admin/products/form";
        }

        try {
            Product saved = adminProductService.saveProduct(form, imageFile);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã lưu sản phẩm '" + saved.getName() + "' (Mã #" + saved.getId() + ") thành công!");
            return "redirect:/admin/products";
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh sản phẩm: {}", e.getMessage(), e);
            model.addAttribute("activeNav", "products");
            model.addAttribute("errorMessage", "Lỗi tải ảnh: " + e.getMessage());
            model.addAttribute("categories", adminCatalogService.getAllCategories());
            model.addAttribute("brands", adminCatalogService.getAllBrands());
            model.addAttribute("isEdit", form.getId() != null);
            return "admin/products/form";
        } catch (Exception e) {
            log.error("Lỗi khi lưu sản phẩm: {}", e.getMessage(), e);
            model.addAttribute("activeNav", "products");
            model.addAttribute("errorMessage", "Không thể lưu sản phẩm: " + e.getMessage());
            model.addAttribute("categories", adminCatalogService.getAllCategories());
            model.addAttribute("brands", adminCatalogService.getAllBrands());
            model.addAttribute("isEdit", form.getId() != null);
            return "admin/products/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminProductService.softDeleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã ngưng kinh doanh sản phẩm #" + id + " thành công (thông số kỹ thuật được bảo toàn).");
        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm #{}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
