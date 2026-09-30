package com.banlinhkien.controller.view.admin;

import com.banlinhkien.entity.Brand;
import com.banlinhkien.service.AdminCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
@Slf4j
public class AdminBrandController {

    private final AdminCatalogService adminCatalogService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("activeNav", "brands");
        model.addAttribute("brands", adminCatalogService.getAllBrands());
        return "admin/brands/index";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String image,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Brand brand = adminCatalogService.saveBrand(id, name, image);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã lưu thương hiệu '" + brand.getName() + "' thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi lưu thương hiệu: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/brands";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.deleteBrand(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thương hiệu #" + id + " thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi xóa thương hiệu #{}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thương hiệu: " + e.getMessage());
        }
        return "redirect:/admin/brands";
    }
}
