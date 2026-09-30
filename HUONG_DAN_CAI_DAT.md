# HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN BAN_LINH_KIEN_JAVA

Hệ thống Website Bán Linh Kiện Máy Tính & Xây Dựng Cấu Hình PC (PC Builder)  
**Công nghệ:** Java 21 / 17+, Spring Boot 3.4.3, Spring Security, Spring Data JPA, Thymeleaf, MySQL.

---

## 1. YÊU CẦU MÔI TRƯỜNG CẦN CÓ

1. **Java JDK:** Phiên bản 17 hoặc 21 trở lên (Đã cài đặt và có trong biến môi trường `PATH`).
   - Kiểm tra bằng cách mở CMD / Terminal gõ: `java -version`
2. **Hệ quản trị CSDL MySQL:** Phiên bản 8.0 trở lên.
   - Có thể sử dụng **Laragon**, **XAMPP**, **WampServer** hoặc MySQL cài độc lập.

---

## 2. HƯỚNG DẪN IMPORT CƠ SỞ DỮ LIỆU (DATABASE)

File cơ sở dữ liệu đã được xuất sẵn tại thư mục gốc: **`db_ban_linh_kien.sql`**  
*(File đã chứa sẵn lệnh tạo cơ sở dữ liệu `db_ban_linh_kien` cùng đầy đủ 53 bảng dữ liệu mẫu: sản phẩm, thương hiệu, danh mục, tài khoản, đơn hàng...)*

### Cách 1: Dùng công cụ đồ họa (HeidiSQL, phpMyAdmin, Navicat, DBeaver) - *Khuyên Dùng*
- Mở **Laragon** (hoặc XAMPP) và bật MySQL.
- Mở **phpMyAdmin** (`http://localhost/phpmyadmin`) hoặc **HeidiSQL**.
- Nhấn vào menu **Import** (hoặc mở file SQL) -> Chọn file **`db_ban_linh_kien.sql`** -> Nhấn **Execute / Go** để chạy.

### Cách 2: Dùng file `import_db.bat` có sẵn trong thư mục
- Click đúp vào file **`import_db.bat`**.
- Nhập thông tin username / password MySQL của máy bạn, script sẽ tự động nạp database.

### Cách 3: Dùng dòng lệnh CMD / PowerShell
```bash
mysql -u root -p < db_ban_linh_kien.sql
```

---

## 3. CẤU HÌNH KẾT NỐI DATABASE (NẾU CẦN)

Mặc định, cấu hình database nằm trong file:  
📁 `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/db_ban_linh_kien?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
    username: root
    password: ${DB_PASSWORD:23122005}
```

> **Ghi chú quan trọng:**  
> - Nếu máy của bạn dùng MySQL với mật khẩu khác (ví dụ dùng XAMPP / Laragon mặc định không có mật khẩu - để trống), bạn có thể:
>   - **Cách A:** Sửa dòng `password: ""` trong file `src/main/resources/application.yml`.
>   - **Cách B:** Khi chạy lệnh, truyền trực tiếp:  
>     `java -jar target/ban-linh-kien-java-0.0.1-SNAPSHOT.jar --spring.datasource.password=""`

---

## 4. HƯỚNG DẪN KHỞI CHẠY HỆ THỐNG

### Cách 1: Click đúp chạy ngay bằng `run.bat` (Nhanh & Tiện nhất)
- Trong thư mục dự án, click đúp chuột vào file **`run.bat`**.
- Cửa sổ ứng dụng sẽ tự khởi động trên cổng `8080`.

### Cách 2: Chạy trực tiếp từ file JAR đã build sẵn
Mở CMD / PowerShell tại thư mục dự án và chạy lệnh:
```bash
java -jar target/ban-linh-kien-java-0.0.1-SNAPSHOT.jar
```

### Cách 3: Chạy bằng công cụ Maven Wrapper (Tự động tải phụ thuộc nếu cần)
- Trên **Windows**:
  ```cmd
  mvnw.cmd spring-boot:run
  ```
- Trên **macOS / Linux**:
  ```bash
  ./mvnw spring-boot:run
  ```

### Cách 4: Mở dự án bằng IntelliJ IDEA / Eclipse / VS Code
1. Khởi động **IntelliJ IDEA** -> Chọn **Open** -> Chọn thư mục **`Ban_linh_kien_java`**.
2. Chọn mở dưới dạng **Maven Project**.
3. Đợi IntelliJ đồng bộ dependencies xong.
4. Tìm đến file `src/main/java/com/banlinhkien/BanLinhKienApplication.java`, nhấn nút **Run** (biểu tượng tam giác xanh).

---

## 5. THÔNG TIN TRUY CẬP VÀ TÀI KHOẢN MẪU

Khi hệ thống khởi động thành công, truy cập qua trình duyệt:

- **Trang chủ Website:** [http://localhost:8080](http://localhost:8080)
- **Trang Đăng nhập:** [http://localhost:8080/login](http://localhost:8080/login)
- **Công cụ Xây dựng Cấu hình PC:** [http://localhost:8080/pc-builder](http://localhost:8080/pc-builder)
- **Giỏ hàng & Thanh toán:** [http://localhost:8080/cart](http://localhost:8080/cart)
- **Trang Quản Trị Viên (Admin Dashboard):** [http://localhost:8080/admin](http://localhost:8080/admin)

### Danh sách tài khoản đăng nhập mẫu:

| Loại tài khoản | Tên đăng nhập (Username) | Mật khẩu (Password) | Quyền hạn |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | **`admin`** | **`admin123`** | Toàn quyền quản trị hệ thống, duyệt đơn hàng, quản lý sản phẩm, danh mục, thương hiệu, báo cáo doanh thu |
| **Khách hàng mẫu 1** | **`nguyenvana`** | **`password`** | Mua hàng, xem lịch sử đơn, xây cấu hình PC |
| **Khách hàng mẫu 2** | **`test2`** | **`12345678`** | Mua hàng, xem lịch sử đơn, xây cấu hình PC |

---

## 6. XỬ LÝ MỘT SỐ VẤN ĐỀ THƯỜNG GẶP (TROUBLESHOOTING)

1. **Lỗi: `Port 8080 was already in use`**
   - *Nguyên nhân:* Cổng 8080 đang bị chương trình khác (như Tomcat, Oracle hoặc lần chạy trước) chiếm giữ.
   - *Khắc phục:* Mở CMD với quyền Admin và tắt tiến trình:
     ```cmd
     netstat -ano | findstr :8080
     taskkill /PID <MÃ_PID> /F
     ```
     Hoặc đổi port trong `application.yml` thành `port: 8081`.

2. **Lỗi: `Communications link failure` hoặc `Access denied for user 'root'@'localhost'`**
   - *Nguyên nhân:* MySQL chưa được bật hoặc mật khẩu MySQL không khớp.
   - *Khắc phục:* Đảm bảo Laragon/XAMPP đã bật MySQL (nút xanh Start). Kiểm tra và cập nhật password trong `src/main/resources/application.yml` cho đúng với MySQL trên máy bạn.

3. **Lỗi: `java: release version 21 not supported`**
   - *Khắc phục:* Cài đặt JDK 21 hoặc trong IntelliJ vào `Project Structure` -> `Project SDK` -> chọn phiên bản JDK 17 hoặc 21.
