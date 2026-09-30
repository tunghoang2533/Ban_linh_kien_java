@echo off
chcp 65001 >nul
title Công Cụ Nhập Database MySQL - db_ban_linh_kien
echo =====================================================================
echo           CÔNG CỤ NHẬP CƠ SỞ DỮ LIỆU db_ban_linh_kien
echo =====================================================================
echo.
echo Bạn có thể nhập database bằng 2 cách:
echo   Cách 1: Nhập tự động qua cửa sổ này (yêu cầu máy có lệnh mysql).
echo   Cách 2: Mở phpMyAdmin / HeidiSQL / Navicat / DBeaver và chọn file "db_ban_linh_kien.sql".
echo.
echo ---------------------------------------------------------------------

set /p db_user=Nhập MySQL Username [Enter để dùng 'root']: 
if "%db_user%"=="" set db_user=root

set /p db_pass=Nhập MySQL Password [Enter nếu không có pass]: 

set /p db_port=Nhập MySQL Port [Enter để dùng 3306]: 
if "%db_port%"=="" set db_port=3306

echo.
echo Đang tiến hành nạp cơ sở dữ liệu vào MySQL...
if "%db_pass%"=="" (
    mysql -u %db_user% -P %db_port% < db_ban_linh_kien.sql
) else (
    mysql -u %db_user% -p%db_pass% -P %db_port% < db_ban_linh_kien.sql
)

if %errorlevel% equ 0 (
    echo.
    echo =====================================================================
    echo [THÀNH CÔNG] Đã import toàn bộ database db_ban_linh_kien vào MySQL!
    echo =====================================================================
) else (
    echo.
    echo [LƯU Ý] Nếu gặp lỗi kết nối hoặc chưa nhận lệnh mysql:
    echo Bạn chỉ cần mở phpMyAdmin (http://localhost/phpmyadmin) hoặc HeidiSQL / Navicat,
    echo tạo database hoặc mở file "db_ban_linh_kien.sql" và nhấn Import/Run là xong!
)
echo.
pause
