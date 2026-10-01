"""Bước 1 – Rút gọn phần đầu báo cáo (lời cảm ơn, lời cam đoan, danh mục thuật ngữ).

Chạy: python tools/shorten_step1_frontmatter.py
"""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


def main():
    e = rt.ReportEditor(DOC)

    # ---------------------------------------------------------------- LỜI CẢM ƠN
    p = e.blocks[20]
    e.set_para_text(p, [
        ("Nhóm thực hiện đồ án xin chân thành cảm ơn Ban Giám hiệu Trường Đại học Tài nguyên và "
         "Môi trường Hà Nội, Khoa Công nghệ Thông tin và quý thầy cô đã trang bị cho nhóm những "
         "kiến thức nền tảng về phân tích – thiết kế hệ thống, cơ sở dữ liệu, lập trình hướng đối "
         "tượng và kiến trúc phần mềm trong suốt quá trình học tập.", None, None),
    ])
    p = e.blocks[21]
    e.set_para_text(p, [
        ("Nhóm xin bày tỏ lòng biết ơn sâu sắc tới ", None, None),
        ("giảng viên hướng dẫn học phần Phát triển phần mềm hướng dịch vụ", True, None),
        (" đã tận tình định hướng và góp ý về kiến trúc hướng dịch vụ, thiết kế hợp đồng REST API, "
         "đồng thời chỉ ra những khiếm khuyết quan trọng như bài toán tranh chấp tồn kho và tính "
         "bất biến của webhook thanh toán. Nhóm cũng xin cảm ơn cộng đồng mã nguồn mở "
         "(Spring Framework, Hibernate ORM, Thymeleaf), đội ngũ kỹ thuật VNPAY đã cung cấp tài "
         "liệu tích hợp trên môi trường Sandbox, cùng gia đình và bạn bè đã hỗ trợ, thử nghiệm "
         "sản phẩm.", None, None),
    ])
    p = e.blocks[22]
    e.set_para_text(p, [
        ("Do giới hạn về thời gian và kinh nghiệm triển khai, báo cáo khó tránh khỏi thiếu sót. "
         "Nhóm rất mong nhận được sự chỉ dẫn, góp ý của quý thầy cô để hoàn thiện cả sản phẩm "
         "phần mềm lẫn năng lực chuyên môn.", None, None),
    ])
    e.delete_blocks([23, 24])
    e.refresh()

    # ---------------------------------------------------------------- LỜI CAM ĐOAN
    # gộp 5 cam đoan thành 3 cam đoan, giữ nguyên tinh thần
    lead = e.find('xin cam đoan những nội dung sau đây', exact=False)[0]
    idx = lead + 1
    e.set_para_text(e.blocks[idx], [
        ("Thứ nhất", True, None),
        (", đồ án môn học với đề tài ", None, None),
        ("“Xây dựng dịch vụ API và ứng dụng client cho hệ thống bán hàng linh kiện PC trực tuyến”",
         False, True),
        (" là công trình do chính nhóm trực tiếp nghiên cứu, phân tích, thiết kế, lập trình và "
         "kiểm thử dưới sự hướng dẫn của giảng viên phụ trách học phần. Toàn bộ mã nguồn của hệ "
         "thống — tầng dịch vụ backend (com.banlinhkien.*), các bộ điều khiển REST API, lớp "
         "nghiệp vụ, lớp truy cập dữ liệu, khuôn mẫu giao diện Thymeleaf và bộ kiểm thử tự động — "
         "đều do nhóm tự xây dựng, được lưu trữ công khai và có lịch sử phiên bản đầy đủ tại kho "
         "mã nguồn Git của nhóm; không có thành phần nào được sao chép nguyên trạng từ đồ án khác "
         "hoặc từ sản phẩm thương mại đang lưu hành.", None, None),
    ])
    e.set_para_text(e.blocks[idx + 1], [
        ("Thứ hai", True, None),
        (", các thư viện, framework và dịch vụ của bên thứ ba được sử dụng trong hệ thống "
         "(Spring Boot, Spring Security, Spring Data JPA, Hibernate ORM, Thymeleaf, Bootstrap, "
         "MySQL Connector/J, Project Lombok, Jackson, cổng thanh toán VNPAY Sandbox, dịch vụ suy "
         "luận ngôn ngữ Groq) đều là những thành phần được phát hành hợp pháp theo giấy phép mã "
         "nguồn mở hoặc theo điều khoản sử dụng công khai của nhà cung cấp; nhóm sử dụng đúng vai "
         "trò kỹ thuật của từng thành phần và đã trích dẫn đầy đủ trong mục ", None, None),
        ("Tài liệu tham khảo", True, None),
        (".", None, None),
    ])
    e.set_para_text(e.blocks[idx + 2], [
        ("Thứ ba", True, None),
        (", mọi số liệu, kết quả đo đạc, ảnh chụp màn hình, nhật ký thực thi và báo cáo kiểm thử "
         "trình bày trong Chương 3 đều là kết quả thực tế thu được khi biên dịch và vận hành hệ "
         "thống trên môi trường thử nghiệm của nhóm, không có số liệu nào được suy diễn hoặc ngụy "
         "tạo; các nội dung lý thuyết kế thừa từ giáo trình, tiêu chuẩn kỹ thuật và tài liệu "
         "chuyên ngành đều được trích dẫn theo chuẩn IEEE.", None, None),
    ])
    e.delete_blocks([idx + 3, idx + 4])
    e.refresh()

    # ------------------------------------------------- DANH MỤC THUẬT NGỮ (rút gọn cột giải nghĩa)
    short = {
        "ACID": "Bốn thuộc tính bảo đảm tính đúng đắn của giao dịch CSDL (InnoDB, @Transactional).",
        "AJAX": "Gửi yêu cầu HTTP bất đồng bộ từ trình duyệt; đồ án hiện thực bằng Fetch API.",
        "API": "Giao diện lập trình ứng dụng; tập endpoint REST do backend công bố.",
        "BCrypt": "Hàm băm mật khẩu một chiều có muối và hệ số chi phí.",
        "CSRF": "Tấn công giả mạo yêu cầu liên trang.",
        "DAO": "Đối tượng truy cập dữ liệu; do các interface *Repository đảm nhiệm.",
        "DI": "Tiêm phụ thuộc – cơ chế cung cấp bean của Spring IoC Container.",
        "DTO": "Đối tượng truyền dữ liệu giữa các tầng và giữa API với client.",
        "ERD": "Sơ đồ thực thể liên kết mô tả cấu trúc cơ sở dữ liệu.",
        "HMAC": "Mã xác thực thông điệp dựa trên hàm băm có khóa; VNPAY dùng HMAC-SHA512.",
        "HTTP": "Giao thức truyền siêu văn bản – nền tảng giao tiếp client – service.",
        "IDOR": "Lỗ hổng tham chiếu đối tượng trực tiếp; phòng chống bằng access_token UUID.",
        "IoC": "Đảo ngược điều khiển – nguyên lý nền tảng của Spring Container.",
        "IPN": "Webhook cổng thanh toán chủ động gọi tới máy chủ bán hàng để báo kết quả giao dịch.",
        "JPA": "Đặc tả Jakarta EE về ánh xạ đối tượng – quan hệ; Hibernate là hiện thực.",
        "JPQL": "Ngôn ngữ truy vấn hướng đối tượng của JPA.",
        "JSON": "Định dạng trao đổi dữ liệu văn bản nhẹ, Content-Type của toàn bộ REST API.",
        "JVM": "Máy ảo Java.",
        "KPI": "Chỉ số hiệu quả trọng yếu hiển thị trên bảng điều khiển quản trị.",
        "LLM": "Mô hình ngôn ngữ lớn; dùng để diễn giải văn bản cho gợi ý cấu hình.",
        "MVC": "Mẫu kiến trúc phân tách dữ liệu – giao diện – điều khiển của Spring Web MVC.",
        "ORM": "Ánh xạ đối tượng – quan hệ.",
        "PSU": "Bộ nguồn máy tính.",
        "RBAC": "Kiểm soát truy cập dựa trên vai trò (ROLE_ADMIN, ROLE_USER).",
        "REST": "Phong cách kiến trúc phần mềm cho hệ phân tán do R. T. Fielding đề xuất.",
        "SOA": "Kiến trúc hướng dịch vụ.",
        "SPA": "Ứng dụng một trang.",
        "SQL": "Ngôn ngữ truy vấn có cấu trúc.",
        "SSR": "Kết xuất giao diện tại máy chủ; Thymeleaf đảm nhiệm, kết hợp Fetch API.",
        "TDD": "Phát triển hướng kiểm thử.",
        "UC": "Trường hợp sử dụng (Use Case).",
        "UML": "Ngôn ngữ mô hình hóa thống nhất.",
        "URI / URL": "Định danh / định vị tài nguyên thống nhất.",
        "UUID": "Định danh duy nhất toàn cục.",
        "VGA": "Card đồ họa rời.",
        "VNPAY": "Cổng thanh toán điện tử trung gian được tích hợp trong hệ thống.",
    }
    tbl = None
    for b in e.blocks:
        if not isinstance(b, rt.Paragraph) and b.rows and b.rows[0].cells[0].text.strip() == 'Từ viết tắt':
            tbl = b
            break
    for row in tbl.rows[1:]:
        key = row.cells[0].text.strip()
        if key in short:
            rt.ReportEditor.set_para_text(e, row.cells[2].paragraphs[0], short[key])
            for extra in row.cells[2].paragraphs[1:]:
                extra._p.getparent().remove(extra._p)

    e.save()
    print("Đã rút gọn phần đầu báo cáo.")


if __name__ == '__main__':
    main()
