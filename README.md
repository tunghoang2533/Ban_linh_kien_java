# 🛒 Website Bán Linh Kiện Điện Tử & PC Builder

> **Website thương mại điện tử** chuyên cung cấp linh kiện máy tính, phụ kiện gaming và công cụ xây dựng cấu hình PC thông minh — xây dựng bằng **Java Spring Boot 3.4 + MySQL**.

---

## 🛠️ Công Nghệ Sử Dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 3.4.3 |
| Bảo mật | Spring Security 6, BCrypt |
| Database & ORM | MySQL 8.0, Spring Data JPA, Hibernate, HikariCP |
| Frontend / Template | Thymeleaf, Bootstrap 5, HTML5, CSS3, JavaScript |
| Thanh toán | VNPay Payment Gateway (Sandbox) |
| Build Tool | Apache Maven Wrapper (`mvnw`, `mvnw.cmd`) |

---

## ✅ Yêu Cầu Môi Trường

- **Java JDK 17 hoặc 21+** — kiểm tra: `java -version`
- **MySQL 8.0+** — có thể dùng XAMPP, Laragon, hoặc MySQL cài độc lập

---

## 🚀 Cài Đặt & Khởi Chạy (3 Bước)

### Bước 1 — Import Database

Chạy file import tự động:
```
import_db.bat
```
Hoặc import thủ công file **`db_ban_linh_kien.sql`** qua phpMyAdmin / HeidiSQL / Navicat.

### Bước 2 — Cấu Hình Kết Nối Database

Mở file `src/main/resources/application.yml`, kiểm tra phần:
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/db_ban_linh_kien
    username: root
    password: ${DB_PASSWORD:23122005}   # <-- đổi nếu mật khẩu MySQL khác
```

### Bước 3 — Chạy Ứng Dụng

**Cách nhanh nhất (Windows):** Double-click file **`run.bat`**

Hoặc dùng lệnh:
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Sau khi khởi động, truy cập: **http://localhost:8080**

---

## 🔑 Tài Khoản Mặc Định

| Loại | Username | Password | Trang |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | http://localhost:8080/admin |
| Khách hàng 1 | `nguyenvana` | `password` | http://localhost:8080/login |
| Khách hàng 2 | `test2` | `12345678` | http://localhost:8080/login |

---

## 📂 Cấu Trúc Thư Mục Chính

```
Ban_linh_kien_java/
├── src/
│   ├── main/
│   │   ├── java/com/banlinhkien/     # Source code Java (Controller, Service, Repository, Entity)
│   │   └── resources/
│   │       ├── templates/             # Thymeleaf HTML templates (storefront + admin)
│   │       ├── static/                # CSS, JS, hình ảnh sản phẩm
│   │       └── application.yml        # ⚙️ Cấu hình ứng dụng (DB, port, ...)
│   └── test/                          # Unit test & Integration test
├── db_ban_linh_kien.sql               # 🗄️ File SQL database đầy đủ
├── pom.xml                            # Maven dependencies
├── run.bat                            # ▶️ Chạy nhanh trên Windows
├── import_db.bat                      # 📥 Import database tự động
└── HUONG_DAN_CAI_DAT.md              # 📖 Hướng dẫn chi tiết đầy đủ
```

---

## ❓ Xử Lý Lỗi Thường Gặp

| Lỗi | Khắc phục |
|---|---|
| `Port 8080 already in use` | Tắt chương trình đang dùng port 8080 hoặc đổi port trong `application.yml` |
| `Access denied for user 'root'` | Kiểm tra password MySQL trong `application.yml` |
| `Communications link failure` | Đảm bảo MySQL đang chạy (bật XAMPP/Laragon) |
| `java: release version 21 not supported` | Cài JDK 17 hoặc 21, cập nhật biến môi trường `JAVA_HOME` |

📖 **Xem hướng dẫn chi tiết tại: [`HUONG_DAN_CAI_DAT.md`](HUONG_DAN_CAI_DAT.md)**
