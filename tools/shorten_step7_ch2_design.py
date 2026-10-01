"""Bước 7 – Rút gọn mục 2.3, 2.4.1, 2.4.3, 2.4.4 và 2.5 của Chương 2."""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt
from report_toolkit import (ReportEditor, Paragraph, set_cell_text, drop_columns,
                            set_col_fractions, fit_table_width)

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'

DEL = [
    # 2.3.1
    'Phân tích luồng xử lý dữ liệu:',
    'Từ định danh sang bản ghi người dùng',
    'Từ bản ghi người dùng sang tập quyền hạn',
    'Từ mật khẩu thô sang kết quả so khớp',
    'Từ kết quả xác thực sang ngữ cảnh phiên',
    # 2.3.2
    'Thứ hai – Chuẩn hóa chuỗi trước khi so sánh',
    'Thứ ba – Chiến lược thành công một phần khi thêm hàng loạt',
    # 2.3.3
    'Về ý nghĩa của bước 1 (gộp số lượng)',
    'Về ý nghĩa của bước 5 (tính lại số tiền)',
    'Về ảnh chụp dữ liệu tại bước 7',
    # 2.3.4
    'Về tính bất biến. Vì cả hai kênh',
    # 2.3.5
    'Về việc tái sử dụng khóa bi quan trong luồng hoàn kho',
    'Về xử lý dữ liệu không nhất quán',
    # 2.4.1
    'Hai phương thức nghiệp vụ trên lớp Product đáng được chú ý',
    'Hai phương thức này hiện thực quy tắc BR-01',
    # 2.4.3
    'Nguyên tắc phân biệt hai loại lỗi đã được trình bày tại mục 1.2.3',
    'Ba lợi ích cụ thể của việc dùng DTO',
    'Ổn định hợp đồng khi mô hình nội bộ thay đổi',
    'Tránh lỗi tuần tự hóa do nạp lười',
    # 2.4.4
    'Mẫu mã nguồn tiêu biểu cho việc tiêu thụ dịch vụ phía client:',
    'Khối finally là chi tiết quan trọng',
    'Mô hình lai này cân bằng được ba yêu cầu',
]

# Các bước mã nguồn của hai khối code lớn cần xoá
CODE_DROP = [
    'if (discountPercent == null || discountPercent <= 0) return false;',
    'LocalDateTime now = LocalDateTime.now();',
    'if (saleStart != null && now.isBefore(saleStart)) return false;',
    'if (saleEnd != null && now.isAfter(saleEnd)) return false;',
    'return true;',
    'public BigDecimal getFinalPrice() {',
    'if (!hasDiscount()) return price;',
    'BigDecimal multiplier = BigDecimal.ONE.subtract(',
    'BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100)));',
    'return price.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);',
    'const btn = document.querySelector(`[data-add="${productId}"]`);',
    'btn.disabled = true;                              // trạng thái đang tải',
    "btn.innerHTML = '<span class=\"spinner-border spinner-border-sm\"></span>';",
    'try {',
    'method: \'POST\',',
    "headers: { 'Content-Type': 'application/json' },",
    'body: JSON.stringify({ product_id: productId, quantity: quantity })',
    '});',
    'const data = await res.json();',
    'if (data.success) {                           // trạng thái thành công',
    "document.getElementById('cart-badge').textContent = data.cart_count;",
    "showToast('success', data.message);",
    '} else {                                      // lỗi nghiệp vụ',
    "showToast('warning', data.message);",
    '}',
    '} catch (e) {                                     // lỗi kết nối',
    "showToast('danger', 'Không thể kết nối tới máy chủ, vui lòng thử lại');",
    '} finally {',
    'btn.disabled = false;                         // luôn khôi phục nút',
    "btn.innerHTML = 'Thêm vào giỏ';",
]


def main():
    e = ReportEditor(DOC)

    for prefix in DEL:
        idx = e.find(prefix)
        if idx:
            e.delete_blocks(idx)
    for line in CODE_DROP:
        idx = e.find(line, exact=True)
        if idx:
            e.delete_blocks(idx)

    # ---------------- thay thế các đoạn phân tích dài bằng bản ngắn gọn
    REPL = {
        'Luồng đăng nhập thể hiện rõ nguyên lý phân tách trách nhiệm':
            "Luồng đăng nhập thể hiện rõ nguyên lý phân tách trách nhiệm của Spring Security: dữ "
            "liệu HTTP được chuyển thành đối tượng xác thực, truy vấn bản ghi người dùng, dựng tập "
            "quyền hạn, so khớp mật khẩu bằng BCrypt rồi lưu ngữ cảnh bảo mật vào phiên.",
        'Từ tham số HTTP sang đối tượng xác thực':
            "Từ tham số HTTP sang đối tượng xác thực. UsernamePasswordAuthenticationFilter chuyển "
            "hai chuỗi văn bản thành đối tượng Authentication chưa xác thực và chuyển cho "
            "AuthenticationManager.",
        'Điểm kiểm soát an toàn: Cả ba nhánh thất bại':
            "Điểm kiểm soát an toàn: cả ba nhánh thất bại (không tìm thấy tài khoản, tài khoản bị "
            "khóa, mật khẩu sai) đều dẫn tới cùng một thông báo chung, tránh tiết lộ định danh nào "
            "sai và chống dò tài khoản.",
        'Sơ đồ này minh họa rõ nhất đặc trưng nghiệp vụ của đề tài':
            "Sơ đồ này minh họa rõ nhất đặc trưng nghiệp vụ của đề tài với hai điểm thiết kế đáng "
            "chú ý: lọc tương thích hai lớp và chiến lược thành công một phần khi thêm hàng loạt.",
        'Thứ nhất – Lọc hai lớp cho ràng buộc tương thích':
            "Thứ nhất – Lọc hai lớp cho ràng buộc tương thích. Hệ thống kiểm tra tương thích ngay "
            "khi người dùng chọn linh kiện (lọc danh sách hiển thị theo socket của CPU hoặc bo mạch "
            "chủ đã chọn) và kiểm tra lại lần cuối trước khi ghi vào phiên, nhờ đó cấu hình lưu trữ "
            "luôn hợp lệ kể cả khi dữ liệu thông số bị thay đổi giữa hai thời điểm.",
        'Về ranh giới giao dịch. Chú thích @Transactional':
            "Về ranh giới giao dịch: @Transactional đặt tại placeOrder() bao trọn cả chín bước "
            "nghiệp vụ, nên nếu bất kỳ bước nào thất bại thì toàn bộ thao tác trừ kho và ghi đơn "
            "đều bị hoàn tác, không để lại trạng thái trung gian.",
        'Về ý nghĩa của bước 2 (sắp xếp thứ tự khóa)':
            "Về ý nghĩa của bước 2 (sắp xếp thứ tự khóa): các bản ghi sản phẩm luôn bị khóa theo thứ "
            "tự định danh tăng dần. Quy tắc này loại trừ hoàn toàn bế tắc giữa hai giao dịch khóa "
            "chéo nhau — kỹ thuật phòng chống deadlock kinh điển của hệ quản trị cơ sở dữ liệu.",
        'Về sự cần thiết của hai kênh':
            "Về sự cần thiết của hai kênh: Return URL phụ thuộc hoàn toàn vào trình duyệt của khách "
            "(khách có thể đóng tab, mất mạng hoặc chuyển hướng bị chặn), trong khi IPN là kênh máy "
            "chủ tới máy chủ có cơ chế thử lại nên trở thành nguồn chân lý cho trạng thái thanh "
            "toán. Hệ thống không bao giờ ghi nhận thanh toán chỉ dựa vào Return URL.",
        'Về ba lớp phòng vệ':
            "Về ba lớp phòng vệ: endpoint IPN được bảo vệ bằng ba lớp kiểm tra tuần tự — xác minh "
            "chữ ký HMAC-SHA512, đối chiếu số tiền với tổng tiền đơn hàng, và kiểm tra tính bất "
            "biến trạng thái (trả mã 02 cho lời gọi lặp).",
        'Về tính nguyên tử của cặp thao tác hủy đơn – hoàn kho':
            "Về tính nguyên tử của cặp thao tác hủy đơn – hoàn kho: hai thao tác nằm trong cùng một "
            "giao dịch, nếu hoàn kho thất bại thì việc hủy đơn cũng không được ghi nhận, bảo đảm "
            "tồn kho và trạng thái đơn luôn nhất quán.",
        'Về khả năng kiểm toán':
            "Về khả năng kiểm toán: mỗi lần chuyển trạng thái đều sinh một bản ghi "
            "order_status_history ghi rõ trạng thái trước, trạng thái sau, người thực hiện và ghi "
            "chú, đáp ứng yêu cầu truy vết trách nhiệm.",
        'Đây là mục trọng tâm của học phần':
            "Đây là mục trọng tâm của học phần Phát triển phần mềm hướng dịch vụ: hợp đồng dịch vụ "
            "được đặc tả tường minh theo quy ước thiết kế, cấu trúc phản hồi chuẩn, cấu trúc phản "
            "hồi lỗi và các đối tượng truyền dữ liệu (DTO) công bố ra bên ngoài.",
        'Cấu trúc này gồm hai phần':
            "Cấu trúc phản hồi gồm hai phần: phần khung bắt buộc (success, message) mô tả kết quả ở "
            "mức ngữ nghĩa nghiệp vụ, và phần dữ liệu tùy theo endpoint (ví dụ cart_count, subtotal, "
            "item). Client chỉ cần kiểm tra trường success để quyết định nhánh xử lý.",
        'Nguyên tắc phân biệt hai loại lỗi':
            "Các lỗi được phân biệt nhất quán: lỗi giao thức hoặc đầu vào trả 400/404 kèm "
            "success: false; kết quả nghiệp vụ không thuận lợi vẫn trả 200 OK kèm success: false và "
            "thông điệp giải thích để client hiển thị cho người dùng.",
        'Ngăn rò rỉ dữ liệu nhạy cảm. Entity Product chứa trường cost_price':
            "DTO ngăn rò rỉ dữ liệu nhạy cảm: entity Product chứa trường cost_price (giá vốn) — nếu "
            "tuần tự hóa entity trực tiếp, thông tin kinh doanh mật này sẽ lộ ra ngoài hợp đồng API.",
        'Ứng dụng client gồm 44 tệp khuôn mẫu Thymeleaf':
            "Ứng dụng client gồm 44 tệp khuôn mẫu Thymeleaf (9.294 dòng mã) với hai bố cục gốc: "
            "layout/storefront.html cho giao diện khách hàng và layout/admin.html cho bảng điều "
            "khiển quản trị; các màn hình chính được liệt kê trong bảng dưới đây.",
        'Một ứng dụng client tiêu thụ dịch vụ phải xử lý đầy đủ năm trạng thái':
            "Mỗi lời gọi API phía client phải xử lý đầy đủ năm trạng thái: đang tải, thành công, "
            "lỗi nghiệp vụ, lỗi kết nối và trạng thái khôi phục. Hệ thống hiện thực chiến lược sau:",
        'Hệ thống áp dụng mô hình lai với tiêu chí phân công rõ ràng':
            "Hệ thống áp dụng mô hình lai: nội dung ổn định (khung trang, danh sách, chi tiết sản "
            "phẩm) kết xuất phía máy chủ để tối ưu tốc độ hiển thị lần đầu và SEO; nội dung biến "
            "động theo tương tác được cập nhật bằng Fetch API.",
    }
    for prefix, new in REPL.items():
        idx = e.find(prefix)
        if idx:
            e.set_para_text(e.blocks[idx[0]], [(new, None, None)])
    e.refresh()

    # ---------------- bảng 2.39 – danh sách màn hình client
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        head = [c.text.strip() for c in b.rows[0].cells] if b.rows else []
        if head[:3] == ['STT', 'Màn hình', 'Tệp khuôn mẫu']:
            drop_columns(b, [6, 5, 2])
            set_col_fractions(b, [0.06, 0.26, 0.40, 0.28])
            for row in b.rows[1:]:
                cell = row.cells[2]
                txt = cell.text.strip()
                if len(txt) > 105:
                    cut = txt[:105]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')
        # bảng quy ước thiết kế API
        if head[:1] == ['Hạng mục quy ước']:
            for row in b.rows[1:]:
                cell = row.cells[1]
                txt = cell.text.strip()
                if len(txt) > 110:
                    cut = txt[:110]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')
        # bảng cấu trúc phản hồi lỗi
        if head[:1] == ['Loại lỗi']:
            drop_columns(b, [4])
            set_col_fractions(b, [0.22, 0.10, 0.30, 0.38])
        # bảng DTO
        if head[:1] == ['Nhóm']:
            set_col_fractions(b, [0.12, 0.24, 0.10, 0.54])
            for row in b.rows[1:]:
                cell = row.cells[-1]
                txt = cell.text.strip()
                if len(txt) > 95:
                    cut = txt[:95]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')
        # bảng trách nhiệm từng tầng
        if head[:2] == ['Tầng', 'Tên tầng']:
            set_col_fractions(b, [0.05, 0.17, 0.22, 0.28, 0.28])
            for row in b.rows[1:]:
                for ci in (3, 4):
                    cell = row.cells[ci]
                    txt = cell.text.strip()
                    if len(txt) > 90:
                        cut = txt[:90]
                        cut = cut[:cut.rfind(' ')]
                        set_cell_text(cell, cut.rstrip('.,;') + '…')
        # bảng chiến lược trạng thái client
        if head[:1] == ['Trạng thái']:
            set_col_fractions(b, [0.20, 0.34, 0.46])
        # bảng phân công trách nhiệm SSR/API
        if head[:1] == ['Loại nội dung']:
            set_col_fractions(b, [0.34, 0.28, 0.38])

    # ---------------- 2.5 kết luận chương 2
    for i, b in enumerate(e.blocks):
        if isinstance(b, Paragraph) and b.text.strip().startswith('Chương 2 đã hoàn thành'):
            e.set_para_text(b, [("Chương 2 đã hoàn thành các nhiệm vụ phân tích và thiết kế:", None, None)])
    e.save()
    print("Đã rút gọn 2.3 – 2.5.")


if __name__ == '__main__':
    main()
