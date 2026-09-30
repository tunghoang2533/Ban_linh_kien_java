package com.banlinhkien.repository;

import com.banlinhkien.entity.Order;
import com.banlinhkien.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findWithItemsById(Long id);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'completed' AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    BigDecimal sumRevenueByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'completed' AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    long countOrdersByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findTop6ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(o.id AS string) = :keyword) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> searchOrdersAdmin(@Param("status") OrderStatus status,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);
}
