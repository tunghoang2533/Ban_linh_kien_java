package com.banlinhkien.repository;

import com.banlinhkien.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);

    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findTop12ByIsActiveTrueAndIsFeaturedTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findTop12ByIsActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findTop12ByIsActiveTrueAndDiscountPercentGreaterThanOrderByDiscountPercentDesc(java.math.BigDecimal minDiscount);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findTop10ByCategoryIdAndIsActiveTrueAndIdNot(Long categoryId, Long id);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findTop12ByIsActiveTrueOrderByIdAsc();

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Product> findTop12ByIsActiveTrueOrderByIdDesc();

    @EntityGraph(attributePaths = {"category", "brand"})
    @Override
    Optional<Product> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.quantity <= 5")
    long countLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity = 0")
    long countOutOfStockProducts();

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.quantity <= 5 ORDER BY p.quantity ASC")
    List<Product> findTop6LowStock();

    long countByCategoryId(Long categoryId);

    long countByBrandId(Long brandId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.discountPercent > 0")
    long countDiscountedProducts();
}

