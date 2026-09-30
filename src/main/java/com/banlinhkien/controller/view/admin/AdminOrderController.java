package com.banlinhkien.controller.view.admin;

import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.User;
import com.banlinhkien.entity.WarehouseLog;
import com.banlinhkien.enums.OrderStatus;
import com.banlinhkien.repository.UserRepository;
import com.banlinhkien.repository.WarehouseLogRepository;
import com.banlinhkien.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final WarehouseLogRepository warehouseLogRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), size, Sort.by("createdAt").descending());
        Page<Order> orders = adminOrderService.getOrders(status, search, pageable);

        model.addAttribute("activeNav", "orders");
        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);
        model.addAttribute("search", search);

        // Status counts for tabs
        model.addAttribute("countAll", adminOrderService.countByStatus(null));
        model.addAttribute("countPending", adminOrderService.countByStatus(OrderStatus.pending));
        model.addAttribute("countProcessing", adminOrderService.countByStatus(OrderStatus.processing));
        model.addAttribute("countShipped", adminOrderService.countByStatus(OrderStatus.shipped));
        model.addAttribute("countCompleted", adminOrderService.countByStatus(OrderStatus.completed));
        model.addAttribute("countCancelled", adminOrderService.countByStatus(OrderStatus.cancelled));

        return "admin/orders/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = adminOrderService.getOrder(id);
        List<WarehouseLog> logs = warehouseLogRepository.findByReferenceIdOrderByCreatedAtDesc(id);

        model.addAttribute("activeNav", "orders");
        model.addAttribute("order", order);
        model.addAttribute("warehouseLogs", logs);
        model.addAttribute("orderStatuses", OrderStatus.values());

        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String trackingCode,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long adminUserId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                adminUserId = user.getId();
            }
        }

        try {
            Order updated = adminOrderService.updateStatus(id, status, trackingCode, adminUserId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật đơn hàng #" + id + " sang trạng thái '" + updated.getStatus() + "' thành công!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Không thể cập nhật trạng thái đơn hàng #{}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi cập nhật trạng thái đơn hàng #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }
}
