package com.banlinhkien.controller.view.admin;

import com.banlinhkien.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("activeNav", "dashboard");
        model.addAttribute("kpis", adminDashboardService.getKpis());
        model.addAttribute("revenueTrend", adminDashboardService.get6MonthRevenueTrend());
        model.addAttribute("recentOrders", adminDashboardService.getRecentOrders());
        model.addAttribute("lowStockProducts", adminDashboardService.getLowStockProducts());
        return "admin/dashboard";
    }
}
