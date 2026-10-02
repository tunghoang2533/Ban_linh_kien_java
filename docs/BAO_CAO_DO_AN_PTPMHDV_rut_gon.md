# BÁO CÁO ĐỒ ÁN: XÂY DỰNG API VÀ CLIENT CHO HỆ THỐNG BÁN LINH KIỆN PC

---

<div align="center">

**TRƯỜNG ĐẠI HỌC ... | KHOA CÔNG NGHỆ THÔNG TIN | BỘ MÔN CÔNG NGHỆ PHẦN MỀM**

**ĐỀ TÀI: XÂY DỰNG DỊCH VỤ API VÀ ỨNG DỤNG CLIENT CHO HỆ THỐNG BÁN HÀNG LINH KIỆN PC TRỰC TUYẾN**

**Kho mã nguồn:** `tunghoang2533/Ban_linh_kien_java` | **Nhánh:** `arena/01a0fa5e-ban-linh-kien-java`

*Hà Nội, tháng 10 năm 2026*

</div>

---

## LỜI CAM ĐOAN

Nhóm cam đoan: (1) Đồ án là công trình do chính nhóm thực hiện; (2) Toàn bộ mã nguồn tự xây dựng, không sao chép; (3) Thư viện, framework sử dụng hợp pháp; (4) Số liệu, kết quả là kết quả thực tế; (5) Nội dung lý thuyết được trích dẫn đầy đủ. Nhóm chịu hoàn toàn trách nhiệm.

---

# MỤC LỤC

1. [MỞ ĐẦU](#mở-đầu)
2. [CHƯƠNG 1. GIỚI THIỆU TỔNG QUAN](#chương-1-giới-thiệu-tổng-quan)
3. [CHƯƠNG 2. PHÂN TÍCH VÀ THIẾT KẾ](#chương-2-phân-tích-và-thiết-kế)
4. [CHƯƠNG 3. KẾT QUẢ THỰC NGHIỆM](#chương-3-kết-quả-thực-nghiệm)
5. [KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN](#kết-luận-và-hướng-phát-triển)
6. [TÀI LIỆU THAM KHẢO](#tài-liệu-tham-khảo)

---

# MỞ ĐẦU

## 1. Lý do chọn đề tài

Thị trường linh kiện PC Việt Nam có đặc thù: sản phẩm phải tương thích (CPU + Mainboard socket), tồn kho mỏng, giá trị đơn hàng cao (10-50 triệu). Hiện trạng: thiếu công cụ tư vấn kỹ thuật, kiến trúc monolithic khó mở rộng, dễ bán vượt tồn kho.

**Giải pháp 3 trụ cột:**
1. **Tách bạch dịch vụ-client**: Backend REST API + Client web tiêu thụ API
2. **Đưa tri thức kỹ thuật vào dịch vụ**: `PcBuilderService` kiểm tra socket, `PcBuilderAiService` gợi ý cấu hình
3. **Bảo đảm toàn vẹn**: Khóa bi quan chống bán vượt tồn kho, HMAC-SHA512 bảo đảm thanh toán

## 2. Mục tiêu nghiên cứu

**Mục tiêu tổng quát:** Nghiên cứu, vận dụng SOA và REST để xây dựng hệ thống thương mại điện tử linh kiện PC.

**Mục tiêu cụ thể:**
- Hệ thống hóa lý thuyết SOA, REST
- Phân tích yêu cầu bằng UML
- Thiết kế kiến trúc phân tầng
- Thiết kế CSDL 17 bảng
- Thiết kế API 6 nhóm tài nguyên
- Hiện thực client
- Giải quyết tranh chấp tồn kho
- Tích hợp VNPAY an toàn
- Xây dựng bộ kiểm thử 60+ ca

## 3. Đối tượng và phạm vi

**Đối tượng:** Quy trình bán lẻ linh kiện PC, kiến trúc SOA, REST, giao dịch CSDL, Spring Security, VNPAY.

**Phạm vi:**
- Nghiệp vụ: Duyệt sản phẩm, Build PC, giỏ hàng, đặt hàng, thanh toán, quản trị
- Kiến trúc: Backend layered monolith + REST API + Client web
- Dữ liệu: MySQL 8, 17 bảng, JPA/Hibernate
- Bảo mật: Xác thực phiên, BCrypt, RBAC, chống IDOR, HMAC-SHA512
- Kiểm thử: Đơn vị, tích hợp, đồng thời

**Loại trừ:** Microservices, client di động, JWT, đám mây, logistics, kiểm thử tải.

## 4. Phương pháp nghiên cứu

- **Lý thuyết:** Tổng hợp tài liệu (Fielding, Erl, Spring, MySQL, VNPAY)
- **OOAD:** Mô hình hóa bằng UML (Use Case, Sequence Diagram)
- **Phát triển:** 5 lát cắt dọc (Walking Skeleton → Build PC → Quản trị)
- **Kiểm thử:** JUnit 5 (đơn vị), @SpringBootTest (tích hợp), ExecutorService (đồng thời)
- **Đánh giá:** Đối chiếu yêu cầu với kết quả

## 5. Cấu trúc đồ án

**Chương 1:** Giới thiệu tổng quan (bối cảnh, lý thuyết, công nghệ)
**Chương 2:** Phân tích và thiết kế (yêu cầu, UML, kiến trúc, CSDL, API, client)
**Chương 3:** Kết quả thực nghiệm (môi trường, hiện thực, giao diện, kiểm thử, đánh giá)
**Kết luận:** Kết quả, hạn chế, hướng phát triển

---

# CHƯƠNG 1. GIỚI THIỆU TỔNG QUAN

## 1.1. Đặt vấn đề

**Bối cảnh:** Thị trường linh kiện PC phát triển nhờ: làm việc trực tuyến, esports, sáng tạo nội dung, AI. Người dùng tự lắp ráp để tối ưu hiệu năng/chi phí.

**Rào cản:**
- Sai socket → không lắp được
- Thiếu công suất → sập nguồn
- Nghẽn cổ chai → hiệu năng thấp
- Chọn hết hàng → phải làm lại

**Hạn chế hệ thống hiện hành:**
- Thiếu tư vấn kỹ thuật tự động
- Kiến trúc monolithic khó mở rộng
- Dễ bán vượt tồn kho

## 1.2. SOA và REST

**SOA:** Kiến trúc tổ chức chức năng thành dịch vụ - đơn vị tự chứa, có ranh giới rõ ràng, công bố năng lực qua hợp đồng.

**4 nguyên lý cốt lõi:**
- **Ràng buộc lỏng:** Client chỉ phụ thuộc hợp đồng
- **Tái sử dụng:** `CartService` dùng cho REST API, MVC, PcBuilder
- **Liên tác:** HTTP/JSON là chuẩn mở
- **Kết hợp:** `CheckoutService` điều phối Product, Shipping, Voucher

**REST:** 6 ràng buộc:
1. Client-Server: Tách bạch client/server
2. Stateless: Mỗi yêu cầu chứa đủ thông tin (phiên cho giỏ hàng)
3. Cacheable: Tài nguyên tĩnh có thể lưu đệm
4. Uniform Interface: Resources + HTTP Verbs (đạt mức 2)
5. Layered System: Client không biết kết nối trực tiếp hay qua proxy
6. Code on Demand: Server gửi mã JavaScript

## 1.3. Công nghệ

| Thành phần | Công nghệ | Phiên bản | Vai trò |
|---|---|---|---|
| Ngôn ngữ | Java | 17/21 | Backend |
| Khung ứng dụng | Spring Boot | 3.4.3 | Tự động cấu hình |
| Web | Spring Web MVC | 6.2.x | Định tuyến HTTP |
| Bảo mật | Spring Security | 6.4.x | Xác thực, phân quyền |
| Truy cập dữ liệu | Spring Data JPA | 3.4.x | Sinh repository |
| ORM | Hibernate | 6.6.x | Ánh xạ O-R |
| CSDL | MySQL | 8.x | Lưu trữ, ACID |
| Template | Thymeleaf | 3.1.x | Kết xuất HTML |
| CSS | Bootstrap | 5.x | Lưới đáp ứng |
| API client | Fetch API | Chuẩn | Gọi HTTP bất đồng bộ |
| Thanh toán | VNPAY | API v2.1.0 | Cổng thanh toán |
| AI | Groq | Llama 3.3 70B | Gợi ý cấu hình |

**MySQL:** Engine InnoDB, hỗ trợ ACID, chỉ mục tối ưu

**VNPAY:** HMAC-SHA512, so sánh thời gian hằng, hai kênh (Return URL + IPN Webhook)

---

# CHƯƠNG 2. PHÂN TÍCH VÀ THIẾT KẾ

## 2.1. Phân tích yêu cầu

**Yêu cầu chức năng:**
- **Khách vãng lai:** Duyệt sản phẩm, Build PC, đăng ký, đăng nhập
- **Khách hàng:** Quản lý giỏ hàng, đặt hàng, thanh toán, theo dõi đơn, đánh giá
- **Quản trị viên:** Quản lý sản phẩm, đơn hàng, kho, voucher, người dùng, thống kê

**Yêu cầu phi chức năng:**
- Hiệu năng: API < 500ms cho 100 yêu cầu
- Bảo mật: BCrypt, RBAC, chống IDOR, HMAC-SHA512
- Toàn vẹn: Không bán vượt tồn kho
- Mở rộng: Kiến trúc sẵn sàng cho client mới

**Quy tắc nghiệp vụ:**
- Giá cuối = price × (1 - discount_percent/100)
- Không cho phép thêm vào giỏ nếu quantity > stock
- Mỗi người dùng chỉ dùng mỗi mã giảm giá một lần
- Chỉ ghi nhận thanh toán sau khi nhận IPN hợp lệ
- Chỉ cho phép ráp nếu linh kiện tương thích socket

## 2.2. Thiết kế hệ thống

### 2.2.1. Kiến trúc phân tầng

```
CLIENT (Thymeleaf + Fetch API + Bootstrap)
       ↓
API GATEWAY (Spring Boot 3.4.3, Port 8088)
       ↓
SERVICE (Cart, Order, Checkout, PcBuilder, Voucher, Vnpay)
       ↓
REPOSITORY (JPA + Hibernate)
       ↓
DATABASE (MySQL 8, InnoDB, 17 bảng)
```

**Bảng phân định trách nhiệm:**
| Tầng | Trách nhiệm | Công nghệ |
|---|---|---|
| Client | Hiển thị, thu thập đầu vào, gọi API | Thymeleaf, Fetch, Bootstrap |
| API Gateway | Tiếp nhận HTTP, định tuyến, xác thực | Spring Web MVC, Security |
| Service | Logic nghiệp vụ | Spring Service |
| Repository | Truy cập CSDL | Spring Data JPA, Hibernate |
| Database | Lưu trữ, ACID | MySQL 8 |

### 2.2.2. Thiết kế cơ sở dữ liệu

**17 bảng cốt lõi:**
- **users**: id, email, password, full_name, phone, address, role, is_active
- **categories**: id, name, slug, parent_id, is_active
- **products**: id, category_id, name, slug, price, discount_percent, stock, is_active
- **product_specs**: id, product_id, spec_name, spec_value
- **orders**: id, user_id, access_token, status, total_amount, shipping_fee, discount_amount, payment_method
- **order_items**: id, order_id, product_id, quantity, unit_price, total_price
- **vouchers**: id, code, discount_percent, min_order_amount, quantity, valid_from, valid_until
- **voucher_usages**: id, voucher_id, user_id, order_id, used_at
- **provinces, districts, wards**: id, name, code, parent_id

### 2.2.3. Thiết kế API

**Quy ước:** JSON, BigDecimal cho tiền tệ, ISO-8601 cho ngày giờ.

**API theo nhóm:**
| Nhóm | Endpoint | Method | Mô tả |
|---|---|---|---|
| Sản phẩm | `/api/san-pham` | GET | Danh sách |
| | `/api/san-pham/{id}/quick-view` | GET | Xem nhanh |
| Danh mục | `/api/danh-muc` | GET | Cây danh mục |
| Giỏ hàng | `/api/cart/add` | POST | Thêm |
| | `/api/cart/update` | POST | Cập nhật |
| | `/api/cart/remove` | POST | Xóa |
| Build PC | `/api/build-pc/components` | GET | Danh sách linh kiện |
| | `/api/build-pc/select` | POST | Chọn |
| | `/api/build-pc/add-to-cart` | POST | Chuyển vào giỏ |
| Voucher | `/api/voucher/apply` | POST | Áp mã |
| Địa giới | `/api/location/provinces` | GET | Tỉnh |
| | `/api/location/districts` | GET | Huyện |
| | `/api/location/wards` | GET | Xã |
| Đặt hàng | `/api/orders` | POST | Đặt hàng |
| | `/api/orders/{id}/vnpay` | POST | URL VNPAY |
| VNPAY | `/vnpay/return` | GET | Return URL |
| | `/api/vnpay/ipn` | GET | IPN Webhook |

### 2.2.4. Thiết kế ứng dụng Client

**Cấu trúc:**
```
templates/
├── layout/
│   ├── storefront.html  # Bố cục khách hàng
│   └── admin.html       # Bố cục quản trị
├── storefront/         # Giao diện khách
│   ├── index.html       # Trang chủ
│   ├── products/        # Sản phẩm
│   ├── cart/            # Giỏ hàng
│   ├── checkout/        # Thanh toán
│   └── buildpc/         # Build PC
└── admin/              # Giao diện quản trị
    ├── dashboard.html
    ├── products/
    ├── orders/
    └── users/
```

**Luồng:** Thymeleaf (SSR) + Fetch API (CSR)

---

# CHƯƠNG 3. KẾT QUẢ THỰC NGHIỆM

## 3.1. Môi trường cài đặt

**Phần cứng:** CPU i5/Ryzen 5, RAM 16GB, SSD 512GB, Windows 10/11/Ubuntu 22.04

**Phần mềm:** Java JDK 17+, Spring Boot 3.4.3, MySQL 8, Node.js 18+, Maven 3.9+

**Quy trình:**
```bash
git clone https://github.com/tunghoang2533/Ban_linh_kien_java.git
cd Ban_linh_kien_java
git checkout arena/01a0fa5e-ban-linh-kien-java
# Tạo database ban_linh_kien, import db_ban_linh_kien.sql
# Chỉnh sửa application.yml
./mvnw spring-boot:run
```

## 3.2. Kết quả hiện thực

**Cấu trúc mã nguồn:**
```
Ban_linh_kien_java/
├── src/main/java/com/banlinhkien/
│   ├── BanLinhKienApplication.java
│   ├── config/          # SecurityConfig, WebMvcConfig
│   ├── controller/
│   │   ├── api/         # CartApi, ProductApi, OrderApi
│   │   └── web/         # Storefront, Admin
│   ├── service/         # Cart, Product, Order, Checkout, Voucher, Vnpay, PcBuilder
│   ├── repository/      # JPA Repository
│   ├── dto/            # Request/Response
│   └── entity/         # JPA Entity
└── src/test/java/...   # 60+ ca kiểm thử
```

**Cơ chế bảo đảm toàn vẹn:**

**Khóa bi quan chống bán vượt tồn kho:**
```java
@Transactional
public OrderResponse placeOrder(OrderRequest request) {
    for (CartItemDto item : request.getItems()) {
        Product product = productRepository.findByIdForUpdate(item.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        
        if (product.getStock() < item.getQuantity()) {
            throw new BusinessException("Không đủ tồn kho");
        }
        product.setStock(product.getStock() - item.getQuantity());
    }
    // Tạo đơn hàng, lưu CSDL
}
```

**Idempotency cho VNPAY:**
```java
@Transactional
public String processPaymentResult(Map<String, String> vnpParams) {
    if (!verifyChecksum(vnpParams)) {
        return createVnpayResponse("97", "Invalid checksum");
    }
    
    Order order = orderRepository.findById(orderId).orElseThrow();
    if (order.getStatus() == OrderStatus.PAID) {
        return createVnpayResponse("02", "Order already paid"); // Idempotency
    }
    
    // Kiểm tra số tiền, cập nhật trạng thái
    order.setStatus(OrderStatus.PAID);
    orderRepository.save(order);
    return createVnpayResponse("00", "Success");
}
```

## 3.3. Kết quả giao diện

**Giao diện khách hàng:**
- Trang chủ, danh sách sản phẩm, chi tiết sản phẩm
- Công cụ Build PC (7 khe, kiểm tra tương thích)
- Gợi ý cấu hình AI
- Giỏ hàng, thanh toán, xác nhận đơn hàng
- Cổng VNPAY Sandbox
- Tài khoản cá nhân, lịch sử đơn hàng

**Giao diện quản trị:**
- Bảng điều khiển (thống kê)
- Quản lý sản phẩm, đơn hàng, kho
- Nhật ký biến động kho
- Quản lý voucher, người dùng

## 3.4. Kiểm thử

**Phân tầng:**
- **Đơn vị:** 35 ca (JUnit 5, Mockito)
- **Tích hợp:** 20 ca (@SpringBootTest, MockMvc)
- **Đồng thời:** 5 ca (ExecutorService, CountDownLatch)

**Kiểm thử đồng thời:** 15 luồng đặt 1 sản phẩm có `stock = 10`
- **Kết quả:** 10 đơn thành công, 5 đơn thất bại, tồn kho cuối = 0
- **Kết luận:** Khóa bi quan hoạt động hiệu quả

**Ma trận kịch bản:** 15+ kịch bản, tất cả đạt

**Đánh giá:** Tất cả 10 mục tiêu (MT01-MT10) đều hoàn thành

---

# KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## 1. Kết quả đạt được

**Lý thuyết:** Nắm vững SOA, REST, ACID, Spring, VNPAY

**Thực tiễn:**
- Hệ thống hoàn chỉnh: Backend + API + Client
- CSDL: 17 bảng, thiết kế chuẩn hóa
- API: 6 nhóm, 25+ endpoint
- Giao diện: 44 khuôn mẫu
- Kiểm thử: 60+ ca, phủ đầy đủ
- Cơ chế: Khóa bi quan, idempotency

## 2. Hạn chế

| Hạn chế | Nguyên nhân | Giải pháp |
|---|---|---|
| Kiến trúc monolith | Quy mô đồ án | Phân rã microservices |
| Giỏ hàng lưu trong phiên | Đơn giản hóa | Chuyển sang Redis |
| Chưa HATEOAS | Quy mô client | Bổ sung hypermedia |
| Chưa API versioning | Chưa cần thiết | Áp dụng /api/v1/... |
| Chưa tối ưu cache | Chưa ưu tiên | Redis, Cache-Control |

## 3. Hướng phát triển

**Ngắn hạn (1-3 tháng):**
- [ ] API versioning
- [ ] HATEOAS
- [ ] Tối ưu cache
- [ ] Rate limiting
- [ ] Chuyển giỏ hàng sang Redis
- [ ] JWT authentication

**Trung hạn (3-6 tháng):**
- [ ] Container hóa (Docker)
- [ ] Triển khai đám mây
- [ ] CI/CD pipeline
- [ ] Phân rã microservices
- [ ] API Gateway

**Dài hạn (6-12 tháng):**
- [ ] Ứng dụng di động
- [ ] Tích hợp logistics
- [ ] Chatbot AI
- [ ] Khuyến mại thông minh

## 4. Bài học kinh nghiệm

**Kỹ thuật:**
- Kiến trúc là quan trọng nhất
- Tuân thủ chuẩn mực (REST, SOLID)
- Kiểm thử là một phần của phát triển
- Quản lý trạng thái cẩn thận
- Bảo mật từ đầu

**Quản lý:**
- Phân chia công việc rõ ràng
- Giao tiếp thường xuyên
- Quản lý phiên bản chặt chẽ
- Tài liệu hóa đầy đủ
- Lập kế hoạch linh hoạt

---

# TÀI LIỆU THAM KHẢO

[1] R. T. Fielding, "Architectural Styles and the Design of Network-based Software Architectures", 2000.

[2] Thomas Erl, "Service-Oriented Architecture (SOA)", Prentice Hall, 2005.

[3] Spring Framework Documentation, https://docs.spring.io/spring-framework/

[4] Spring Boot Documentation, https://docs.spring.io/spring-boot/

[5] Spring Security Documentation, https://docs.spring.io/spring-security/

[6] MySQL 8 Documentation, https://dev.mysql.com/doc/

[7] VNPAY Developer Documentation, https://developers.vnpayment.vn/

[8] Thymeleaf Documentation, https://www.thymeleaf.org/

---

<div align="center">
**HẾT**
</div>
