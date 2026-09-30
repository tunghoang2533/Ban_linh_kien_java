package com.banlinhkien.service;

import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts() {
        return productRepository.findTop12ByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getLatestProducts() {
        return productRepository.findTop12ByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getOnSaleProducts() {
        return productRepository.findTop12ByIsActiveTrueAndDiscountPercentGreaterThanOrderByDiscountPercentDesc(java.math.BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<Product> getTopSellingProducts() {
        return productRepository.findTop12ByIsActiveTrueOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Product> getRecommendedProducts() {
        return productRepository.findTop12ByIsActiveTrueOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(int page, int size, String search, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (search != null && !search.trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(search.trim(), pageable);
        }
        if (categoryId != null && categoryId > 0) {
            return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        }
        return productRepository.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Long categoryId, Long currentProductId) {
        if (categoryId == null) {
            return List.of();
        }
        return productRepository.findTop10ByCategoryIdAndIsActiveTrueAndIdNot(categoryId, currentProductId);
    }
}
