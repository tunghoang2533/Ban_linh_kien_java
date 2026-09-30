package com.banlinhkien.controller.view.admin;

import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.Product;
import com.banlinhkien.entity.User;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/export")
@RequiredArgsConstructor
public class AdminExportController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/orders")
    public void exportOrders(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=danh_sach_don_hang_" + getTimestamp() + ".csv");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff'); // UTF-8 BOM
        writer.println("Mã đơn,Khách hàng,Email,Số điện thoại,Địa chỉ,Phương thức TT,Trạng thái,Tổng tiền,Ngày tạo");

        List<Order> orders = orderRepository.findAll();
        for (Order o : orders) {
            writer.printf("\"#%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    o.getId(),
                    escapeCsv(o.getCustomerName()),
                    escapeCsv(o.getCustomerEmail()),
                    escapeCsv(o.getCustomerPhone()),
                    escapeCsv(o.getCustomerAddress()),
                    o.getPaymentMethod() != null ? o.getPaymentMethod().name() : "",
                    o.getStatus() != null ? o.getStatus().name() : "",
                    o.getTotalAmount() != null ? o.getTotalAmount().toString() : "0",
                    o.getCreatedAt() != null ? o.getCreatedAt().toString() : ""
            );
        }
        writer.flush();
    }

    @GetMapping("/products")
    public void exportProducts(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=danh_sach_san_pham_" + getTimestamp() + ".csv");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("Mã SP,Tên sản phẩm,Danh mục,Thương hiệu,Giá gốc,Giảm giá (%),Tồn kho,Trạng thái");

        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            writer.printf("\"#%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\",\"%s\"%n",
                    p.getId(),
                    escapeCsv(p.getName()),
                    p.getCategory() != null ? escapeCsv(p.getCategory().getName()) : "",
                    p.getBrand() != null ? escapeCsv(p.getBrand().getName()) : "",
                    p.getPrice() != null ? p.getPrice().toString() : "0",
                    p.getDiscountPercent() != null ? p.getDiscountPercent().toString() : "0",
                    p.getQuantity() != null ? p.getQuantity() : 0,
                    Boolean.TRUE.equals(p.getIsActive()) ? "Đang bán" : "Ẩn"
            );
        }
        writer.flush();
    }

    @GetMapping("/users")
    public void exportUsers(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=danh_sach_khach_hang_" + getTimestamp() + ".csv");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("Mã KH,Tên đăng nhập,Họ tên,Email,Số điện thoại,Vai trò,Trạng thái,Ngày tham gia");

        List<User> users = userRepository.findAll();
        for (User u : users) {
            writer.printf("\"#%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    u.getId(),
                    escapeCsv(u.getUsername()),
                    escapeCsv(u.getFullName()),
                    escapeCsv(u.getEmail()),
                    escapeCsv(u.getPhone()),
                    u.getRole() != null ? escapeCsv(u.getRole()) : "USER",
                    Boolean.TRUE.equals(u.getIsActive()) ? "Hoạt động" : "Khóa",
                    u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
            );
        }
        writer.flush();
    }

    @GetMapping("/revenue")
    public void exportRevenue(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=bao_cao_doanh_thu_" + getTimestamp() + ".csv");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("Mã ĐH,Ngày đặt,Khách hàng,Phí ship,Giảm giá voucher,Doanh thu thực nhận,Trạng thái");

        List<Order> orders = orderRepository.findAll();
        for (Order o : orders) {
            writer.printf("\"#%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    o.getId(),
                    o.getCreatedAt() != null ? o.getCreatedAt().toString() : "",
                    escapeCsv(o.getCustomerName()),
                    o.getShippingFee() != null ? o.getShippingFee().toString() : "0",
                    o.getDiscountAmount() != null ? o.getDiscountAmount().toString() : "0",
                    o.getTotalAmount() != null ? o.getTotalAmount().toString() : "0",
                    o.getStatus() != null ? o.getStatus().name() : ""
            );
        }
        writer.flush();
    }

    @GetMapping("/profit")
    public void exportProfit(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=bao_cao_loi_nhuan_" + getTimestamp() + ".csv");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("Mã SP,Tên sản phẩm,Giá vốn (Cost),Giá bán niêm yết,Giá bán hiện tại,Lợi nhuận gộp ước tính,Tỷ suất LN (%)");

        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            BigDecimal cost = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
            BigDecimal selling = p.getFinalPrice() != null ? p.getFinalPrice() : BigDecimal.ZERO;
            BigDecimal margin = selling.subtract(cost);
            double marginPercent = cost.compareTo(BigDecimal.ZERO) > 0
                    ? margin.doubleValue() / cost.doubleValue() * 100
                    : 0.0;

            writer.printf("\"#%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.2f%%\"%n",
                    p.getId(),
                    escapeCsv(p.getName()),
                    cost.toString(),
                    p.getPrice() != null ? p.getPrice().toString() : "0",
                    selling.toString(),
                    margin.toString(),
                    marginPercent
            );
        }
        writer.flush();
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
