package com.banlinhkien.service;

import com.banlinhkien.dto.DashboardKpiDto;
import com.banlinhkien.dto.RevenueChartPointDto;
import com.banlinhkien.dto.TopSellingProductDto;
import com.banlinhkien.enums.OrderStatus;
import com.banlinhkien.repository.OrderItemRepository;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardKpiDto getDashboardKpi() {
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.completed);

        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);

        BigDecimal thisMonthRev = orderRepository.sumRevenueByYearAndMonth(currentMonth.getYear(), currentMonth.getMonthValue());
        BigDecimal lastMonthRev = orderRepository.sumRevenueByYearAndMonth(lastMonth.getYear(), lastMonth.getMonthValue());

        long thisMonthOrd = orderRepository.countOrdersByYearAndMonth(currentMonth.getYear(), currentMonth.getMonthValue());
        long lastMonthOrd = orderRepository.countOrdersByYearAndMonth(lastMonth.getYear(), lastMonth.getMonthValue());

        double revGrowth = 0.0;
        if (lastMonthRev != null && lastMonthRev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = (thisMonthRev != null ? thisMonthRev : BigDecimal.ZERO).subtract(lastMonthRev);
            revGrowth = diff.divide(lastMonthRev, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        double ordGrowth = 0.0;
        if (lastMonthOrd > 0) {
            ordGrowth = Math.round(((double) (thisMonthOrd - lastMonthOrd) / lastMonthOrd * 100) * 10.0) / 10.0;
        }

        // 6-month revenue trend
        List<RevenueChartPointDto> trend = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM/yyyy");
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            BigDecimal rev = orderRepository.sumRevenueByYearAndMonth(ym.getYear(), ym.getMonthValue());
            long count = orderRepository.countOrdersByYearAndMonth(ym.getYear(), ym.getMonthValue());

            trend.add(RevenueChartPointDto.builder()
                    .monthLabel(ym.format(labelFormatter))
                    .revenue(rev != null ? rev : BigDecimal.ZERO)
                    .orderCount(count)
                    .build());
        }

        // Status distribution counts
        Map<String, Long> statusData = new LinkedHashMap<>();
        for (OrderStatus st : OrderStatus.values()) {
            statusData.put(st.name(), orderRepository.countByStatus(st));
        }

        // Top selling products
        List<TopSellingProductDto> topSelling = new ArrayList<>();
        List<Object[]> rawTop = orderItemRepository.findTopSellingProductsByStatusRaw(OrderStatus.completed, PageRequest.of(0, 5));
        if (rawTop.isEmpty()) {
            rawTop = orderItemRepository.findTopSellingProductsAllRaw(PageRequest.of(0, 5));
        }

        for (Object[] row : rawTop) {
            String name = (String) row[0];
            Number totalSold = (Number) row[1];
            topSelling.add(TopSellingProductDto.builder()
                    .productName(name != null ? name : "Sản phẩm")
                    .totalSold(totalSold != null ? totalSold.longValue() : 0L)
                    .build());
        }

        // Sidebar badge metrics
        long pendingOrders = orderRepository.countByStatus(OrderStatus.pending);
        long lowStock = productRepository.countLowStockProducts();
        long discounts = productRepository.countDiscountedProducts();

        return DashboardKpiDto.builder()
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .thisMonthRevenue(thisMonthRev != null ? thisMonthRev : BigDecimal.ZERO)
                .lastMonthRevenue(lastMonthRev != null ? lastMonthRev : BigDecimal.ZERO)
                .thisMonthOrders(thisMonthOrd)
                .lastMonthOrders(lastMonthOrd)
                .revGrowth(revGrowth)
                .ordGrowth(ordGrowth)
                .revenueTrend(trend)
                .statusData(statusData)
                .recentOrders(orderRepository.findTop6ByOrderByCreatedAtDesc())
                .lowStockProducts(productRepository.findTop6LowStock())
                .topSellingProducts(topSelling)
                .pendingOrdersCount(pendingOrders)
                .lowStockCount(lowStock)
                .discountCount(discounts > 0 ? discounts : 24)
                .abandonedCartCount(4)
                .emailQueueCount(2)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardKpiDto getKpis() {
        return getDashboardKpi();
    }

    @Transactional(readOnly = true)
    public List<RevenueChartPointDto> get6MonthRevenueTrend() {
        return getDashboardKpi().getRevenueTrend();
    }

    @Transactional(readOnly = true)
    public List<com.banlinhkien.entity.Order> getRecentOrders() {
        return orderRepository.findTop6ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<com.banlinhkien.entity.Product> getLowStockProducts() {
        return productRepository.findTop6LowStock();
    }
}
