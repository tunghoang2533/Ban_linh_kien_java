"""Bước 3 – Rút gọn mục 1.2 (lý thuyết SOA/REST) của Chương 1."""
import sys
sys.path.insert(0, 'tools')
from copy import deepcopy

from docx.oxml.ns import qn
from docx.table import Table

import report_toolkit as rt

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


def clone_table_after(e, tbl_block, dest_block):
    """Nhân bản một bảng và chèn ngay sau một block khác."""
    new_el = deepcopy(tbl_block._tbl)
    dest_el = dest_block._p if isinstance(dest_block, rt.Paragraph) else dest_block._tbl
    dest_el.addnext(new_el)
    e.refresh()
    return Table(new_el, dest_block._parent)


def insert_rows(tbl, cells_list, at=1, bold_first=False):
    """Chèn các hàng mới vào bảng tại vị trí `at` (0 = trước tiêu đề)."""
    body_tr = tbl.rows[min(1, len(tbl.rows) - 1)]._tr
    for vals in cells_list:
        tr = deepcopy(body_tr)
        tbl.rows[0]._tr.addprevious(tr) if at == 0 else None
        if at != 0:
            # chèn sau hàng cuối rồi di chuyển tới vị trí mong muốn
            tbl._tbl.append(tr)
            target = tbl.rows[at]._tr
            target.addprevious(tr)
            at += 1
        row = None
        for r in tbl.rows:
            if r._tr is tr:
                row = r
                break
        for i, val in enumerate(vals):
            if i >= len(row.cells):
                break
            cell = row.cells[i]
            rt.ReportEditor.set_para_text(
                None, cell.paragraphs[0],
                val if isinstance(val, list) else [(val, bold_first or None, None)])
            for extra in cell.paragraphs[1:]:
                extra._p.getparent().remove(extra._p)


def main():
    e = rt.ReportEditor(DOC)

    # =========================================================== 1.2.1
    # 316 – mở đầu (giữ, gọn hơn)
    e.set_para_text(e.blocks[316], [
        ("Kiến trúc hướng dịch vụ (Service-Oriented Architecture – SOA) là phong cách kiến trúc "
         "phần mềm trong đó chức năng hệ thống được tổ chức thành tập hợp các dịch vụ tự chứa, có "
         "ranh giới rõ ràng, công bố năng lực qua hợp đồng (service contract) và được thiết kế để "
         "bên tiêu thụ sử dụng lại trong nhiều ngữ cảnh khác nhau.", None, None),
    ])
    e.set_para_text(e.blocks[317], [
        ("Thomas Erl hệ thống hóa SOA thành tám nguyên lý thiết kế, trong đó bốn nguyên lý cốt lõi "
         "và bốn nguyên lý bổ trợ được nhóm đối chiếu trực tiếp với hệ thống như bảng dưới đây:",
         None, None),
    ])
    # bảng 337 (4 nguyên lý bổ trợ) → bổ sung thêm 4 nguyên lý cốt lõi lên đầu bảng
    core_rows = [
        ["Ràng buộc lỏng (Loose Coupling)",
         "Dịch vụ và bên tiêu thụ chỉ phụ thuộc vào hợp đồng, không phụ thuộc chi tiết hiện thực.",
         "Áp dụng đầy đủ: client chỉ biết endpoint và lược đồ JSON, không biết dữ liệu giỏ hàng "
         "được lưu ở đâu hay lược đồ bảng ra sao."],
        ["Khả năng tái sử dụng (Reusability)",
         "Một dịch vụ được thiết kế để phục vụ nhiều ngữ cảnh tiêu thụ khác nhau.",
         "Áp dụng đầy đủ: CartService phục vụ cả kênh AJAX lẫn kênh MVC; VnpayService xử lý chung "
         "cho Return URL và IPN Webhook."],
        ["Khả năng liên tác (Interoperability)",
         "Dịch vụ giao tiếp được với bên tiêu thụ chạy trên nền tảng khác nhau.",
         "Áp dụng đầy đủ: HTTP/1.1 và JSON UTF-8; DTO chú thích @JsonProperty theo quy ước "
         "snake_case (cartCount → cart_count)."],
        ["Khả năng kết hợp (Composability)",
         "Các dịch vụ có thể được lắp ghép thành quy trình nghiệp vụ lớn hơn.",
         "Áp dụng đầy đủ: CheckoutService là dịch vụ tổ hợp, điều phối ProductRepository, "
         "ShippingService và VoucherService."],
    ]
    tbl_337 = e.blocks[337]
    insert_rows(tbl_337, core_rows, at=1)
    # xoá các tiểu mục diễn giải chi tiết 318–336, giữ lại bảng 337
    e.delete_blocks(list(range(318, 337)))
    e.refresh()

    # =========================================================== 1.2.2
    e.set_para_text(e.blocks[e.heading_index('1.2.2') + 1], [
        ("REST (Representational State Transfer) là phong cách kiến trúc do Roy Thomas Fielding "
         "trình bày trong luận án tiến sĩ năm 2000. REST không phải giao thức hay tiêu chuẩn mà là "
         "tập hợp các ràng buộc kiến trúc (architectural constraints) mà một hệ phân tán cần thỏa "
         "mãn để đạt được các tính chất mong muốn như khả năng mở rộng, tính đơn giản và khả năng "
         "tiến hóa độc lập giữa các thành phần.", None, None),
    ])
    intro_idx = e.heading_index('1.2.2') + 2
    e.set_para_text(e.blocks[intro_idx], [
        ("Sáu ràng buộc kiến trúc và mức độ tuân thủ thực tế của hệ thống được tổng hợp trong "
         "bảng sau:", None, None),
    ])
    # nhân bản bảng 347 (7 hàng × 3 cột) làm bảng tổng hợp mới, chèn sau đoạn dẫn
    compliance = [
        ["Ràng buộc", "Nội dung ràng buộc", "Mức độ tuân thủ của hệ thống"],
        ["Client – Server", "Tách bạch giao diện người dùng với lưu trữ và xử lý dữ liệu; hai bên "
                            "tiến hóa độc lập miễn là hợp đồng không đổi.",
         "Tuân thủ đầy đủ: backend Spring Boot (cổng 8088) công bố tài nguyên dưới /api/**; client "
         "Thymeleaf tiêu thụ qua Fetch API."],
        ["Stateless", "Mỗi yêu cầu từ client phải chứa đủ thông tin để máy chủ xử lý; máy chủ "
                      "không lưu ngữ cảnh phiên giữa các yêu cầu.",
         "Tuân thủ có chọn lọc: hầu hết endpoint phi trạng thái hoàn toàn; giỏ hàng lưu trong phiên "
         "là đánh đổi có chủ đích (biện luận bên dưới)."],
        ["Cacheable", "Phản hồi phải tự mô tả khả năng lưu đệm để client hoặc tầng trung gian tái "
                      "sử dụng, giảm tải máy chủ.",
         "Tuân thủ một phần: tài nguyên tĩnh và dữ liệu tham chiếu (tỉnh/thành) được lưu đệm; dữ "
         "liệu động phải tính mới nên không đệm."],
        ["Uniform Interface", "Giao diện thống nhất: định danh tài nguyên bằng URI, thao tác qua "
                              "biểu diễn, thông điệp tự mô tả, siêu liên kết điều khiển trạng thái.",
         "Đạt mức 1 và mức 2 của mô hình Richardson (tài nguyên + động từ HTTP, mã trạng thái đúng "
         "ngữ nghĩa); chưa đạt mức 3 (HATEOAS)."],
        ["Layered System", "Client không cần biết đang kết nối trực tiếp tới máy chủ đích hay qua "
                           "các tầng trung gian (proxy, cân bằng tải, gateway).",
         "Tuân thủ: hệ thống chạy đúng sau reverse proxy (xử lý X-Forwarded-For) và backend phân "
         "tầng nghiêm ngặt Controller → Service → Repository."],
        ["Code on Demand (tùy chọn)", "Máy chủ có thể mở rộng chức năng client bằng cách gửi mã "
                                      "thực thi.",
         "Áp dụng ở dạng cổ điển: JavaScript trong tài liệu HTML do Thymeleaf kết xuất thực hiện "
         "lời gọi Fetch và cập nhật DOM cục bộ."],
    ]
    src_tbl = None
    for b in e.blocks:
        if not isinstance(b, rt.Paragraph) and b.rows and \
                b.rows[0].cells[0].text.strip() == 'Nhóm tài nguyên':
            src_tbl = b
            break
    new_tbl = clone_table_after(e, src_tbl, e.blocks[intro_idx])
    # bảng mới thừa một hàng (7 hàng có sẵn) → xoá bớt hàng cuối nếu cần
    while len(new_tbl.rows) > len(compliance):
        new_tbl._tbl.remove(new_tbl.rows[-1]._tr)
    for r_idx, vals in enumerate(compliance):
        row = new_tbl.rows[r_idx]
        for c_idx, val in enumerate(vals):
            if c_idx >= len(row.cells):
                continue
            rt.ReportEditor.set_para_text(None, row.cells[c_idx].paragraphs[0], val)
            for extra in row.cells[c_idx].paragraphs[1:]:
                extra._p.getparent().remove(extra._p)
    # hai đoạn biện luận ngắn sau bảng
    last_cell_para = new_tbl.rows[-1].cells[-1].paragraphs[0]
    e.new_paragraph('Normal', [
        ("Hai điểm cần biện luận. ", True, None),
        ("Thứ nhất, việc lưu giỏ hàng trong phiên máy chủ là đánh đổi có chủ đích giữa tính thuần "
         "khiết kiến trúc và trải nghiệm người dùng: nếu tuân thủ tuyệt đối ràng buộc phi trạng "
         "thái, client phải gửi toàn bộ nội dung giỏ hàng trong mỗi yêu cầu, làm tăng kích thước "
         "thông điệp và độ phức tạp đồng bộ. Thứ hai, về mô hình trưởng thành Richardson, hệ thống "
         "đạt mức 1 và mức 2; mức 3 (Hypermedia Controls – HATEOAS) chưa được hiện thực vì với quy "
         "mô một client duy nhất, chi phí hiện thực lớn hơn lợi ích thu được — đây là hạn chế được "
         "nhóm chủ động thừa nhận.", None, None),
    ], last_cell_para)
    e.refresh()
    # xoá toàn bộ phần diễn giải chi tiết 1.2.2 (từ H4 a) tới hết f)
    start = e.heading_index('a) Client – Server')
    end = e.find('Áp dụng ở dạng cổ điển. Máy chủ gửi kèm mã JavaScript')[0]
    e.delete_range(start, end)
    e.refresh()

    # =========================================================== 1.2.3
    # giữ bảng quy tắc đặt tên (371), rút gọn hai ghi chú dài
    e.set_para_text(e.blocks[e.find('Cần lưu ý một quyết định thiết kế có chủ đích')[0]], [
        ("Ghi chú thiết kế: một số endpoint thao tác dùng động từ ở đoạn cuối URI (/api/cart/add, "
         "/api/build-pc/select, /api/voucher/apply). Cách đặt tên này chấp nhận đánh đổi tính thuần "
         "REST để đổi lấy sự rõ ràng cho hành động nghiệp vụ không ánh xạ trực tiếp sang thao tác "
         "CRUD trên một tài nguyên duy nhất.", None, None),
    ])
    e.set_para_text(e.blocks[e.find('Lưu ý kỹ thuật về webhook IPN')[0]], [
        ("Lưu ý về webhook IPN: đặc tả VNPAY quy định máy chủ bán hàng nhận thông báo IPN qua "
         "phương thức GET với dữ liệu trong query string, mâu thuẫn với nguyên tắc “GET phải an "
         "toàn” của REST. Hệ thống chấp nhận ngoại lệ này vì endpoint không trả về biểu diễn tài "
         "nguyên cho người dùng mà chỉ ghi nhận kết quả giao dịch, đồng thời được bảo vệ bằng xác "
         "thực chữ ký HMAC-SHA512.", None, None),
    ])
    # gộp hai bullet về quy ước lỗi thành một đoạn
    b0 = e.find('Lỗi giao thức / lỗi đầu vào')[0]
    b0_end = b0 + 1
    e.set_para_text(e.blocks[b0], [
        ("Quy ước phản hồi lỗi: lỗi giao thức hoặc lỗi đầu vào (thiếu tham số, số lượng âm, sản "
         "phẩm không tồn tại) trả mã HTTP tương ứng (400, 404) kèm JSON có success: false; kết quả "
         "nghiệp vụ không thuận lợi (mã giảm giá hết lượt, đơn chưa đạt giá trị tối thiểu) trả 200 "
         "OK kèm success: false và thông điệp giải thích, vì về mặt giao thức yêu cầu đã được xử lý "
         "thành công.", None, None),
    ])
    e.delete_blocks([b0_end])
    e.refresh()

    # =========================================================== 1.2.4
    e.set_para_text(e.blocks[e.find('Phân tích sơ đồ: Sự khác biệt bản chất')[0]], [
        ("Phân tích sơ đồ: khác biệt bản chất nằm ở vị trí ranh giới hợp đồng. Trong mô hình "
         "nguyên khối, ranh giới duy nhất là biểu mẫu HTML — không có lược đồ hình thức và không "
         "thể kiểm thử tự động ở mức giao diện. Trong kiến trúc hướng dịch vụ, ranh giới là hợp "
         "đồng REST có lược đồ JSON tường minh, nhờ đó mọi kênh tiêu thụ đều được kiểm thử và tiến "
         "hóa độc lập với phần hiện thực.", None, None),
    ])

    # =========================================================== 1.2.5
    e.set_para_text(e.blocks[e.find('Một sai lầm thường gặp')[0]], [
        ("Một sai lầm thường gặp trong các báo cáo đồ án là tự nhận hệ thống Spring Boot là "
         "“microservices”. Nhóm chủ động định vị kiến trúc một cách chính xác để tránh sai lầm "
         "này.", None, None),
    ])
    e.set_para_text(e.blocks[e.find('Biện luận định vị:')[0]], [
        ("Biện luận định vị: hệ thống là một Service-Oriented Monolith — ứng dụng chạy trong một "
         "tiến trình JVM duy nhất nhưng được tổ chức nội bộ theo nguyên lý hướng dịch vụ và công "
         "bố năng lực nghiệp vụ ra bên ngoài dưới dạng REST API. Hai bảng dưới đây đối chiếu hệ "
         "thống với các đặc trưng của microservices và của SOA:", None, None),
    ])
    e.set_para_text(e.blocks[e.find('Việc lựa chọn Service-Oriented Monolith')[0]], [
        ("Việc lựa chọn Service-Oriented Monolith thay vì microservices là quyết định kiến trúc có "
         "cơ sở: với quy mô nghiệp vụ hiện tại và yêu cầu nhất quán mạnh trong chuỗi giao dịch trừ "
         "kho – đặt hàng – ghi nhận thanh toán, một cơ sở dữ liệu dùng chung và giao dịch cục bộ "
         "giúp giảm mạnh độ phức tạp vận hành mà vẫn giữ được ranh giới dịch vụ rõ ràng.", None, None),
    ])

    e.save()
    print("Đã rút gọn mục 1.2.")


if __name__ == '__main__':
    main()
