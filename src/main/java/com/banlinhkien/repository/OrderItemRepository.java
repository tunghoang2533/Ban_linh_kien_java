package com.banlinhkien.repository;

import com.banlinhkien.entity.OrderItem;
import com.banlinhkien.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.productName, SUM(oi.quantity) " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = :status " +
           "GROUP BY oi.productName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsByStatusRaw(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT oi.productName, SUM(oi.quantity) " +
           "FROM OrderItem oi " +
           "GROUP BY oi.productName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProductsAllRaw(Pageable pageable);
}

