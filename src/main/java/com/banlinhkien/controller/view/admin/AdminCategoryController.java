package com.banlinhkien.controller.view.admin;

import com.banlinhkien.entity.Category;
import com.banlinhkien.service.AdminCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final AdminCatalogService adminCatalogService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("activeNav", "categories");
        model.addAttribute("categories", adminCatalogService.getAllCategories());
        return "admin/categories/index";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String slug,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Category category = adminCatalogService.saveCategory(id, name, slug);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã lưu danh mục '" + category.getName() + "' thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi lưu danh mục: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục #" + id + " thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi xóa danh mục #{}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
