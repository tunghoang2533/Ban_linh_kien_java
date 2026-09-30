package com.banlinhkien.service;

import com.banlinhkien.entity.Brand;
import com.banlinhkien.entity.Category;
import com.banlinhkien.repository.BrandRepository;
import com.banlinhkien.repository.CategoryRepository;
import com.banlinhkien.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCatalogService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    // --- Category Operations ---

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục #" + id));
    }

    @Transactional
    public Category saveCategory(Long id, String name, String slug) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        }

        Category category;
        if (id != null) {
            category = categoryRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục #" + id));
        } else {
            category = new Category();
        }

        category.setName(name.trim());
        if (slug == null || slug.isBlank()) {
            category.setSlug(toSlug(name));
        } else {
            category.setSlug(toSlug(slug));
        }

        Category saved = categoryRepository.save(category);
        log.info("Saved category #{}: '{}' (slug: '{}')", saved.getId(), saved.getName(), saved.getSlug());
        return saved;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategory(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục '" + category.getName()
                    + "' vì đang có " + productCount + " sản phẩm liên kết.");
        }
        categoryRepository.delete(category);
        log.info("Deleted category #{} ('{}')", id, category.getName());
    }

    // --- Brand Operations ---

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Brand getBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thương hiệu #" + id));
    }

    @Transactional
    public Brand saveBrand(Long id, String name, String image) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên thương hiệu không được để trống.");
        }

        Brand brand;
        if (id != null) {
            brand = brandRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thương hiệu #" + id));
        } else {
            brand = new Brand();
        }

        brand.setName(name.trim());
        if (image != null && !image.isBlank()) {
            brand.setImage(image.trim());
        }

        Brand saved = brandRepository.save(brand);
        log.info("Saved brand #{}: '{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = getBrand(id);
        long productCount = productRepository.countByBrandId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa thương hiệu '" + brand.getName()
                    + "' vì đang có " + productCount + " sản phẩm liên kết.");
        }
        brandRepository.delete(brand);
        log.info("Deleted brand #{} ('{}')", id, brand.getName());
    }

    public static String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    }
}
