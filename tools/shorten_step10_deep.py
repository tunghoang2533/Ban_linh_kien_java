"""Bước 10 – Rút gọn sâu: cắt mục phụ, gộp từ điển dữ liệu, viết lại đoạn dài."""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt
from report_toolkit import ReportEditor, Paragraph, set_cell_text

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


def cut(text: str, limit: int) -> str:
    t = ' '.join(text.split())
    if len(t) <= limit:
        return text
    s = t[:limit]
    s = s[:s.rfind(' ')]
    return s.rstrip(' .,;:-–') + '…'


# --- xoá đoạn văn theo tiền tố -------------------------------------------
DEL = [
    # KẾT LUẬN – bỏ mục "Bài học kinh nghiệm" (5 đoạn dài + tiêu đề)
    'Bài học thứ nhất – Ranh giới hợp đồng quan trọng hơn công nghệ cụ thể',
    'Bài học thứ hai – Tính đúng đắn dưới tranh chấp đồng thời phải được thiết kế từ đầu',
    'Bài học thứ ba – Không bao giờ tin dữ liệu do client gửi lên',
    'Bài học thứ tư – Phòng vệ nhiều lớp là cách duy nhất bảo đảm quy tắc nghiệp vụ tuyệt đối',
    'Bài học thứ năm – Mô tả đúng hệ thống đang tồn tại quan trọng hơn mô tả một hệ thống lý tưởng',
    # KẾT LUẬN – bớt diễn giải trùng lặp
    'Đề tài đã hệ thống hóa cơ sở lý thuyết của kiến trúc hướng dịch vụ',
    'Thứ nhất, hệ thống chưa được kiểm chứng ở quy mô dữ liệu và lưu lượng thực tế',
    'Thứ hai, hệ thống chưa được triển khai lên môi trường vận hành thật',
    'Thứ tư, chưa có cơ chế theo dõi và cảnh báo vận hành',
    'Thứ ba, công cụ ráp cấu hình với ràng buộc tương thích cứng hai chiều',
    # CH1 – bớt phân tích lý thuyết dài
    'Phần lớn các ứng dụng thương mại điện tử quy mô vừa được xây dựng theo mô hình nguyên khối truyền thống',
    'Phân tích sơ đồ: khác biệt bản chất nằm ở vị trí ranh giới hợp đồng',
    'Ưu điểm: danh mục rất rộng (cả thiết bị nguyên bộ linh kiện rời',
    'Nhu cầu sở hữu máy tính cá nhân hiệu năng cao tại Việt Nam được thúc đẩy bởi bốn động lực song song',
    # MỞ ĐẦU – bớt đoạn mô tả phương pháp
    'Nhóm tổng hợp và phân tích tài liệu gốc',
    'Kết quả được thu thập từ nhật ký thực thi bộ kiểm thử',
    # CH2 – bớt đoạn dẫn dắt và nhận xét dài
    'Yêu cầu chức năng được phân rã theo ba nhóm tác nhân',
    'Quy tắc nghiệp vụ là những ràng buộc thuộc về bản chất bài toán',
    'Nhận xét thiết kế: Bảng này áp dụng mô hình thực thể – thuộc tính – giá trị',
    'Nhận xét thiết kế: Ràng buộc duy nhất unique_user_voucher',
    'Nhận xét thiết kế: Việc lưu danh sách tỉnh dưới dạng mảng JSON trong một cột TEXT',
    'Quan hệ hợp thành (composition, ký hiệu hình thoi đặc)',
    # CH3
    'Đặc điểm kiến trúc: đây là nhóm endpoint phi trạng thái hoàn toàn',
    'Ba kết luận rút ra từ kết quả kiểm thử đồng thời',
]

# --- viết lại đoạn dài ----------------------------------------------------
REPL = {
    'Thị trường thương mại điện tử Việt Nam trong giai đoạn 2020 – 2026':
        "Thị trường thương mại điện tử Việt Nam tăng trưởng hai chữ số mỗi năm, trong đó nhóm hàng "
        "linh kiện máy tính có đặc thù riêng: người mua thường phải tự đối chiếu thông số kỹ thuật "
        "giữa nhiều linh kiện trước khi quyết định.",
    'Sản phẩm không độc lập về mặt kỹ thuật':
        "Sản phẩm không độc lập về mặt kỹ thuật: một bộ vi xử lý chỉ vận hành trên bo mạch chủ cùng "
        "socket và chipset, còn công suất nguồn phải đủ cho tổng mức tiêu thụ của toàn cấu hình.",
    'Trụ cột thứ nhất – Tách bạch nhà cung cấp và người tiêu thụ dịch vụ':
        "Trụ cột thứ nhất – Tách bạch nhà cung cấp và người tiêu thụ dịch vụ: máy chủ công bố các "
        "năng lực nghiệp vụ qua REST API, giao diện web chỉ là một trong nhiều khách hàng có thể.",
    'Trụ cột thứ hai – Đưa tri thức kỹ thuật phần cứng vào dịch vụ':
        "Trụ cột thứ hai – Đưa tri thức kỹ thuật phần cứng vào dịch vụ: dịch vụ ráp cấu hình quản lý "
        "bảy khe linh kiện và kiểm tra tương thích thay vì để người dùng tự đối chiếu thủ công.",
    'Trụ cột thứ ba – Bảo đảm tính toàn vẹn giao dịch ở mức công nghiệp':
        "Trụ cột thứ ba – Bảo đảm tính toàn vẹn giao dịch ở mức công nghiệp: nghiệp vụ đặt hàng nằm "
        "trong một giao dịch cơ sở dữ liệu có khóa bi quan, chống bán vượt tồn kho khi tranh chấp.",
    'Lưu ý về webhook IPN':
        "Lưu ý về webhook IPN: đặc tả VNPAY quy định máy chủ nhận thông báo IPN qua phương thức GET "
        "với dữ liệu trên query string, mâu thuẫn với nguyên tắc bất biến của REST nhưng buộc phải "
        "tuân thủ; đây là sai lệch có kiểm soát và đã được ghi chú rõ trong thiết kế.",
    'Quy ước phản hồi lỗi':
        "Quy ước phản hồi lỗi: lỗi giao thức hoặc đầu vào trả mã HTTP tương ứng (400, 404) kèm JSON "
        "có trường message; kết quả nghiệp vụ không thuận lợi nhưng hợp lệ vẫn trả 200 kèm "
        "success: false — giúp client phân biệt lỗi hệ thống với lỗi người dùng.",
    'Việc lựa chọn Service-Oriented Monolith thay vì microservices':
        "Việc lựa chọn Service-Oriented Monolith thay vì microservices có cơ sở rõ ràng: quy mô "
        "nghiệp vụ hiện tại chưa cần triển khai độc lập từng dịch vụ, trong khi yêu cầu nhất quán "
        "giao dịch mạnh lại được bảo đảm dễ dàng hơn trong một tiến trình duy nhất.",
    'Giải thích biểu đồ: Biểu đồ phân chia hệ thống thành năm phân hệ chức năng':
        "Giải thích biểu đồ: hệ thống được phân chia thành năm phân hệ chức năng; quan hệ kế thừa "
        "giữa các tác nhân cho phép tái sử dụng đặc tả của tác nhân cơ sở cho các tác nhân mở rộng.",
    'Mười ca sử dụng trọng yếu được đặc tả theo mẫu chuẩn gồm mười ba trường':
        "Mười ca sử dụng trọng yếu được đặc tả theo mẫu chuẩn mười ba trường; Bảng 2.7 tổng hợp "
        "các trường cốt lõi của mười ca, hai ca phức tạp nhất (UC06, UC07) được đặc tả đầy đủ ở "
        "Bảng 2.8 và Bảng 2.9.",
    'Thứ nhất, về vai trò kép của tầng 1':
        "Thứ nhất, về vai trò kép của tầng 1: gói controller.api công bố hợp đồng dịch vụ, còn gói "
        "controller.view tiêu thụ chính các dịch vụ đó để kết xuất giao diện — nhờ vậy cùng một "
        "logic nghiệp vụ phục vụ cả hai kênh mà không bị sao chép.",
    'Cơ sở dữ liệu db_ban_linh_kien chứa tổng cộng 56 bảng':
        "Cơ sở dữ liệu db_ban_linh_kien chứa 56 bảng, trong đó 17 bảng tạo thành lõi nghiệp vụ của "
        "hệ thống; mục này trình bày danh mục bảng, từ điển dữ liệu các bảng trọng yếu, ma trận khóa "
        "ngoại và các tham chiếu mềm.",
    'Ghi chú quan trọng về sai lệch giữa đặc tả ban đầu và lược đồ thực tế':
        "Ghi chú về sai lệch giữa đặc tả ban đầu và lược đồ thực tế: đặc tả mô tả hai bảng app_* "
        "dành riêng cho ứng dụng client, nhưng lược đồ thực tế dùng chung bảng users với vai trò "
        "phân quyền — sai lệch này được chấp nhận và ghi nhận minh bạch trong báo cáo.",
    'Về sự cần thiết của hai kênh':
        "Về sự cần thiết của hai kênh: Return URL phụ thuộc trình duyệt của khách nên có thể bị "
        "đóng giữa chừng, còn IPN do máy chủ VNPAY gọi trực tiếp nên đáng tin cậy hơn; vì vậy hệ "
        "thống dùng IPN làm căn cứ ghi nhận thanh toán và đối chiếu chéo với Return URL.",
    'Thứ nhất – Lọc hai lớp cho ràng buộc tương thích':
        "Thứ nhất – Lọc hai lớp cho ràng buộc tương thích: hệ thống kiểm tra ngay khi người dùng "
        "chọn linh kiện và kiểm tra lần nữa trước khi tạo đơn, bảo đảm cấu hình luôn hợp lệ kể cả "
        "khi dữ liệu linh kiện thay đổi giữa hai thời điểm.",
    'Thứ ba, về thiết kế luồng xử lý':
        "Thứ ba, về thiết kế luồng xử lý: năm sơ đồ tuần tự đặc tả các luồng phức tạp nhất, tập "
        "trung vào xác thực, ráp cấu hình, đặt hàng có khóa bi quan và thanh toán VNPAY hai kênh.",
    'Thứ tư, về thiết kế hệ thống':
        "Thứ tư, về thiết kế hệ thống: chương xác lập kiến trúc năm tầng, từ điển dữ liệu 17 bảng "
        "lõi, hợp đồng REST cho 17 endpoint và chiến lược trạng thái phía client gồm bốn trạng thái.",
    'Một điểm cần nhấn mạnh về phương pháp':
        "Một điểm cần nhấn mạnh về phương pháp: báo cáo đối chiếu đặc tả ban đầu với lược đồ cơ sở "
        "dữ liệu thực tế và ghi nhận minh bạch mọi sai lệch thay vì mô tả một hệ thống lý tưởng.",
    'Yêu cầu chức năng: 41 yêu cầu chức năng phân theo ba nhóm tác nhân':
        "Về đặc tả yêu cầu, chương đã xác định 41 yêu cầu chức năng theo ba nhóm tác nhân, 12 yêu "
        "cầu phi chức năng và 31 quy tắc nghiệp vụ có mã định danh duy nhất.",
    'Đề tài đã xây dựng một bản thiết kế đầy đủ và có thể hiện thực trực tiếp':
        "Đề tài đã xây dựng một bản thiết kế đầy đủ và có thể hiện thực trực tiếp: 41 yêu cầu chức "
        "năng, 12 yêu cầu phi chức năng, 31 quy tắc nghiệp vụ, mười ca sử dụng, năm sơ đồ tuần tự, "
        "17 bảng dữ liệu lõi và 17 endpoint REST.",
    'Về cài đặt, hệ thống đã được hiện thực hoàn chỉnh với 7.027 dòng mã backend':
        "Về cài đặt, hệ thống được hiện thực hoàn chỉnh với 7.027 dòng mã backend tổ chức thành 11 "
        "gói theo kiến trúc phân tầng, kèm 1.709 dòng mã kiểm thử.",
}


def main():
    e = ReportEditor(DOC)
    # 1. xoá đoạn văn
    for prefix in DEL:
        idx = e.find(prefix)
        if idx:
            e.delete_blocks(idx)
    # 2. viết lại đoạn văn
    for prefix, new in REPL.items():
        idx = e.find(prefix)
        if idx:
            e.set_para_text(e.blocks[idx[0]], [(new, None, None)])
    e.refresh()

    # 3. xoá tiêu đề "4. Bài học kinh nghiệm" nếu còn
    idx = e.find('Bài học kinh nghiệm', exact=True)
    if idx:
        e.delete_blocks(idx)

    # 4. xoá 3 bảng từ điển dữ liệu phụ (caption + bảng đi kèm)
    for key in ('Từ điển dữ liệu bảng order_items',
                'Từ điển dữ liệu bảng vouchers',
                'Từ điển dữ liệu bảng warehouse_logs'):
        idx = e.find(key)
        if idx:
            i = idx[0]
            nxt = e.blocks[i + 1] if i + 1 < len(e.blocks) else None
            kill = [i]
            if not isinstance(nxt, Paragraph):
                kill.append(i + 1)
                nxt2 = e.blocks[i + 2] if i + 2 < len(e.blocks) else None
                if isinstance(nxt2, Paragraph) and nxt2.text.strip().startswith('Ghi chú'):
                    kill.append(i + 2)
            e.delete_blocks(kill)

    # 5. xoá bảng "Danh mục lớp kiểm thử" (trùng với hai bảng UT/IT)
    idx = e.find('Bảng 3.11 – Danh mục lớp kiểm thử tự động')
    if not idx:
        idx = e.find('Danh mục lớp kiểm thử tự động')
    if idx:
        i = idx[0]
        kill = [i]
        nxt = e.blocks[i + 1] if i + 1 < len(e.blocks) else None
        if not isinstance(nxt, Paragraph):
            kill.append(i + 1)
        e.delete_blocks(kill)

    # 6. xoá khối mã 12 dòng ở mục quy trình cài đặt
    idx = e.find('Quy trình cài đặt và khởi chạy')
    if idx:
        a = idx[0]
        for j in range(a, a + 40):
            b = e.blocks[j]
            if isinstance(b, Paragraph) and b.style.name == 'Heading 3':
                break
            if isinstance(b, Paragraph) and b.text.strip().startswith('Khởi chạy ứng dụng ở chế độ phát triển'):
                e.delete_blocks([j])
                break

    # 7. cắt gọn thêm một số cột bảng
    e.refresh()
    HEAD_RULES = [
        (['Mã', 'Tên yêu cầu'], {2: 62}),
        (['Mã', 'Thuộc tính chất lượng'], {2: 62, 3: 62}),
        (['Mã', 'Tên quy tắc'], {2: 66, 3: 46}),
        (['Method', 'Endpoint', 'Chức năng'], {2: 45, 3: 52}),
        (['Mã', 'Tên Use Case'], {4: 85}),
        (['Tên trường', 'Kiểu dữ liệu'], {4: 34}),
        (['Bảng', 'Trường và kiểu dữ liệu'], {1: 70, 2: 60}),
        (['Tên ràng buộc'], {3: 40}),
        (['Hạng mục quy ước'], {1: 50, 2: 38}),
        (['STT', 'Màn hình', 'Chức năng client'], {2: 30, 3: 22}),
        (['Hạng mục', 'Công cụ'], {1: 34, 3: 45}),
        (['Nhóm', 'Khóa cấu hình'], {1: 30, 2: 32, 3: 42}),
        (['Test ID'], {1: 30, 2: 42}),
        (['Nhóm yêu cầu'], {1: 26, 2: 20, 3: 42}),
        (['STT', 'Tên bảng'], {2: 45}),
        (['Gói', 'Số lớp tiêu biểu'], {2: 48}),
        (['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử'], {2: 42}),
    ]
    for b in e.blocks:
        if isinstance(b, Paragraph) or not b.rows:
            continue
        head = [c.text.strip() for c in b.rows[0].cells]
        for sig, rules in HEAD_RULES:
            if head[:len(sig)] == sig:
                for ci, limit in rules.items():
                    if ci >= len(b.columns):
                        continue
                    for row in b.rows[1:]:
                        cell = row.cells[ci]
                        txt = cell.text.strip()
                        if len(txt) > limit:
                            set_cell_text(cell, cut(txt, limit))
                break
    e.save()
    print("Đã rút gọn sâu Chương 1/2/3 và Kết luận.")


if __name__ == '__main__':
    main()
