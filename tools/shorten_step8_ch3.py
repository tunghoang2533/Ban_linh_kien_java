"""Bước 8 – Rút gọn Chương 3 (kết quả thực nghiệm)."""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt
from report_toolkit import (ReportEditor, Paragraph, set_cell_text, drop_columns,
                            set_col_fractions, trim_steps)

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'

# Dòng cây cấu trúc gói cần xoá (giữ lại các dòng tiêu biểu)
TREE_DROP = [
    '│', '│   ├── WebConfig.java                   — ResourceHandler cho tài nguyên tĩnh và thư mục tải lên',
    '│   └── ...', '│   │', '│   │   ├── PcBuilderApiController.java  — /api/build-pc/**',
    '│   │   ├── VoucherApiController.java    — /api/voucher/**',
    '│   │   ├── LocationApiController.java   — /api/location/**',
    '│   │   ├── ProductApiController.java    — /api/san-pham/**',
    '│   │   └── VnpayApiController.java      — /api/vnpay/ipn',
    '│       ├── CartViewController.java      — /gio-hang, /cart',
    '│       ├── CheckoutViewController.java  — /thanh-toan, /dat-hang-thanh-cong/{id}',
    '│       ├── BuildPcViewController.java   — /build-pc/**',
    '│       ├── AuthViewController.java      — /login, /register',
    '│       ├── AccountViewController.java   — /tai-khoan, /tai-khoan/orders',
    '│       ├── VnpayViewController.java     — /vnpay/payment/{id}, /vnpay/return',
    '│       └── admin/                       — Phân hệ quản trị',
    '│           ├── AdminDashboardController.java',
    '│           ├── AdminProductController.java',
    '│           ├── AdminOrderController.java',
    '│           ├── AdminCategoryController.java',
    '│           ├── AdminBrandController.java',
    '│           └── AdminFeaturesController.java',
    '│   ├── PcBuilderAiService.java          — Gợi ý cấu hình lai luật và mô hình ngôn ngữ',
    '│   ├── VoucherService.java              — Xác thực và tính mức giảm giá',
    '│   ├── ShippingService.java             — Tính phí vận chuyển theo vùng',
    '│   ├── VnpayService.java                — Ký và xác minh HMAC-SHA512',
    '│   ├── AdminDashboardService.java       — Tổng hợp chỉ số quản trị',
    '│   ├── ProductSpecRepository.java       — findSpecValue phục vụ kiểm tra tương thích',
    '│   ├── OrderRepository.java',
    '│   ├── OrderItemRepository.java',
    '│   ├── OrderStatusHistoryRepository.java',
    '│   ├── PaymentTransactionRepository.java',
    '│   ├── UserAddressRepository.java',
    '│   ├── VoucherRepository.java',
    '│   ├── WarehouseLogRepository.java',
    '│   ├── ShippingZoneRepository.java',
    '│   ├── CategoryRepository.java',
    '│   ├── BrandRepository.java',
    '│   └── ProductCommentRepository.java',
    '│   │                                          VoucherType, WarehouseLogType',
    # các dòng mã nguồn 3.2.3
    '@Query("SELECT p FROM Product p WHERE p.id = :id")',
    'public Order placeOrder(CheckoutRequest request, CartDto cart, HttpSession session) {',
    'Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();',
    'for (CartItemDto item : cart.getItems()) {',
    'requestedQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);',
    'List<Long> sortedProductIds = requestedQuantities.keySet()',
    '.stream().sorted().toList();',
    'Map<Long, Product> lockedProducts = new LinkedHashMap<>();',
    'for (Long productId : sortedProductIds) {',
    'Product product = productRepository.findByIdForUpdate(productId)',
    '.orElseThrow(() -> new IllegalArgumentException(',
    '"Sản phẩm không tồn tại: " + productId));',
    'if (Boolean.FALSE.equals(product.getIsActive())) {',
    'throw new IllegalArgumentException(',
    '"Sản phẩm đã ngừng kinh doanh: " + product.getName());',
    'int requested = requestedQuantities.get(productId);',
    'if (product.getQuantity() == null || product.getQuantity() < requested) {',
    'throw new InsufficientStockException(String.format(',
    '"Sản phẩm \'%s\' chỉ còn %d sản phẩm trong kho",',
    'product.getName(),',
    'product.getQuantity() == null ? 0 : product.getQuantity()));',
    'product.setQuantity(product.getQuantity() - requested);',
    'lockedProducts.put(productId, product);',
    '// ... các bước 5 đến 9',
    'SELECT p.id, p.category_id, p.brand_id, p.name, p.price, p.quantity, ...',
    'FROM products p',
    'WHERE p.id = ?',
    'FOR UPDATE;',
    # JSON ví dụ ở 3.2.2
    '"success": false,',
    '"message": "Sản phẩm \'CPU Intel Core i5-13400F\' chỉ còn 2 sản phẩm trong kho",',
    '"cart_count": 1,',
    '"subtotal": 18010000',
    '{',
    '}',
]

DEL = [
    'Quy tắc nghiệp vụ được thực thi: BR-01 (giá do máy chủ tính',
    'Quan hệ phụ thuộc ba cấp giữa ba endpoint này',
    'Về tính khả đệm: Endpoint này trả về dữ liệu có thể thay đổi (giá khuyến mại, tồn kho)',
    'Ghi chú về việc sử dụng động từ GET cho một thao tác biến đổi trạng thái',
    'Giao dịch A thực thi SELECT ... FOR UPDATE trên bản ghi có định danh 101',
    'Giao dịch A tiếp tục thực hiện các bước nghiệp vụ còn lại và cuối cùng thực thi COMMIT',
    'Nếu tồn kho không còn đủ, B ném InsufficientStockException',
    'Điểm mấu chốt là bước 4: nhờ khóa, giao dịch B không thể đọc được giá trị cũ',
    'Tỷ lệ mã kiểm thử trên mã nghiệp vụ đạt khoảng 24,3%',
    'Thứ hai – Bảo mật thanh toán đạt chuẩn thực tiễn công nghiệp',
    'Thứ tư – Nghiệp vụ đặc thù tạo giá trị khác biệt thực sự',
    'Thứ sáu – Khả năng truy vết và kiểm toán đầy đủ',
]

REPL = {
    'Mục này là đặc tả hợp đồng dịch vụ đầy đủ của hệ thống':
        "Mục này đặc tả hợp đồng dịch vụ đầy đủ của hệ thống: mỗi nhóm tài nguyên được trình bày "
        "trong một bảng riêng gồm phương thức, đường dẫn, chức năng, tham số và phản hồi chính; "
        "phần cuối là bảng tổng hợp toàn bộ endpoint công bố ra bên ngoài.",
    'Điểm đáng lưu ý về endpoint gợi ý cấu hình':
        "Điểm đáng lưu ý ở endpoint gợi ý cấu hình: trường source trong phản hồi cho biết kết quả "
        "được sinh bởi bộ luật thuần túy hay có sự tham gia của mô hình ngôn ngữ, giúp kiểm chứng "
        "vai trò chỉ-diễn-giải của mô hình.",
    'Lưu ý về mã trạng thái':
        "Lưu ý về mã trạng thái: kể cả khi mã giảm giá không áp dụng được, endpoint vẫn trả 200 OK "
        "kèm success: false — nhất quán với quy ước phân biệt lỗi giao thức và kết quả nghiệp vụ "
        "không thuận lợi đã nêu tại mục 1.2.3.",
    'Đặc điểm kiến trúc của nhóm tài nguyên này':
        "Đặc điểm kiến trúc: đây là nhóm endpoint phi trạng thái hoàn toàn và có khả năng lưu đệm "
        "cao nhất trong toàn hệ thống vì dữ liệu địa giới hành chính hầu như không thay đổi.",
    'Điểm quan trọng về bảo mật dữ liệu: DTO ProductQuickViewDto':
        "Điểm quan trọng về bảo mật dữ liệu: DTO ProductQuickViewDto cố ý không chứa trường "
        "cost_price (giá vốn) — nếu tuần tự hóa entity Product trực tiếp, thông tin kinh doanh mật "
        "này sẽ bị phơi bày qua API.",
    'Lý do chọn khóa bi quan':
        "Lý do chọn khóa bi quan: nghiệp vụ đặt hàng là nghiệp vụ nhiều bước (kiểm tra nhiều sản "
        "phẩm, tính phí, áp mã, ghi đơn), nên nếu dùng khóa lạc quan thì mọi bước đều phải viết "
        "lại khi xảy ra xung đột; khóa bi quan bảo đảm tính đúng đắn tuyệt đối với chi phí chỉ là "
        "giảm thông lượng khi tranh chấp cao — mức đánh đổi phù hợp với quy mô hệ thống.",
    'Trên engine InnoDB, mệnh đề FOR UPDATE':
        "Trên engine InnoDB, mệnh đề FOR UPDATE tạo khóa hàng độc quyền kèm khóa ý định trên bảng: "
        "giao dịch B thực thi cùng câu lệnh trên cùng bản ghi sẽ bị chặn cho tới khi A COMMIT, sau "
        "đó B đọc lại và nhìn thấy giá trị tồn kho mới — điều kiện tranh chấp bị loại bỏ hoàn toàn.",
    'Đặc điểm thiết kế đáng lưu ý: Ca kiểm thử gọi trực tiếp phương thức của tầng dịch vụ':
        "Đặc điểm thiết kế đáng lưu ý: ca kiểm thử gọi trực tiếp phương thức tầng dịch vụ (bỏ qua "
        "tầng HTTP) nhằm cô lập chính xác cơ chế khóa bi quan; nếu kiểm thử qua HTTP, kết quả có "
        "thể bị ảnh hưởng bởi nhiều yếu tố khác ngoài khóa.",
    'Ghi chú quan trọng về sai lệch so với đặc tả ban đầu':
        "Ghi chú về sai lệch so với đặc tả ban đầu: đề bài mô tả kịch bản “10 luồng đồng thời mua "
        "một sản phẩm còn 2 đơn vị”. Trong thực tế, cơ chế khóa bi quan chỉ cho phép đúng 2 giao "
        "dịch thành công và 8 giao dịch còn lại nhận InsufficientStockException; đây là kết quả "
        "đúng theo thiết kế, không phải lỗi, và là minh chứng trực tiếp cho yêu cầu NFR-01.",
    'Ba kết luận rút ra từ kết quả':
        "Ba kết luận rút ra từ kết quả kiểm thử đồng thời:",
    'Cơ chế khóa bi quan hoạt động đúng như thiết kế':
        "Cơ chế khóa bi quan hoạt động đúng như thiết kế: với 10 luồng cùng tấn công một bản ghi "
        "có tồn kho 2, đúng 2 đơn được tạo và 8 đơn bị từ chối với thông điệp tồn kho không đủ.",
    'Tồn kho không bao giờ nhận giá trị âm':
        "Tồn kho không bao giờ nhận giá trị âm: khẳng định assertEquals(0, ...) được thực hiện sau "
        "khi cả 10 luồng kết thúc, chứng minh bất biến dữ liệu được giữ vững.",
    'Không có bế tắc và không có hết thời gian chờ khóa':
        "Không có bế tắc và không có hết thời gian chờ khóa: danh sách ngoại lệ rỗng, nghĩa là 8 "
        "luồng thất bại một cách “sạch” do kiểm tra nghiệp vụ chứ không do khóa chết.",
    'Trên cơ sở kết quả thực nghiệm đã trình bày':
        "Trên cơ sở kết quả thực nghiệm, hệ thống đạt được bốn ưu điểm nổi bật, mỗi ưu điểm đều "
        "gắn với minh chứng cụ thể:",
    'Thứ nhất – Tính toàn vẹn dữ liệu được bảo đảm và kiểm chứng hình thức':
        "Thứ nhất – Tính toàn vẹn dữ liệu được bảo đảm và kiểm chứng hình thức. Khóa bi quan theo "
        "thứ tự tăng dần kết hợp giao dịch nguyên tử giúp hệ thống không bao giờ bán vượt tồn kho; "
        "kết quả được chứng minh bằng CheckoutConcurrencyTest với 10 luồng tranh chấp trên 2 đơn "
        "vị hàng.",
    'Thứ ba – Hợp đồng dịch vụ được thiết kế và đặc tả bài bản':
        "Thứ hai – Hợp đồng dịch vụ được thiết kế và đặc tả bài bản. Hệ thống công bố 17 endpoint "
        "REST thuộc sáu nhóm tài nguyên với cấu trúc phản hồi thống nhất, DTO tách biệt khỏi entity "
        "và quy ước lỗi rõ ràng — đúng trọng tâm của học phần Phát triển phần mềm hướng dịch vụ.",
    'Thứ năm – Kiến trúc phân tầng nghiêm ngặt và khả năng kiểm thử cao':
        "Thứ ba – Bảo mật và khả năng kiểm thử. Tích hợp VNPAY được bảo vệ bằng ba lớp (chữ ký "
        "HMAC-SHA512, đối chiếu số tiền, tính bất biến); kiến trúc phân tầng với tiêm phụ thuộc qua "
        "hàm khởi tạo cho phép 55 ca kiểm thử tự động chạy nhanh và độc lập với cơ sở dữ liệu.",
}

# Các bảng cần bỏ bớt cột: (header đầu tiên, các cột cần bỏ)
TABLE_FIX = [
    (['Method', 'Endpoint', 'Chức năng'], [5, 3]),          # bỏ Status Code, Request Body/Param
    (['Mã RspCode'], [4]),                                  # bỏ Hành động của VNPAY
    (['STT', 'Method', 'Endpoint'], [6, 5, 4]),             # bảng tổng hợp hợp đồng
    (['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử'], [4]),       # UT: bỏ Endpoint? (không có) -> bỏ Kết quả? giữ
    (['Test ID'], [3, 4]),                                  # ma trận TC: bỏ Dữ liệu đầu vào, Kết quả thực tế
    (['Ký hiệu', 'Màn hình'], [3, 2]),                      # bảng màn hình: bỏ Nội dung, Điểm kỹ thuật
    (['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử', 'Endpoint liên quan'], [2]),
]


def main():
    e = ReportEditor(DOC)

    for line in TREE_DROP:
        idx = e.find(line, exact=True)
        if idx:
            e.delete_blocks(idx)
    for prefix in DEL:
        idx = e.find(prefix)
        if idx:
            e.delete_blocks(idx)
    for prefix, new in REPL.items():
        idx = e.find(prefix)
        if idx:
            e.set_para_text(e.blocks[idx[0]], [(new, None, None)])
    e.refresh()

    # ---- xử lý các bảng
    for b in e.blocks:
        if isinstance(b, Paragraph):
            continue
        head = [c.text.strip() for c in b.rows[0].cells] if b.rows else []
        if not head:
            continue
        if head[:3] == ['Method', 'Endpoint', 'Chức năng']:
            drop_columns(b, [5, 3])
            set_col_fractions(b, [0.10, 0.30, 0.30, 0.30])
            for row in b.rows[1:]:
                cell = row.cells[-1]
                txt = cell.text.strip()
                if len(txt) > 110:
                    cut = txt[:110]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')
        elif head[:1] == ['Mã RspCode']:
            drop_columns(b, [4])
            set_col_fractions(b, [0.10, 0.20, 0.36, 0.34])
        elif head[:3] == ['STT', 'Method', 'Endpoint']:
            drop_columns(b, [6, 5, 4])
            set_col_fractions(b, [0.07, 0.12, 0.47, 0.34])
        elif head[:4] == ['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử', 'Endpoint liên quan']:
            drop_columns(b, [5, 3])
            set_col_fractions(b, [0.07, 0.16, 0.42, 0.35])
            for row in b.rows[1:]:
                cell = row.cells[2]
                txt = cell.text.strip()
                if len(txt) > 100:
                    cut = txt[:100]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')
        elif head[:3] == ['Mã', 'Lớp kiểm thử', 'Tên ca kiểm thử']:
            set_col_fractions(b, [0.07, 0.16, 0.42, 0.20, 0.15])
        elif head[:1] == ['Test ID']:
            drop_columns(b, [3, 4])
            set_col_fractions(b, [0.09, 0.35, 0.40, 0.16])
            for row in b.rows[1:]:
                cell = row.cells[2]
                txt = cell.text.strip()
                if len(txt) > 90:
                    cut = txt[:90]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')
        elif head[:2] == ['Ký hiệu', 'Màn hình']:
            drop_columns(b, [3, 2])
            set_col_fractions(b, [0.25, 0.75])
        elif head[:2] == ['Nhóm yêu cầu', 'Yêu cầu cụ thể']:
            set_col_fractions(b, [0.22, 0.26, 0.18, 0.34])
            for row in b.rows[1:]:
                cell = row.cells[-1]
                txt = cell.text.strip()
                if len(txt) > 80:
                    cut = txt[:80]
                    cut = cut[:cut.rfind(' ')]
                    set_cell_text(cell, cut.rstrip('.,;') + '…')
        elif head[:1] == ['Mã'] and 'Hạn chế' in head and len(b.columns) >= 5:
            set_col_fractions(b, [0.07, 0.22, 0.30, 0.15, 0.26])
            for row in b.rows[1:]:
                for ci in (2, 4):
                    cell = row.cells[ci]
                    txt = cell.text.strip()
                    if len(txt) > 80:
                        cut = txt[:80]
                        cut = cut[:cut.rfind(' ')]
                        set_cell_text(cell, cut.rstrip('.,;') + '…')

    # ---- 3.5.3 tổng kết chương: gộp bốn đoạn thành hai
    e.refresh()
    idx = e.find('Về hợp đồng dịch vụ, chương đã đặc tả chi tiết 17 endpoint REST')
    if idx:
        e.set_para_text(e.blocks[idx[0]], [
            ("Về hợp đồng dịch vụ, chương đã đặc tả 17 endpoint REST thuộc sáu nhóm tài nguyên với "
             "đầy đủ phương thức, tham số, phản hồi và mã trạng thái; về cơ chế toàn vẹn, chương đã "
             "phân tích bài toán bán vượt tồn kho từ gốc, so sánh ba phương án giải quyết và trình "
             "bày mã nguồn hiện thực khóa bi quan.", None, None),
        ])
    idx = e.find('Về kiểm thử, 55 ca kiểm thử tự động trên 11 lớp đều đạt')
    if idx:
        e.set_para_text(e.blocks[idx[0]], [
            ("Về kiểm thử, 55 ca kiểm thử tự động trên 11 lớp đều đạt, 35 kịch bản kiểm thử hệ "
             "thống đều đạt, và đặc biệt ca kiểm thử đồng thời đã chứng minh tồn kho không bao giờ "
             "âm khi 10 luồng tranh chấp cùng một bản ghi.", None, None),
        ])
    e.save()
    print("Đã rút gọn Chương 3.")


if __name__ == '__main__':
    main()
