"""Bước 2 – Rút gọn phần MỞ ĐẦU (từ ~8 trang xuống ~5 trang)."""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


def set_text(e, i, segs):
    e.set_para_text(e.blocks[i], segs)


def main():
    e = rt.ReportEditor(DOC)

    # --- 1.1 Đặt vấn đề: bốn đặc trưng của thị trường linh kiện -------------
    set_text(e, 243, [
        ("Sản phẩm không độc lập về mặt kỹ thuật. ", True, None),
        ("Một bộ vi xử lý chỉ vận hành trên bo mạch chủ cùng socket và chipset tương thích; một bộ "
         "nguồn công suất thấp không đủ cấp điện cho card đồ họa cao cấp. Giá trị sử dụng của mỗi "
         "linh kiện chỉ được xác lập trong quan hệ với các linh kiện còn lại — điều mà mô hình "
         "danh mục sản phẩm phẳng không biểu diễn được.", None, None),
    ])
    set_text(e, 244, [
        ("Không gian thuộc tính kỹ thuật lớn và không đồng nhất. ", True, None),
        ("Mỗi nhóm linh kiện có tập thuộc tính riêng (CPU: socket, số nhân, TDP; RAM: chuẩn DDR, "
         "bus, dung lượng; card đồ họa: chipset, VRAM, công suất yêu cầu). Lược đồ quan hệ cứng "
         "nhắc không mô tả được sự đa dạng này nếu không dùng mô hình thuộc tính động.", None, None),
    ])
    set_text(e, 245, [
        ("Giá trị đơn hàng cao và biến động tồn kho nhanh. ", True, None),
        ("Một bộ máy hoàn chỉnh có giá từ mười đến vài chục triệu đồng trong khi tồn kho các dòng "
         "linh kiện phổ biến rất mỏng — điều kiện lý tưởng để phát sinh bán vượt tồn kho "
         "(overselling) khi nhiều khách hàng đặt mua đồng thời.", None, None),
    ])
    set_text(e, 246, [
        ("Rủi ro thanh toán. ", True, None),
        ("Khách hàng có xu hướng thanh toán trực tuyến qua cổng trung gian, đặt ra yêu cầu khắt "
         "khe về xác thực chữ ký số, chống giả mạo số tiền, chống phát lại (replay) và bảo đảm "
         "tính bất biến (idempotency) của thông báo thanh toán.", None, None),
    ])

    # --- 1.2 Thực trạng ------------------------------------------------------
    set_text(e, 249, [
        ("Hạn chế về tư vấn kỹ thuật tự động. ", True, None),
        ("Phần lớn nền tảng chỉ lọc theo thương hiệu và khoảng giá, không kiểm tra tính tương "
         "thích socket, chipset và công suất nên người dùng phổ thông dễ chọn sai cấu hình.",
         None, None),
    ])
    set_text(e, 250, [
        ("Hạn chế về kiến trúc phần mềm. ", True, None),
        ("Nhiều hệ thống là nguyên khối (monolithic) với giao diện kết xuất phía máy chủ gắn chặt "
         "vào logic nghiệp vụ, khiến việc bổ sung kênh bán hàng mới (di động, kiosk, đối tác "
         "affiliate) phải viết lại hoặc bọc thêm một lớp API tạm bợ.", None, None),
    ])
    set_text(e, 251, [
        ("Hạn chế về tính toàn vẹn dữ liệu. ", True, None),
        ("Lối lập trình “đọc tồn kho rồi trừ tồn kho” mà không có cơ chế khóa khiến hiện tượng "
         "tranh chấp (race condition) làm tồn kho âm hoàn toàn có thể xảy ra dưới tải cao.",
         None, None),
    ])

    # --- 1.3 Ba trụ cột giải pháp -------------------------------------------
    set_text(e, 254, [
        ("Trụ cột thứ nhất – Tách bạch nhà cung cấp và người tiêu thụ dịch vụ. ", True, None),
        ("Dịch vụ backend công bố các năng lực nghiệp vụ dưới dạng tài nguyên REST trao đổi bằng "
         "JSON; ứng dụng client web tiêu thụ các tài nguyên đó. Mọi quy tắc nghiệp vụ (tính giá "
         "cuối, kiểm tra tồn kho, xác thực voucher, phí vận chuyển, đối soát thanh toán) đều nằm "
         "ở tầng dịch vụ, nhờ vậy khi bổ sung client mới — chẳng hạn ứng dụng Android — hợp đồng "
         "dịch vụ hiện hữu được tái sử dụng nguyên vẹn.", None, None),
    ])
    set_text(e, 255, [
        ("Trụ cột thứ hai – Đưa tri thức kỹ thuật phần cứng vào dịch vụ. ", True, None),
        ("Dịch vụ PcBuilderService quản lý bảy khe linh kiện bắt buộc, chuẩn hóa chuỗi socket, lọc "
         "hai chiều danh sách linh kiện tương thích và tự động loại bỏ linh kiện xung đột khi "
         "người dùng thay đổi lựa chọn. Dịch vụ PcBuilderAiService áp dụng mô hình lai: luật "
         "nghiệp vụ trên dữ liệu tồn kho thực sinh ra cấu hình khả thi, mô hình ngôn ngữ lớn chỉ "
         "đảm nhiệm phần diễn giải bằng ngôn ngữ tự nhiên.", None, None),
    ])
    set_text(e, 256, [
        ("Trụ cột thứ ba – Bảo đảm tính toàn vẹn giao dịch ở mức công nghiệp. ", True, None),
        ("Nghiệp vụ đặt hàng nằm trong một giao dịch cơ sở dữ liệu duy nhất, áp dụng khóa bi quan "
         "(PESSIMISTIC_WRITE) trên từng bản ghi sản phẩm theo thứ tự khóa tăng dần để vừa chống "
         "bán vượt tồn kho vừa chống bế tắc (deadlock) đa tài nguyên; nghiệp vụ thanh toán xác "
         "thực chữ ký HMAC-SHA512 theo phương pháp so sánh thời gian hằng, đối chiếu số tiền và "
         "kiểm tra bất biến trạng thái để chống phát lại.", None, None),
    ])

    # --- 2.1 Mục tiêu tổng quát ---------------------------------------------
    set_text(e, 259, [
        ("Nghiên cứu, vận dụng nguyên lý kiến trúc hướng dịch vụ và chuẩn thiết kế RESTful API để "
         "phân tích, thiết kế, hiện thực và kiểm chứng một hệ thống thương mại điện tử chuyên "
         "ngành linh kiện máy tính, gồm một dịch vụ backend công bố API và một ứng dụng client "
         "web tiêu thụ API đó, đáp ứng đồng thời yêu cầu về nghiệp vụ, tính toàn vẹn dữ liệu và "
         "an toàn thanh toán.", None, None),
    ])

    # --- 4.1 Nghiên cứu lý thuyết -------------------------------------------
    set_text(e, 276, [
        ("Nhóm tổng hợp và phân tích tài liệu gốc: luận án của R. T. Fielding về phong cách kiến "
         "trúc REST, tài liệu chuẩn hóa của Thomas Erl về thiết kế dịch vụ, đặc tả Jakarta "
         "Persistence, tài liệu tham chiếu Spring Framework và Spring Security, tài liệu MySQL về "
         "cơ chế khóa InnoDB và tài liệu tích hợp VNPAY; trên cơ sở đó rút ra các tiêu chí thiết "
         "kế áp dụng trực tiếp vào hệ thống.", None, None),
    ])

    # --- 4.2 OOAD -----------------------------------------------------------
    set_text(e, 278, [
        ("Nghiệp vụ được mô hình hóa theo quy trình OOAD: xác định tác nhân → xác định trường hợp "
         "sử dụng → đặc tả luồng sự kiện → nhận diện lớp thực thể, lớp biên và lớp điều khiển → "
         "thiết lập quan hệ giữa các lớp → ánh xạ sang lược đồ quan hệ. Sản phẩm mô hình hóa được "
         "biểu diễn bằng UML: biểu đồ Use Case, biểu đồ lớp, biểu đồ tuần tự và biểu đồ máy trạng "
         "thái.", None, None),
    ])

    # --- 4.5 Thực nghiệm và đánh giá ----------------------------------------
    set_text(e, 288, [
        ("Kết quả được thu thập từ nhật ký thực thi bộ kiểm thử, ảnh chụp màn hình các luồng "
         "nghiệp vụ và kiểm tra trực tiếp trạng thái cơ sở dữ liệu sau mỗi kịch bản. Việc đánh "
         "giá được thực hiện bằng cách đối chiếu từng mục tiêu ở mục 2.2 với kết quả thực nghiệm "
         "theo ba mức: Hoàn thành, Hoàn thành một phần, Chưa thực hiện.", None, None),
    ])

    # --- 5. Cấu trúc đồ án --------------------------------------------------
    set_text(e, 291, [
        ("Chương 1 – Giới thiệu tổng quan về đề tài: ", True, None),
        ("bối cảnh và tính cấp thiết của đề tài; lý thuyết về kiến trúc hướng dịch vụ và chuẩn "
         "RESTful API; luận giải lựa chọn công nghệ nền tảng; khảo sát các hệ thống tương tự và "
         "xác lập điểm cải tiến mà đề tài hướng tới.", None, None),
    ])
    set_text(e, 292, [
        ("Chương 2 – Phân tích và thiết kế hệ thống: ", True, None),
        ("đặc tả yêu cầu chức năng, phi chức năng và quy tắc nghiệp vụ; mô hình hóa nghiệp vụ "
         "bằng biểu đồ Use Case và các bảng đặc tả; sơ đồ tuần tự cho các luồng nghiệp vụ trọng "
         "yếu; thiết kế kiến trúc phân tầng, cơ sở dữ liệu, hợp đồng dịch vụ API và ứng dụng "
         "client.", None, None),
    ])
    set_text(e, 293, [
        ("Chương 3 – Kết quả thực nghiệm và đánh giá: ", True, None),
        ("môi trường cài đặt và triển khai; cấu trúc mã nguồn và cách hiện thực các API cùng các "
         "xử lý nghiệp vụ phức tạp; ứng dụng client và cơ chế tiêu thụ API; kết quả kiểm thử đơn "
         "vị, tích hợp và đồng thời; ma trận kịch bản kiểm thử và bảng đánh giá mức độ đáp ứng "
         "yêu cầu.", None, None),
    ])
    set_text(e, 294, [
        ("Kết luận và hướng phát triển", True, None),
        (" tổng kết những kết quả đạt được, chỉ rõ các hạn chế còn tồn tại và đề xuất lộ trình "
         "phát triển tiếp theo.", None, None),
    ])

    # --- Bảng: rút gọn nội dung dài trong các ô ----------------------------
    # Bảng 3.2 – Phạm vi nghiên cứu
    for b in e.blocks:
        if isinstance(b, rt.Paragraph):
            continue
        head = b.rows[0].cells[0].text.strip() if b.rows else ''
        if head == 'Khía cạnh':
            cells = {
                'Nghiệp vụ': 'Duyệt, tìm kiếm, xem chi tiết linh kiện; ráp cấu hình PC bảy khe có '
                             'kiểm tra socket; giỏ hàng; mã giảm giá; phí vận chuyển theo vùng; '
                             'đặt hàng; thanh toán COD, chuyển khoản, VNPAY; theo dõi đơn; đánh '
                             'giá sản phẩm; quản trị danh mục, sản phẩm, đơn hàng, kho, voucher, '
                             'banner, người dùng và thống kê doanh thu',
                'Kiến trúc': 'Dịch vụ backend đơn khối phân tầng công bố REST API; client web kết '
                             'xuất phía máy chủ kết hợp gọi API bất đồng bộ',
                'Dữ liệu': 'MySQL 8 với 17 bảng nghiệp vụ cốt lõi ánh xạ bằng JPA',
                'Bảo mật': 'Xác thực biểu mẫu dựa trên phiên, băm mật khẩu BCrypt, phân quyền theo '
                           'vai trò, chống IDOR bằng token đơn hàng, xác thực chữ ký thanh toán',
                'Kiểm thử': 'Kiểm thử đơn vị, tích hợp theo lát cắt chức năng và kiểm thử đa luồng',
                'Triển khai': 'Triển khai cục bộ (local deployment) trên máy phát triển',
            }
            for row in b.rows[1:]:
                key = row.cells[0].text.strip()
                if key in cells:
                    e.set_para_text(row.cells[1].paragraphs[0], cells[key])
                    for extra in row.cells[1].paragraphs[1:]:
                        extra._p.getparent().remove(extra._p)
        if head == 'Nội dung loại trừ':
            reasons = {
                'Phân rã thành microservices triển khai độc lập, service registry, API gateway':
                    'Vượt quy mô đồ án môn học; hệ thống hiện tại là monolith phân tầng theo định '
                    'hướng dịch vụ',
                'Ứng dụng client di động gốc (Android/iOS)':
                    'Nằm ngoài phạm vi; hợp đồng API đã sẵn sàng cho việc mở rộng',
                'Xác thực không trạng thái bằng JWT/OAuth2':
                    'Hệ thống dùng xác thực dựa trên phiên; JWT được nêu ở phần hướng phát triển',
                'Triển khai lên hạ tầng đám mây, container hóa, điều phối Kubernetes':
                    'Được nêu ở phần hướng phát triển',
                'Tích hợp vận chuyển thực tế với đối tác logistics':
                    'Hệ thống chỉ mô phỏng bằng bảng vùng vận chuyển',
                'Kiểm thử hiệu năng tải quy mô lớn bằng JMeter/Gatling':
                    'Chỉ thực hiện kiểm thử đồng thời ở mức đơn vị nghiệp vụ',
            }
            for row in b.rows[1:]:
                key = row.cells[0].text.strip()
                if key in reasons:
                    e.set_para_text(row.cells[1].paragraphs[0], reasons[key])
                    for extra in row.cells[1].paragraphs[1:]:
                        extra._p.getparent().remove(extra._p)
        if head == 'Mã':  # bảng mục tiêu cụ thể
            crit = {
                'MT02': 'Biểu đồ Use Case tổng quan, 5 biểu đồ phân rã, 10 bảng đặc tả UC, 5 sơ đồ '
                        'tuần tự',
                'MT05': 'Đầy đủ method, URI, tham số, thân yêu cầu/phản hồi, mã trạng thái',
                'MT07': 'Kiểm thử đa luồng chứng minh tồn kho không âm và số đơn thành công đúng '
                        'bằng lượng tồn',
                'MT09': 'Tối thiểu 50 ca kiểm thử, phủ kiểm thử đơn vị, tích hợp và đồng thời',
            }
            for row in b.rows[1:]:
                key = row.cells[0].text.strip()
                if key in crit:
                    e.set_para_text(row.cells[2].paragraphs[0], crit[key])
                    for extra in row.cells[2].paragraphs[1:]:
                        extra._p.getparent().remove(extra._p)
        if head == 'Lát cắt':
            for row in b.rows[1:]:
                name = row.cells[0].text.strip()
                if name == 'Slice 1':
                    e.set_para_text(row.cells[1].paragraphs[0],
                                    'Bộ khung vận hành được: trang chủ, danh mục sản phẩm, đăng '
                                    'nhập, API xem nhanh sản phẩm')
                if name == 'Slice 2':
                    e.set_para_text(row.cells[1].paragraphs[0],
                                    'Giỏ hàng, dịch vụ địa giới hành chính, luồng đặt hàng đầu – '
                                    'cuối')

    e.save()
    print("Đã rút gọn phần MỞ ĐẦU.")


if __name__ == '__main__':
    main()
