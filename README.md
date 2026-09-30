# Dự Án Website Bán Linh Kiện Điện Tử & Build PC (Java Spring Boot)

Website thương mại điện tử chuyên cung cấp linh kiện máy tính, phụ kiện gaming và công cụ tự xây dựng cấu hình PC thông minh.

## 🛠️ Công Nghệ Sử Dụng
- **Backend:** Java 21, Spring Boot 3.4.3
- **Bảo mật:** Spring Security, BCrypt Password Encoder
- **Database & ORM:** MySQL 8.0, Spring Data JPA, Hibernate, HikariCP
- **Frontend / Template:** Thymeleaf, Thymeleaf Layout Dialect, Bootstrap 5, HTML5, CSS3, JavaScript
- **Thanh toán:** Tích hợp VNPay Payment Gateway Sandbox
- **Build Tool:** Apache Maven Wrapper (`mvnw`, `mvnw.cmd`)

## 🚀 Khởi Động Nhanh Trong 3 Bước   
1. **Import Database:** Mở file `db_ban_linh_kien.sql` nạp vào MySQL (Port 3306).
2. **Cấu hình:** Sửa mật khẩu MySQL trong `src/main/resources/application.yml` (nếu mật khẩu của bạn khác `23122005`).
3. **Chạy ứng dụng:**
   - Double click file **`run.bat`**  
   - Hoặc chạy lệnh: `mvnw.cmd spring-boot:run` (Windows) / `./mvnw spring-boot:run` (Linux/Mac).

## 🔑 Tài Khoản Mặc Định
- **Admin:** `admin` / `admin123` (Quản trị tại `/admin`)
- **Khách hàng:** `nguyenvana` / `password` hoặc `test2` / `12345678`

*Xem chi tiết hướng dẫn tại file `HUONG_DAN_CAI_DAT.md`.*
