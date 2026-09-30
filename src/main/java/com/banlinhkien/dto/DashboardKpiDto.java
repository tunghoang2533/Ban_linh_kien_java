package com.banlinhkien.dto;

import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.Product;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardKpiDto {

    private long totalProducts;
    private long totalOrders;
    private long totalUsers;
    private BigDecimal totalRevenue;

    private BigDecimal thisMonthRevenue;
    private BigDecimal lastMonthRevenue;
    private long thisMonthOrders;
    private long lastMonthOrders;
    private double revGrowth;
    private double ordGrowth;

    private List<RevenueChartPointDto> revenueTrend;
    private Map<String, Long> statusData;
    private List<Order> recentOrders;
    private List<Product> lowStockProducts;
    private List<TopSellingProductDto> topSellingProducts;

    // Sidebar badge metrics
    @Builder.Default
    private long pendingOrdersCount = 0;
    @Builder.Default
    private long lowStockCount = 0;
    @Builder.Default
    private long discountCount = 0;
    @Builder.Default
    private long abandonedCartCount = 0;
    @Builder.Default
    private long emailQueueCount = 0;

    // Convenience getters for templates
    public BigDecimal getTotalRevenueThisMonth() {
        return thisMonthRevenue != null ? thisMonthRevenue : BigDecimal.ZERO;
    }

    public double getRevenueGrowthRate() {
        return revGrowth;
    }

    public long getTotalOrdersThisMonth() {
        return thisMonthOrders;
    }

    public double getOrdersGrowthRate() {
        return ordGrowth;
    }

    public long getPendingOrders() {
        return pendingOrdersCount > 0 ? pendingOrdersCount : (statusData != null && statusData.containsKey("pending") ? statusData.get("pending") : 0L);
    }
}
