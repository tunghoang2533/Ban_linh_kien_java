@echo off
chcp 65001 >nul
title Khởi Động Website Bán Linh Kiện - Spring Boot
echo =====================================================================
echo       HỆ THỐNG WEBSITE BÁN LINH KIỆN ĐIỆN TỬ (JAVA SPRING BOOT)
echo =====================================================================
echo.

:: 1. Kiểm tra Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [LỖI] Chưa tìm thấy Java trong biến môi trường PATH!
    echo Vui lòng cài đặt JDK 17 hoặc JDK 21 trở lên để chạy ứng dụng.
    echo.
    pause
    exit /b 1
)

echo [1/3] Đã phát hiện Java môi trường:
java -version
echo.
echo [2/3] Lưu ý: Hãy đảm bảo MySQL đã được BẬT (trên XAMPP hoặc Laragon)
echo       và đã import cơ sở dữ liệu từ file "db_ban_linh_kien.sql"!
echo.

:: 2. Khởi động ứng dụng
echo [3/3] Đang khởi động hệ thống trên cổng 8088...
echo - Trang chủ:       http://localhost:8088
echo - Đăng nhập:       http://localhost:8088/login
echo - Quản trị viên:   http://localhost:8088/admin (Tài khoản: admin / admin123)
echo - Khách hàng test: nguyenvana / password  hoặc  test2 / 12345678
echo =====================================================================
echo Bấm Ctrl + C để dừng hệ thống.
echo.

if exist "target\ban-linh-kien-java-0.0.1-SNAPSHOT.jar" (
    java -jar target\ban-linh-kien-java-0.0.1-SNAPSHOT.jar
) else (
    call mvnw.cmd spring-boot:run
)

pause
