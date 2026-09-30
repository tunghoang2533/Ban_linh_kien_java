package com.banlinhkien.service;

import com.banlinhkien.config.WebMvcConfig;
import com.banlinhkien.dto.AdminProductFormDto;
import com.banlinhkien.entity.Brand;
import com.banlinhkien.entity.Category;
import com.banlinhkien.entity.Product;
import com.banlinhkien.entity.ProductSpec;
import com.banlinhkien.repository.BrandRepository;
import com.banlinhkien.repository.CategoryRepository;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.ProductSpecRepository;
import com.banlinhkien.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductSpecRepository productSpecRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024L; // 5 MB

    @Transactional(readOnly = true)
    public Page<Product> getProducts(String q, Long categoryId, Long brandId, String stock, Boolean isActive, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filter(q, categoryId, brandId, stock, isActive);
        return productRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public AdminProductFormDto getProductForm(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm #" + id));

        List<ProductSpec> specs = productSpecRepository.findByProductId(id);
        List<String> specNames = new ArrayList<>();
        List<String> specValues = new ArrayList<>();

        for (ProductSpec s : specs) {
            specNames.add(s.getSpecName());
            specValues.add(s.getSpecValue());
        }

        return AdminProductFormDto.builder()
                .id(product.getId())
                .name(product.getName())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .discountPercent(product.getDiscountPercent())
                .quantity(product.getQuantity())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .description(product.getDescription())
                .image(product.getImage())
                .specNames(specNames)
                .specValues(specValues)
                .build();
    }

    @Transactional
    public Product saveProduct(AdminProductFormDto form, MultipartFile imageFile) throws IOException {
        Product product;
        if (form.getId() != null) {
            product = productRepository.findById(form.getId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm #" + form.getId()));
        } else {
            product = new Product();
            product.setCreatedAt(LocalDateTime.now());
        }

        product.setName(form.getName());
        product.setPrice(form.getPrice());
        product.setCostPrice(form.getCostPrice());
        product.setDiscountPercent(form.getDiscountPercent());
        product.setQuantity(form.getQuantity() != null ? form.getQuantity() : 0);
        product.setIsActive(form.getIsActive() != null ? form.getIsActive() : true);
        product.setIsFeatured(form.getIsFeatured() != null ? form.getIsFeatured() : false);
        product.setDescription(form.getDescription());

        if (form.getCategoryId() != null) {
            categoryRepository.findById(form.getCategoryId()).ifPresent(product::setCategory);
        } else {
            product.setCategory(null);
        }

        if (form.getBrandId() != null) {
            brandRepository.findById(form.getBrandId()).ifPresent(product::setBrand);
        } else {
            product.setBrand(null);
        }

        // Image upload pipeline
        if (imageFile != null && !imageFile.isEmpty()) {
            validateImageFile(imageFile);
            String originalName = StringUtils.cleanPath(Objects.requireNonNull(imageFile.getOriginalFilename()));
            String ext = "";
            int dotIdx = originalName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalName.substring(dotIdx);
            }
            String baseSlug = originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String filename = System.currentTimeMillis() + "_" + baseSlug;

            Path uploadPath = Paths.get(WebMvcConfig.UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetFile = uploadPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            product.setImage(filename);
            log.info("Saved uploaded product image to persistent storage: {}", targetFile.toAbsolutePath());
        } else if (form.getImage() != null && !form.getImage().isBlank()) {
            product.setImage(form.getImage().trim());
        }

        Product savedProduct = productRepository.save(product);

        // Sync Product Specifications
        if (form.getSpecNames() != null && form.getSpecValues() != null) {
            // Remove existing specs for clean update
            List<ProductSpec> oldSpecs = productSpecRepository.findByProductId(savedProduct.getId());
            productSpecRepository.deleteAll(oldSpecs);

            List<ProductSpec> newSpecs = new ArrayList<>();
            for (int i = 0; i < form.getSpecNames().size(); i++) {
                String sName = form.getSpecNames().get(i);
                String sVal = i < form.getSpecValues().size() ? form.getSpecValues().get(i) : null;

                if (sName != null && !sName.isBlank() && sVal != null && !sVal.isBlank()) {
                    newSpecs.add(ProductSpec.builder()
                            .productId(savedProduct.getId())
                            .specName(sName.trim())
                            .specValue(sVal.trim())
                            .build());
                }
            }
            if (!newSpecs.isEmpty()) {
                productSpecRepository.saveAll(newSpecs);
            }
        }

        return savedProduct;
    }

    /**
     * Soft-deactivate product: set is_active = false and PRESERVE all specifications.
     */
    @Transactional
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm #" + id));

        product.setIsActive(false);
        productRepository.save(product);
        log.info("Soft-deactivated product #{} ('{}') preserving its specifications.", id, product.getName());
    }

    /**
     * Validates that an uploaded file is a genuine image.
     * Uses ImageIO.read() to actually decode the pixel data, so renaming
     * a PHP/HTML file to .jpg will still be rejected.
     *
     * @throws IllegalArgumentException if the file fails any validation check
     * @throws IOException              if the file cannot be read
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        // 1. Size check
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("File ảnh không được vượt quá 5MB.");
        }

        // 2. Extension whitelist (case-insensitive)
        String originalName = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename())).toLowerCase();
        boolean extOk = ALLOWED_IMAGE_EXTENSIONS.stream().anyMatch(originalName::endsWith);
        if (!extOk) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh: JPG, PNG, GIF, WEBP.");
        }

        // 3. Pixel-level decode — rejects scripts/HTML renamed as images
        try (InputStream is = file.getInputStream()) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                throw new IllegalArgumentException("File không phải ảnh hợp lệ hoặc bị hỏng.");
            }
        }
    }
}
