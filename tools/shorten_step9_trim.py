"""Bước 9 – Cắt gọn ô bảng dài trên toàn báo cáo + rút gọn văn xuôi Chương 1 / Kết luận."""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt
from report_toolkit import ReportEditor, Paragraph, set_cell_text, drop_columns

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


def cut(text: str, limit: int) -> str:
    t = ' '.join(text.split())
    if len(t) <= limit:
        return text
    s = t[:limit]
    s = s[:s.rfind(' ')]
    return s.rstrip(' .,;:-–') + '…'


def cap(tbl, cols_limits, min_cols=None):
    """Giới hạn độ dài văn bản của các cột được chỉ định."""
    if min_cols and len(tbl.columns) < min_cols:
        return
    for ci, limit in cols_limits.items():
        if ci >= len(tbl.columns):
            continue
        for row in tbl.rows[1:]:
            cell = row.cells[ci]
            txt = cell.text.strip()
            if len(txt) > limit:
                set_cell_text(cell, cut(txt, limit))


# (khớp tiền tố header) -> {chỉ số cột: giới hạn ký tự}
RULES = [
    (['Từ viết tắt'], {2: 55}),
    (['Mã', 'Mục tiêu cụ thể'], {1: 70, 2: 60}),
    (['Nhóm lỗi'], {2: 45, 3: 55}),
    (['Nguyên lý', 'Nội dung'], {1: 60, 2: 70}),
    (['Ràng buộc', 'Nội dung ràng buộc'], {1: 70, 2: 120}),
    (['Mã', 'Tên chuẩn'], {2: 50, 3: 70}),
    (['Tiêu chí so sánh'], {0: 40, 1: 40, 2: 45, 3: 55}),
    (['Chỉ mục', 'Bảng'], {0: 40, 3: 70}),
    (['Thành phần kiến trúc'], {3: 45, 4: 50}),
    (['Tiêu chí', 'Phong Vũ'], {0: 45, 4: 40}),
    (['Mã', 'Tên yêu cầu'], {1: 34, 2: 75}),
    (['Mã', 'Thuộc tính chất lượng'], {1: 38, 2: 75, 3: 80}),
    (['Mã', 'Tên quy tắc'], {1: 32, 2: 85, 3: 60}),
    (['Mã', 'Tác nhân'], {3: 70, 4: 60}),
    (['Mã', 'Tên Use Case'], {1: 32, 3: 70, 4: 110, 5: 24}),
    (['Thành phần', 'Nội dung'], {1: 330}),
    (['Tầng', 'Tên tầng'], {3: 60, 4: 60}),
    (['STT', 'Tên bảng'], {2: 60}),
    (['Tên trường', 'Kiểu dữ liệu'], {4: 48}),
    (['Bảng', 'Trường và kiểu dữ liệu'], {1: 95, 2: 85}),
    (['Tên ràng buộc'], {0: 24, 1: 34, 3: 55}),
    (['Hạng mục quy ước'], {1: 65, 2: 50}),
    (['Loại lỗi', 'Mã HTTP'], {2: 60, 3: 55}),
    (['Nhóm', 'Lớp DTO'], {3: 60}),
    (['STT', 'Màn hình', 'Chức năng client'], {2: 40, 3: 30}),
    (['Trạng thái', 'Định nghĩa'], {1: 45, 2: 90}),
    (['Hạng mục', 'Công cụ'], {0: 24, 1: 45, 3: 60}),
    (['Nhóm', 'Khóa cấu hình'], {1: 38, 2: 40, 3: 55}),
    (['Gói', 'Số lớp tiêu biểu'], {0: 20, 2: 65, 3: 35}),
    (['Method', 'Endpoint', 'Chức năng'], {2: 60, 3: 70}),
    (['Mã RspCode'], {2: 60, 3: 70}),
    (['STT', 'Lớp kiểm thử'], {5: 70}),
    (['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử'], {2: 55, 3: 45}),
    (['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử', 'Quy tắc nghiệp vụ kiểm chứng'], {2: 55, 3: 12}),
    (['Hạng mục', 'Nội dung'], {1: 120}),
    (['Tiêu chí đo'], {0: 30, 3: 45, 4: 40}),
    (['Test ID'], {1: 40, 2: 55}),
    (['Nhóm yêu cầu'], {1: 35, 2: 28, 3: 55}),
    (['Mã', 'Hạn chế'], {1: 34, 2: 55, 3: 55, 4: 55}),
    (['Mã mục tiêu'], {1: 50, 2: 50}),
    (['Ưu tiên', 'Nội dung'], {1: 70, 2: 65}),
    (['Nội dung', 'Mô tả chi tiết'], {1: 90, 2: 60}),
    (['Nội dung', 'Mô tả'], {1: 80, 2: 60}),
    (['Tham số', 'Bắt buộc'], {2: 34, 3: 30}),
]

DEL = [
    'Không thể tái sử dụng cho kênh bán hàng khác',
    'Ngoài ra, hệ thống còn sử dụng bộ chuyển đổi thuộc tính tùy biến',
    'Chú thích @Lock(LockModeType.PESSIMISTIC_WRITE) chỉ thị Hibernate thêm mệnh đề FOR UPDATE vào câu lệnh SELECT',
    'Thứ tự sắp xếp tham số phải tuyệt đối nhất quán',
    'Lý do: String.equals() thoát khỏi vòng lặp ngay tại byte đầu tiên khác biệt',
    'thymeleaf-extras-springsecurity6 cung cấp không gian tên sec:',
    'Ưu điểm: một trong số ít nền tảng trong nước có công cụ xây dựng cấu hình tương đối hoàn chỉnh',
    'Ưu điểm: có công cụ Build PC chọn linh kiện theo từng khe kèm dịch vụ lắp ráp',
    'Hệ thống xử lý cả hai kênh qua cùng một hàm xử lý và đối chiếu chéo',
    'Toàn bộ endpoint đều đạt yêu cầu NFR-10 về thời gian phản hồi',
    'Quy tắc nghiệp vụ được thực thi: BR-01',
    'Quan hệ phụ thuộc ba cấp giữa ba endpoint này',
    'Đặc điểm kiến trúc của nhóm tài nguyên này',
    'Ba hạn chế bổ sung về mặt dữ liệu và kiến trúc',
    'Ba kết luận rút ra từ kết quả',
]

REPL = {
    'Mô hình đối tượng (kế thừa, đa hình, tham chiếu, đồ thị đối tượng)':
        "Mô hình đối tượng (kế thừa, đa hình, tham chiếu) và mô hình quan hệ (bảng, hàng, cột, khóa "
        "ngoại) có bản chất khác nhau; sự lệch pha này gọi là trở kháng đối tượng – quan hệ, và ORM "
        "ra đời để san phẳng nó.",
    'Tiêm qua hàm khởi tạo mang lại bốn lợi ích':
        "Tiêm qua hàm khởi tạo mang lại bốn lợi ích: phụ thuộc là final nên bất biến, đối tượng luôn "
        "hợp lệ ngay sau khi tạo, dễ kiểm thử vì có thể truyền mock, và phát hiện sớm phụ thuộc vòng.",
    'Cơ chế 3 – @EntityGraph giải quyết bài toán N+1':
        "Cơ chế 3 – @EntityGraph giải quyết bài toán N+1: khi tải N sản phẩm rồi truy cập danh mục "
        "và hãng của từng sản phẩm, Hibernate sẽ phát sinh thêm 2N truy vấn; @EntityGraph buộc sinh "
        "một truy vấn LEFT JOIN FETCH duy nhất.",
    'Cơ chế 4 – Specification cho truy vấn động':
        "Cơ chế 4 – Specification cho truy vấn động: lớp ProductSpecification dùng Criteria API dựng "
        "vị từ tại thời điểm chạy, phục vụ bộ lọc kết hợp nhiều tiêu chí trên trang danh mục.",
    'Một phát hiện kỹ thuật đáng lưu ý':
        "Một phát hiện kỹ thuật đáng lưu ý: cơ sở dữ liệu kế thừa từ ứng dụng PHP/Laravel trước đó, "
        "nơi mật khẩu băm với tiền tố $2y$. BCrypt của Spring báo lỗi khi gặp tiền tố này, nên hệ "
        "thống chuẩn hóa $2y$ → $2a$ khi đăng nhập để bảo toàn tài khoản cũ.",
    'Hạn chế thứ nhất – bảo vệ CSRF đang bị vô hiệu hóa':
        "Hạn chế thứ nhất – bảo vệ CSRF đang bị vô hiệu hóa bằng .csrf(csrf -> csrf.disable()), "
        "phục vụ giai đoạn phát triển; đây là rủi ro cần khắc phục trước khi triển khai thật.",
    'Cấu hình kết nối trong application.yml':
        "Cấu hình kết nối trong application.yml khai báo ba nhóm tham số quan trọng: serverTimezone "
        "để dấu thời gian khớp múi giờ Việt Nam, useUnicode/characterEncoding để lưu tiếng Việt "
        "đúng, và chiến lược cập nhật lược đồ cho môi trường phát triển.",
    'Trong hệ thống, Thymeleaf kết xuất khung trang và nội dung ổn định':
        "Trong hệ thống, Thymeleaf kết xuất khung trang và nội dung ổn định, còn nội dung biến động "
        "theo tương tác được nạp bất đồng bộ bằng Fetch API — kết hợp đúng thế mạnh của hai cách "
        "tiếp cận và giữ cho kiến trúc vẫn hướng dịch vụ ở tầng dữ liệu.",
    'Cổng thanh toán trung gian giải quyết bài toán tin cậy giữa ba bên':
        "Cổng thanh toán trung gian giải quyết bài toán tin cậy giữa ba bên: website bán hàng không "
        "tiếp xúc với thông tin thẻ, mà chuyển hướng người dùng sang cổng và nhận kết quả qua hai "
        "kênh — Return URL cho trình duyệt và IPN cho máy chủ.",
    'HMAC cung cấp đồng thời hai bảo đảm':
        "HMAC cung cấp đồng thời hai bảo đảm: tính toàn vẹn — mọi thay đổi dù chỉ một bit đều làm "
        "giá trị băm đổi hoàn toàn; và tính xác thực — chỉ bên nắm khóa bí mật mới tạo được chữ ký "
        "hợp lệ, nên kẻ giả mạo không thể tự sinh tham số thanh toán.",
    'Nguyên tắc thiết kế then chốt':
        "Nguyên tắc thiết kế then chốt: không bao giờ tin Return URL là căn cứ duy nhất để ghi nhận "
        "thanh toán thành công; hệ thống xử lý cả hai kênh qua cùng một hàm và đối chiếu chéo với "
        "cơ sở dữ liệu trước khi cập nhật trạng thái đơn.",
    'Nhận định thứ hai': 
        "Nhận định thứ hai: các nền tảng khảo sát đều có khoảng trống chung về hỗ trợ ra quyết định "
        "kỹ thuật — không nền tảng nào cung cấp đồng thời gợi ý cấu hình thông minh, kiểm tra tương "
        "thích theo thông số kỹ thuật và cam kết toàn vẹn tồn kho khi tranh chấp.",
    'Thứ nhất, chương đã xác lập cơ sở thực tiễn của đề tài':
        "Thứ nhất, chương xác lập cơ sở thực tiễn: bài toán bán lẻ linh kiện máy tính trực tuyến có "
        "bốn đặc thù khiến nó khác biệt căn bản so với thương mại điện tử hàng hóa thông thường.",
    'Thứ hai, chương đã hệ thống hóa cơ sở lý thuyết':
        "Thứ hai, chương hệ thống hóa cơ sở lý thuyết: tám nguyên lý SOA, sáu ràng buộc REST và quy "
        "chuẩn thiết kế tài nguyên, gắn mỗi nội dung lý thuyết với vị trí áp dụng cụ thể trong hệ "
        "thống; thứ ba, chương định vị kiến trúc hệ thống là Service-Oriented Monolith kèm biện luận "
        "đầy đủ và kết quả khảo sát bốn nền tảng cùng ngành.",
    'Thứ ba, chương đã định vị chính xác kiến trúc của hệ thống':
        None,
}


def main():
    e = ReportEditor(DOC)

    for prefix in DEL:
        idx = e.find(prefix)
        if idx:
            e.delete_blocks(idx)
    for prefix, new in REPL.items():
        if new is None:
            continue
        idx = e.find(prefix)
        if idx:
            e.set_para_text(e.blocks[idx[0]], [(new, None, None)])
    e.refresh()

    for b in e.blocks:
        if isinstance(b, Paragraph) or not b.rows:
            continue
        head = [c.text.strip() for c in b.rows[0].cells]
        for sig, rules in RULES:
            if head[:len(sig)] == sig:
                cap(b, rules)
                break
    e.save()
    print("Đã cắt gọn ô bảng và văn xuôi.")


if __name__ == '__main__':
    main()
