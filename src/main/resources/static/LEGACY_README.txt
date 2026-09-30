=======================================================
 THÔNG BÁO: Các file CSS cũ (legacy) — KHÔNG XÓA VỘI
=======================================================

Các file CSS dưới đây là tàn dư từ phiên bản PHP thuần cũ.
Chúng KHÔNG được bất kỳ view Laravel nào liên kết tới (kiểm tra 09/2026).
Giao diện hiện tại sử dụng: public/css/style.css + inline <style> trong layouts.

Danh sách file legacy:
  - banner.css
  - baohanh.css
  - chitietsanpham.css
  - footer.css
  - gioHang.css
  - gioithieu.css
  - header.css
  - home_products.css
  - lienhe.css
  - nguoidung.css
  - pagination_phantrang.css
  - style.min.css      (bản min cũ, style.css đã được cập nhật)
  - taikhoan.css
  - tintuc.css
  - topnav.css
  - trangchu.css
  - tuyendung.css

Hành động đề xuất:
  Trước khi xóa, kiểm tra chắc chắn không còn file .php/.blade nào
  gọi đến các file này qua <link href="..."> hoặc asset().
  Sau khi xác nhận, có thể xóa toàn bộ để giảm kích thước dự án.
