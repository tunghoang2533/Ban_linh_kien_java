"""Bước 5 – Rút gọn mục 2.1 (yêu cầu) và 2.2 (đặc tả Use Case) của Chương 2."""
import sys
sys.path.insert(0, 'tools')
from copy import deepcopy

import report_toolkit as rt
from report_toolkit import (ReportEditor, set_cell_text, drop_columns, resize_columns,
                            trim_steps, edit_caption, Paragraph)

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


UC_SUMMARY = [
    ["Mã", "Tên Use Case", "Tác nhân chính", "Mục tiêu tóm tắt",
     "Luồng chính (rút gọn)", "Quy tắc/NFR liên quan"],
    ["UC01", "Đăng ký tài khoản", "Khách vãng lai",
     "Tạo tài khoản mới để dùng các chức năng dành cho khách hàng đã xác thực.",
     "1. GET /register kết xuất biểu mẫu; 2. Gửi POST /register; 3. Kiểm chứng dữ liệu, kiểm tra "
     "trùng tên đăng nhập và email; 4. Băm mật khẩu bằng BCrypt, tạo bản ghi users; 5. Chuyển "
     "hướng /login?registered=true.",
     "NFR-03"],
    ["UC02", "Đăng nhập và phân quyền theo vai trò", "Khách hàng, Quản trị viên",
     "Xác thực bằng tên đăng nhập hoặc email, gán quyền hạn và điều hướng theo vai trò.",
     "1. POST /login → UsernamePasswordAuthenticationFilter; 2. DaoAuthenticationProvider gọi "
     "CustomUserDetailsService; 3. Kiểm tra cờ is_blocked và dựng quyền ROLE_*; 4. So khớp BCrypt, "
     "lưu SecurityContext vào phiên; 5. Điều hướng /admin hoặc trang chủ.",
     "BR-28, NFR-03, NFR-04"],
    ["UC03", "Tìm kiếm và lọc sản phẩm nâng cao", "Khách vãng lai, Khách hàng",
     "Thu hẹp danh sách linh kiện theo từ khóa và danh mục, kết quả có phân trang.",
     "1. GET /san-pham với search, category_id, page, size; 2. Bộ điều khiển chọn nhánh truy vấn "
     "phù hợp; 3. Repository trả trang kết quả kèm tổng số bản ghi; 4. Kết xuất danh sách và thanh "
     "phân trang (trạng thái rỗng nếu không có kết quả).",
     "BR-01, BR-02, NFR-08, NFR-09"],
    ["UC04", "Ráp cấu hình PC và kiểm tra tương thích", "Khách vãng lai, Khách hàng",
     "Chọn linh kiện cho bảy khe, kiểm tra tương thích socket hai chiều, tính tổng tiền và tổng "
     "công suất.",
     "1. GET /build-pc kết xuất bảy khe; 2. Chọn khe → GET /build-pc/components; 3. Lọc linh kiện "
     "tương thích và tự gỡ linh kiện xung đột; 4. Chọn linh kiện, cập nhật tổng tiền và tổng công "
     "suất; 5. (tùy chọn) nhận gợi ý cấu hình theo ngân sách.",
     "BR-07, BR-08, FR-G11"],
    ["UC05", "Quản lý giỏ hàng", "Khách vãng lai, Khách hàng",
     "Thêm, sửa số lượng, xóa mục hoặc xóa toàn bộ giỏ hàng qua REST API bất đồng bộ.",
     "1. JavaScript gửi POST /api/cart/add; 2. Kiểm tra quantity > 0 và trạng thái kinh doanh của "
     "sản phẩm; 3. Kiểm tra số lượng dự kiến so với tồn kho; 4. Cập nhật giỏ hàng trong phiên; "
     "5. Trả JSON kèm cart_count và tạm tính.",
     "BR-03, BR-30, FR-G08"],
    ["UC06", "Đặt hàng và áp dụng mã giảm giá", "Khách vãng lai, Khách hàng",
     "Hoàn tất đơn hàng trong một giao dịch nguyên tử có khóa bi quan trên bản ghi tồn kho.",
     "Đặc tả chi tiết đầy đủ trình bày tại Bảng 2.8.",
     "BR-01 – BR-06, BR-09 – BR-17"],
    ["UC07", "Thanh toán trực tuyến qua VNPAY", "Khách hàng",
     "Sinh URL thanh toán đã ký số và nhận kết quả qua hai kênh Return URL và IPN Webhook.",
     "Đặc tả chi tiết đầy đủ trình bày tại Bảng 2.9.",
     "BR-23 – BR-27, NFR-06, NFR-07"],
    ["UC08", "Quản lý đơn hàng và cập nhật trạng thái", "Quản trị viên",
     "Xem danh sách, chi tiết đơn hàng và điều khiển tiến trình xử lý theo máy trạng thái.",
     "1. GET /admin/orders có lọc theo trạng thái; 2. Xem chi tiết đơn; 3. Chọn trạng thái mới, "
     "kiểm tra tính hợp lệ của phép chuyển; 4. Ghi bản ghi order_status_history; 5. Nếu hủy đơn: "
     "hoàn kho và ghi nhật ký trong cùng một giao dịch.",
     "BR-06, BR-18 – BR-23"],
    ["UC09", "Quản lý tồn kho và nhật ký biến động", "Quản trị viên",
     "Theo dõi tồn kho, cảnh báo sắp hết hàng, điều chỉnh kho và tra cứu lịch sử phục vụ kiểm toán.",
     "1. Bảng điều khiển hiển thị sản phẩm có quantity ≤ min_stock; 2. Trang quản lý tồn kho liệt "
     "kê tồn kho và trạng thái cảnh báo; 3. Điều chỉnh số lượng kèm lý do trong giao dịch có khóa "
     "bản ghi; 4. Ghi bản ghi warehouse_logs tương ứng.",
     "BR-20, BR-22, NFR-16"],
    ["UC10", "Gửi đánh giá và bình luận sản phẩm", "Khách hàng (kiểm duyệt: Quản trị viên)",
     "Gửi nhận xét kèm điểm đánh giá 1–5 sao; bình luận hiển thị công khai và có thể bị ẩn.",
     "1. GET /san-pham/{id} hiển thị bình luận và biểu mẫu; 2. Gửi POST /san-pham/{id}/comment; "
     "3. Kiểm tra trạng thái xác thực, nội dung và điểm đánh giá; 4. Lưu bình luận, tính lại điểm "
     "trung bình của sản phẩm.",
     "FR-C10, FR-A12"],
]


def main():
    e = ReportEditor(DOC)

    # ================================================= 2.1.1 – bảng yêu cầu chức năng
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        head = b.rows[0].cells[0].text.strip() if b.rows else ''
        if head == 'Mã' and b.rows[0].cells[1].text.strip() == 'Tên yêu cầu':
            drop_columns(b, [6, 4, 3])   # bỏ Trạng thái, Đầu ra, Đầu vào

    # ================================================= 2.1.2 – bảng yêu cầu phi chức năng
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        head = b.rows[0].cells[0].text.strip() if b.rows else ''
        if head == 'Mã' and b.rows[0].cells[1].text.strip() == 'Thuộc tính chất lượng':
            drop_columns(b, [4])         # bỏ cột Trạng thái
            for row in b.rows[1:]:
                cell = row.cells[2]
                txt = cell.text.strip()
                if len(txt) > 150:       # rút gọn giải pháp kỹ thuật
                    parts = [p.strip() for p in txt.split(';')]
                    set_cell_text(cell, '; '.join(parts[:2]) + '.')

    # ================================================= 2.1.3 – bảng quy tắc nghiệp vụ
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        head = b.rows[0].cells[0].text.strip() if b.rows else ''
        if head == 'Mã' and b.rows[0].cells[1].text.strip() == 'Tên quy tắc':
            drop_columns(b, [4])         # bỏ cột Hành vi khi vi phạm
            for row in b.rows[1:]:
                cell = row.cells[2]
                txt = cell.text.strip()
                if len(txt) > 190:
                    cut = txt[:190]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')

    # ================================================= 2.2.3 – đặc tả Use Case
    # 1. nhân bản một caption (giữ trường SEQ) và một bảng 2 cột làm mẫu
    cap_src = None
    for b in e.blocks:
        if isinstance(b, Paragraph) and b.style.name == 'Caption' and 'Đặc tả Use Case UC01' in b.text:
            cap_src = b
            break
    uc_tbl_src = None
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        if b.rows and b.rows[0].cells[0].text.strip() == 'Thành phần':
            uc_tbl_src = b
            break
    cap_clone = deepcopy(cap_src._p)
    tbl_clone = deepcopy(uc_tbl_src._tbl)

    # 2. xoá UC01–UC05 (từ tiêu đề a) tới ngay trước tiêu đề f) UC06)
    start = e.heading_index('a) UC01')
    end = e.find('f) UC06')[0]
    e.delete_range(start, end - 1)
    e.refresh()

    # 3. chèn bảng tổng hợp + caption ngay trước tiêu đề f) UC06
    anchor = e.blocks[e.find('f) UC06')[0]]
    #   a) caption
    new_cap = deepcopy(cap_clone)
    anchor._p.addprevious(new_cap)
    e.refresh()
    cap_par = Paragraph(new_cap, anchor._parent)
    edit_caption(cap_par, ' – Tổng hợp đặc tả các Use Case trọng yếu (UC01 – UC10)')
    #   b) bảng tổng hợp 6 cột
    new_tbl = deepcopy(tbl_clone)
    cap_par._p.addnext(new_tbl)
    e.refresh()
    from docx.table import Table
    summary = Table(new_tbl, anchor._parent)
    # dựng đủ 11 hàng × 6 cột
    body_tr = deepcopy(summary.rows[1]._tr)
    while len(summary.rows) > 1:
        summary._tbl.remove(summary.rows[-1]._tr)
    for _ in range(len(UC_SUMMARY)):
        summary._tbl.append(deepcopy(body_tr))
    resize_columns(summary, 6)
    for r_idx, vals in enumerate(UC_SUMMARY):
        row = summary.rows[r_idx]
        for c_idx, val in enumerate(vals):
            set_cell_text(row.cells[c_idx], val)

    # 4. cập nhật đoạn dẫn và rút gọn hai đặc tả chi tiết
    e.set_para_text(e.blocks[e.find('Mười ca sử dụng trọng yếu')[0]], [
        ("Mười ca sử dụng trọng yếu được đặc tả theo mẫu chuẩn gồm mười ba trường. Bảng 2.7 tổng "
         "hợp các trường cốt lõi của cả mười ca sử dụng; hai ca sử dụng có độ phức tạp cao nhất — "
         "UC06 (đặt hàng có khóa bi quan và áp mã giảm giá) và UC07 (thanh toán VNPAY với hai kênh "
         "phản hồi) — được đặc tả chi tiết đầy đủ tại Bảng 2.8 và Bảng 2.9.", None, None),
    ])

    keep_rows = ['Thành phần', 'Mã Use Case', 'Tên Use Case', 'Tác nhân chính', 'Mô tả tóm tắt',
                 'Tiền điều kiện', 'Luồng sự kiện chính', 'Luồng thay thế', 'Luồng ngoại lệ',
                 'Hậu điều kiện thành công', 'Điểm cuối kỹ thuật']
    limits = {'Luồng sự kiện chính': 6, 'Luồng thay thế': 2, 'Luồng ngoại lệ': 3,
              'Hậu điều kiện thành công': 3, 'Tiền điều kiện': 3}
    for b in list(e.blocks):
        if isinstance(b, Paragraph):
            continue
        if b.rows and b.rows[0].cells[0].text.strip() == 'Thành phần':
            for row in list(b.rows):
                key = row.cells[0].text.strip()
                if key not in keep_rows:
                    row._tr.getparent().remove(row._tr)
                    continue
                if key in limits:
                    set_cell_text(row.cells[1], trim_steps(row.cells[1].text.strip(), limits[key]))

    # 5. xoá UC08–UC10
    e.refresh()
    start = e.heading_index('h) UC08')
    end = e.heading_index('2.3. Thiết kế sơ đồ tuần tự')
    e.delete_range(start, end - 1)

    e.save()
    print("Đã rút gọn 2.1 và 2.2.")


if __name__ == '__main__':
    main()
