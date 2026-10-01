package com.banlinhkien.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false)
    private String name;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "sale_start")
    private LocalDateTime saleStart;

    @Column(name = "sale_end")
    private LocalDateTime saleEnd;

    private Integer quantity;

    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "min_stock")
    private Integer minStock;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Column(name = "meta_keywords")
    private String metaKeywords;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "bin_location")
    private String binLocation;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isFeatured == null) {
            this.isFeatured = false;
        }
        if (this.quantity == null) {
            this.quantity = 0;
        }
    }

    /**
     * Checks if promotional discount is currently active.
     */
    public boolean hasDiscount() {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (saleStart != null && saleEnd != null) {
            return !now.isBefore(saleStart) && !now.isAfter(saleEnd);
        }
        return true;
    }

    /**
     * Calculates authoritative final price after discount.
     */
    public BigDecimal getFinalPrice() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        if (!hasDiscount()) {
            return price;
        }
        BigDecimal factor = BigDecimal.ONE.subtract(
                discountPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        return price.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }

    public String getImage() {
        if (this.image != null && !this.image.trim().isEmpty() && !this.image.equals("i5.jpg") && !this.image.startsWith("1778") && !this.image.startsWith("1780")) {
            return this.image;
        }
        return com.banlinhkien.service.ProductImageInitializer.getImageForProduct(this.id, this.name, this.category != null ? this.category.getId() : null);
    }
}
