"""Bước 4 – Rút gọn mục 1.3, 1.4, 1.5 của Chương 1."""
import sys
sys.path.insert(0, 'tools')

import report_toolkit as rt

DOC = 'docs/BAO_CAO_DO_AN_PTPMHDV_rut_gon.docx'


def del_prefix(e, prefix, count=None):
    """Xoá các block có nội dung chứa `prefix` (mặc định xoá hết)."""
    idxs = e.find(prefix)
    if count is not None:
        idxs = idxs[:count]
    e.delete_blocks(idxs)
    e.refresh()


def set_by_prefix(e, prefix, segs):
    i = e.find(prefix)[0]
    e.set_para_text(e.blocks[i], segs)
    return i


def del_range_by_prefix(e, start_prefix, end_prefix):
    a = e.find(start_prefix)[0]
    b = e.find(end_prefix)[0]
    e.delete_range(a, b)
    e.refresh()


def drop_table_rows(tbl, first_cells):
    for row in list(tbl.rows):
        key = row.cells[0].text.strip()
        if key in first_cells:
            row._tr.getparent().remove(row._tr)


def main():
    e = rt.ReportEditor(DOC)

    # ======================================================= 1.3.1
    del_prefix(e, 'Lý do lựa chọn: Java được chọn')            # 96w
    set_by_prefix(e, 'Auto-configuration (Tự động cấu hình)', [
        ("Spring Boot giải quyết bài toán cấu hình rườm rà của Spring Framework bằng ba cơ chế "
         "chính: ", None, None),
        ("auto-configuration", True, None),
        (" (tự động cấu hình dựa trên các phụ thuộc có mặt trong classpath), ", None, None),
        ("starter dependencies", True, None),
        (" (nhóm phụ thuộc đã kiểm chứng tương thích) và ", None, None),
        ("externalized configuration", True, None),
        (" (cấu hình tập trung tại application.yml, hỗ trợ ghi đè bằng biến môi trường khi triển "
         "khai).", None, None),
    ])
    del_prefix(e, 'Starter dependencies (Gói phụ thuộc khởi tạo)')
    del_prefix(e, 'Externalized configuration (Cấu hình ngoại vi)')
    for line in ['private final ProductRepository productRepository;',
                 'private final OrderRepository orderRepository;',
                 'private final OrderItemRepository orderItemRepository;',
                 'private final WarehouseLogRepository warehouseLogRepository;',
                 'private final UserAddressRepository userAddressRepository;',
                 'private final VoucherRepository voucherRepository;',
                 'private final VoucherService voucherService;',
                 'private final ShippingService shippingService;']:
        del_prefix(e, line)
    set_by_prefix(e, 'Lựa chọn tiêm qua hàm khởi tạo thay vì tiêm qua trường', [
        ("Tiêm qua hàm khởi tạo mang lại bốn lợi ích: các phụ thuộc là ", None, None),
        ("final", False, True),
        (" nên bất biến sau khởi tạo; đối tượng luôn ở trạng thái hợp lệ ngay khi được tạo; lớp có "
         "thể khởi tạo thủ công trong kiểm thử đơn vị với đối tượng giả lập Mockito mà không cần "
         "container Spring; và phụ thuộc vòng bị phát hiện ngay khi khởi động ứng dụng.", None, None),
    ])
    del_prefix(e, 'Sự phân tách hai loại bộ điều khiển này')

    # ======================================================= 1.3.2
    set_by_prefix(e, 'Mô hình đối tượng (kế thừa, đa hình', [
        ("Mô hình đối tượng (kế thừa, đa hình, tham chiếu, đồ thị đối tượng) và mô hình quan hệ "
         "(bảng, hàng, cột, khóa ngoại, phép nối) có bản chất khác nhau; sự khác biệt này được gọi "
         "là trở kháng đối tượng – quan hệ (object–relational impedance mismatch). ORM là lớp phần "
         "mềm trung gian giải quyết sự khác biệt đó, cho phép lập trình viên làm việc với đồ thị "
         "đối tượng Java trong khi dữ liệu vẫn lưu ở dạng quan hệ.", None, None),
    ])
    for line in ['@ManyToOne(fetch = FetchType.LAZY)',
                 '@JoinColumn(name = "category_id")',
                 'private Category category;',
                 '@JoinColumn(name = "brand_id")',
                 'private Brand brand;']:
        del_prefix(e, line)
    set_by_prefix(e, 'Cơ chế 3 – @EntityGraph', [
        ("Cơ chế 3 – @EntityGraph giải quyết bài toán N+1", True, None),
        (", một trong những vấn đề hiệu năng nghiêm trọng nhất của ORM: khi tải N sản phẩm rồi "
         "truy cập product.getCategory().getName() cho từng phần tử, Hibernate phát sinh thêm N "
         "truy vấn phụ. Khai báo đồ thị thực thể cần tải cùng lúc giúp gộp thành một truy vấn "
         "LEFT JOIN FETCH duy nhất.", None, None),
    ])
    set_by_prefix(e, 'Cơ chế 4 – Specification cho truy vấn động', [
        ("Cơ chế 4 – Specification cho truy vấn động", True, None),
        (": lớp ProductSpecification dùng Criteria API để dựng vị từ (predicate) động tại thời "
         "điểm chạy, phục vụ bộ lọc quản trị nhiều tiêu chí tùy chọn (từ khóa, danh mục, thương "
         "hiệu, trạng thái tồn kho). Cách làm này an toàn tuyệt đối trước tấn công SQL Injection "
         "vì mọi giá trị đều được truyền dưới dạng tham số ràng buộc.", None, None),
    ])
    set_by_prefix(e, 'Chú thích @Lock(LockModeType.PESSIMISTIC_WRITE)', [
        ("Chú thích @Lock(LockModeType.PESSIMISTIC_WRITE) chỉ thị Hibernate thêm mệnh đề FOR UPDATE "
         "vào câu lệnh SELECT. Trên engine InnoDB của MySQL, mệnh đề này đặt khóa ghi độc quyền "
         "trên bản ghi tương ứng; mọi giao dịch khác muốn đọc-để-ghi cùng bản ghi sẽ bị chặn cho "
         "tới khi giao dịch hiện tại kết thúc. Đây là nền tảng của giải pháp chống bán vượt tồn "
         "kho trình bày tại mục 3.2.3.", None, None),
    ])

    # ======================================================= 1.3.3
    set_by_prefix(e, 'Spring Security hoạt động theo mô hình chuỗi bộ lọc', [
        ("Spring Security hoạt động theo mô hình chuỗi bộ lọc servlet (filter chain): "
         "DelegatingFilterProxy được đăng ký vào vòng đời servlet container, chuyển tiếp yêu cầu "
         "tới FilterChainProxy, bộ này lần lượt áp dụng các bộ lọc chuyên trách (quản lý ngữ cảnh "
         "bảo mật, đăng nhập biểu mẫu, đăng xuất, xử lý ngoại lệ và ủy quyền).", None, None),
    ])
    set_by_prefix(e, 'Spring Security 6 (đi kèm Spring Boot 3.x)', [
        ("Spring Security 6 loại bỏ lớp WebSecurityConfigurerAdapter đã lỗi thời, chuyển sang khai "
         "báo bean SecurityFilterChain với cú pháp lambda. Cấu hình rút gọn của hệ thống trong lớp "
         "SecurityConfig:", None, None),
    ])
    for line in ['http.authenticationProvider(authenticationProvider())',
                 '.authorizeHttpRequests(auth -> auth',
                 '.requestMatchers("/login", "/register").permitAll()',
                 '.requestMatchers("/profile/**", "/tai-khoan/**").authenticated()',
                 '.anyRequest().permitAll())',
                 '.formLogin(form -> form',
                 '.loginPage("/login")',
                 '.loginProcessingUrl("/login")',
                 '.successHandler(/* điều hướng theo vai trò */)',
                 '.failureUrl("/login?error=true").permitAll())',
                 '.logout(logout -> logout',
                 '.logoutUrl("/logout")',
                 '.logoutSuccessUrl("/login?logout=true")',
                 '.invalidateHttpSession(true)',
                 '.deleteCookies("JSESSIONID").permitAll())']:
        del_prefix(e, line)
    set_by_prefix(e, 'Quy trình xác thực trong hệ thống diễn ra theo sáu bước', [
        ("Quy trình xác thực trong hệ thống diễn ra theo các bước chính sau:", None, None),
    ])
    del_prefix(e, 'Lớp dịch vụ này kiểm tra cờ is_blocked')
    del_prefix(e, 'Danh sách quyền hạn được xây dựng từ trường users.role')
    del_prefix(e, 'Có hệ số chi phí điều chỉnh được (adaptive cost factor)')
    set_by_prefix(e, 'Một phát hiện kỹ thuật đáng lưu ý của đề tài', [
        ("Một phát hiện kỹ thuật đáng lưu ý: cơ sở dữ liệu của hệ thống được kế thừa từ một ứng "
         "dụng PHP/Laravel trước đó, nơi mật khẩu được băm với tiền tố $2y$. Nhóm đã kiểm chứng "
         "bằng lớp BcryptSpikeTest rằng BCryptPasswordEncoder của Spring Security tự nhận diện và "
         "so khớp thành công cả ba biến thể $2a$, $2b$, $2y$, đồng thời đọc hệ số chi phí từ chính "
         "chuỗi băm. Nhờ đó hệ thống dùng trực tiếp được toàn bộ tài khoản cũ mà không buộc người "
         "dùng đặt lại mật khẩu.", None, None),
    ])
    set_by_prefix(e, 'Hạn chế thứ nhất – Bảo vệ CSRF', [
        ("Hạn chế thứ nhất – bảo vệ CSRF đang bị vô hiệu hóa. ", True, None),
        ("Dòng .csrf(csrf -> csrf.disable()) tắt cơ chế chống giả mạo yêu cầu liên trang, phục vụ "
         "giai đoạn phát triển khi mã JavaScript gọi API bằng Fetch. Vì hệ thống xác thực dựa trên "
         "cookie phiên, đây là rủi ro thực sự: kẻ tấn công có thể dựng trang web độc hại tự động "
         "gửi yêu cầu tới /admin/products/{id}/delete và thao tác sẽ được thực thi nếu quản trị "
         "viên đang có phiên hợp lệ. Khuyến nghị khắc phục: bật lại bảo vệ CSRF và bổ sung token "
         "vào các lời gọi Fetch.", None, None),
    ])
    set_by_prefix(e, 'Hạn chế thứ hai – Quy tắc anyRequest().permitAll()', [
        ("Hạn chế thứ hai – quy tắc anyRequest().permitAll() mở mặc định cho mọi đường dẫn chưa "
         "được liệt kê tường minh, trái với nguyên tắc “từ chối mặc định” (deny by default); cần "
         "thay bằng anyRequest().authenticated() và liệt kê đầy đủ các đường dẫn công khai.",
         None, None),
    ])

    # ======================================================= 1.3.4
    set_by_prefix(e, 'Cấu hình kết nối trong application.yml', [
        ("Cấu hình kết nối trong application.yml khai báo ba nhóm tham số quan trọng: "
         "serverTimezone=Asia/Ho_Chi_Minh để dấu thời gian đơn hàng khớp múi giờ Việt Nam; "
         "characterEncoding=UTF-8 để lưu trữ chính xác tiếng Việt có dấu; và bộ tham số HikariCP "
         "(maximum-pool-size 10, minimum-idle 2, idle-timeout 30000, connection-timeout 20000) "
         "giới hạn số kết nối đồng thời.", None, None),
    ])
    set_by_prefix(e, 'Một cấu hình đáng chú ý khác là spring.jpa.hibernate.ddl-auto', [
        ("Cấu hình spring.jpa.hibernate.ddl-auto: none chỉ thị Hibernate không tự tạo hay sửa đổi "
         "lược đồ; lược đồ được quản lý tường minh bằng tệp db_ban_linh_kien.sql — thực hành đúng "
         "đắn với hệ thống có dữ liệu thật, tránh nguy cơ mất dữ liệu hoặc mất chỉ mục đã tối ưu.",
         None, None),
    ])
    set_by_prefix(e, 'Bên cạnh đó, hệ thống khai báo naming.physical-strategy', [
        ("Chiến lược naming.physical-strategy: CamelCaseToUnderscoresNamingStrategy cho phép tên "
         "thuộc tính Java kiểu camelCase (discountPercent, isActive, createdAt) tự động ánh xạ "
         "sang tên cột snake_case (discount_percent, is_active, created_at) mà không cần khai báo "
         "@Column cho từng trường.", None, None),
    ])
    set_by_prefix(e, 'Nguyên tắc thiết kế chỉ mục ghép', [
        ("Các chỉ mục ghép được thiết kế theo nguyên tắc tiền tố trái nhất (leftmost prefix): cột "
         "có độ chọn lọc cao và xuất hiện trong mệnh đề WHERE đẳng thức đứng trước, cột dùng để "
         "sắp xếp đứng sau, nhờ đó một chỉ mục phục vụ được nhiều truy vấn khác nhau.", None, None),
    ])

    # ======================================================= 1.3.5
    set_by_prefix(e, 'Thymeleaf là template engine phía máy chủ', [
        ("Thymeleaf là template engine phía máy chủ theo triết lý “khuôn mẫu tự nhiên”: tệp khuôn "
         "mẫu là HTML5 hợp lệ, mở được trực tiếp trong trình duyệt ở dạng bản mẫu tĩnh, đồng thời "
         "chứa các thuộc tính th:* được máy chủ xử lý khi kết xuất.", None, None),
    ])
    set_by_prefix(e, 'Trong kiến trúc của đề tài, Thymeleaf đảm nhiệm', [
        ("Trong hệ thống, Thymeleaf kết xuất khung trang và nội dung ổn định (danh mục, chi tiết "
         "sản phẩm, điều hướng), còn nội dung biến động theo tương tác (giỏ hàng, cấu hình PC, "
         "danh sách huyện/xã, kết quả áp mã) được cập nhật bằng lời gọi API bất đồng bộ. Cách phân "
         "công này giúp trang đầu hiển thị nhanh và thân thiện với công cụ tìm kiếm nhờ kết xuất "
         "phía máy chủ, các thao tác kế tiếp mượt mà nhờ cập nhật cục bộ, đồng thời tầng dịch vụ "
         "REST có bên tiêu thụ thực sự để kiểm chứng hợp đồng.", None, None),
    ])
    del_prefix(e, 'templates/layout/storefront.html')
    del_prefix(e, 'templates/layout/admin.html')
    set_by_prefix(e, 'Mỗi trang con khai báo layout:decorate', [
        ("Mỗi trang con khai báo layout:decorate=\"~{layout/storefront}\" và chỉ cung cấp phần nội "
         "dung riêng qua layout:fragment, nhờ đó loại bỏ việc lặp mã HTML khung trang trên 44 tệp "
         "khuôn mẫu của dự án.", None, None),
    ])
    set_by_prefix(e, 'Thư viện thymeleaf-extras-springsecurity6', [
        ("Thư viện thymeleaf-extras-springsecurity6 cung cấp không gian tên sec: cho phép kết xuất "
         "có điều kiện theo quyền hạn ngay trong khuôn mẫu (ví dụ sec:authorize=\"hasRole('ADMIN')\"). "
         "Đây chỉ là biện pháp cải thiện trải nghiệm người dùng, không phải biện pháp bảo mật: việc "
         "kiểm soát truy cập thực sự vẫn do chuỗi bộ lọc Spring Security đảm nhiệm ở phía máy chủ.",
         None, None),
    ])
    set_by_prefix(e, 'Toàn bộ tương tác bất đồng bộ được hiện thực bằng Fetch API', [
        ("Toàn bộ tương tác bất đồng bộ được hiện thực bằng Fetch API — giao diện chuẩn của trình "
         "duyệt dựa trên Promise, thay thế XMLHttpRequest cổ điển; hệ thống không dùng thư viện bên "
         "thứ ba như Axios hay jQuery AJAX nhằm giảm phụ thuộc và giữ mã client gần với chuẩn web.",
         None, None),
    ])

    # ======================================================= 1.3.6
    set_by_prefix(e, 'Cổng thanh toán trung gian giải quyết bài toán tin cậy', [
        ("Cổng thanh toán trung gian giải quyết bài toán tin cậy giữa ba bên: người mua, người bán "
         "và ngân hàng. Website bán hàng không bao giờ tiếp xúc với thông tin thẻ hay thông tin "
         "đăng nhập ngân hàng của khách — dữ liệu nhạy cảm được nhập trực tiếp trên hạ tầng của "
         "VNPAY; website chỉ tham gia ở hai điểm: tạo yêu cầu thanh toán đã ký số và tiếp nhận kết "
         "quả đã ký số.", None, None),
    ])
    set_by_prefix(e, 'HMAC cung cấp đồng thời hai bảo đảm', [
        ("HMAC cung cấp đồng thời hai bảo đảm: tính toàn vẹn — mọi thay đổi dù chỉ một bit trong "
         "thông điệp đều làm giá trị băm thay đổi hoàn toàn; và tính xác thực nguồn gốc — chỉ bên "
         "nắm khóa bí mật mới tạo được chữ ký hợp lệ. Hai bảo đảm này ngăn chặn kịch bản tấn công "
         "điển hình: kẻ tấn công chặn URL chuyển hướng và sửa vnp_Amount từ 25.000.000 xuống "
         "1.000 đồng.", None, None),
    ])
    set_by_prefix(e, 'Thứ tự sắp xếp tham số phải tuyệt đối nhất quán', [
        ("Thứ tự sắp xếp tham số phải tuyệt đối nhất quán. ", True, None),
        ("Cả hai bên đều sắp xếp tên tham số theo thứ tự ASCII tăng dần trước khi nối chuỗi; chỉ "
         "cần một bên sắp xếp khác đi thì chữ ký sẽ không bao giờ khớp — đây là nguyên nhân phổ "
         "biến nhất của lỗi “sai chữ ký” khi tích hợp cổng thanh toán.", None, None),
    ])
    set_by_prefix(e, 'Đơn vị tiền tệ nhân 100', [
        ("Đơn vị tiền tệ nhân 100. ", True, None),
        ("VNPAY quy định vnp_Amount được biểu diễn bằng đơn vị nhỏ nhất, tức số tiền VND nhân 100; "
         "hệ thống thực hiện phép nhân này cả khi tạo yêu cầu lẫn khi đối chiếu số tiền lúc nhận "
         "kết quả.", None, None),
    ])
    set_by_prefix(e, 'Lý do: String.equals()', [
        ("Lý do: String.equals() thoát khỏi vòng lặp ngay tại byte đầu tiên khác biệt, khiến thời "
         "gian thực thi phụ thuộc vào số ký tự đầu trùng khớp; kẻ tấn công có thể khai thác chênh "
         "lệch này để dò từng byte của chữ ký (tấn công phân tích thời gian). "
         "MessageDigest.isEqual() luôn duyệt hết mảng byte nên thời gian thực thi không phụ thuộc "
         "vị trí khác biệt.", None, None),
    ])
    set_by_prefix(e, 'Nguyên tắc thiết kế then chốt', [
        ("Nguyên tắc thiết kế then chốt: không bao giờ tin Return URL là căn cứ duy nhất để ghi "
         "nhận thanh toán thành công. Hệ thống xử lý cả hai kênh qua cùng một phương thức "
         "processPaymentResult() có tính bất biến, nên dù kênh nào đến trước thì kết quả cuối cùng "
         "vẫn đúng và không bị ghi nhận hai lần.", None, None),
    ])

    # ======================================================= 1.3.7 – bảng tổng hợp công nghệ
    big = None
    for b in e.blocks:
        if isinstance(b, rt.Paragraph):
            continue
        if b.rows and b.rows[0].cells[0].text.strip() == 'Thành phần kiến trúc':
            big = b
    if big is not None:
        drop = ['Bể kết nối', 'Tích hợp bảo mật giao diện', 'Khung CSS', 'Xử lý JSON',
                'Kiểm chứng dữ liệu', 'Hàng kiểm thử', 'Kiểm thử bảo mật', 'Kiểm thử tầng web',
                'Công cụ xây dựng', 'Quản lý mã nguồn', 'Kiểm thử API thủ công']
        drop_table_rows(big, drop)

    # ======================================================= 1.4
    set_by_prefix(e, 'Ưu điểm: Danh mục hàng hóa rất rộng, bao phủ cả thiết bị nguyên bộ', [
        ("Ưu điểm: danh mục rất rộng (cả thiết bị nguyên bộ lẫn linh kiện rời), thông tin sản phẩm "
         "chi tiết, chính sách bảo hành minh bạch với mạng lưới trung tâm bảo hành riêng. ", None, None),
        ("Nhược điểm: ", True, None),
        ("công cụ xây dựng cấu hình còn sơ khai, chủ yếu gợi ý các bộ máy dựng sẵn theo phân khúc "
         "giá, chưa cho phép tự ghép linh kiện kèm kiểm tra tương thích tự động.", None, None),
    ])
    del_prefix(e, 'Nhược điểm: Công cụ hỗ trợ xây dựng cấu hình còn sơ khai')
    del_prefix(e, 'Phong Vũ là chuỗi bán lẻ thiết bị công nghệ lâu đời')
    set_by_prefix(e, 'Ưu điểm: Đây là một trong số ít nền tảng trong nước', [
        ("Ưu điểm: một trong số ít nền tảng trong nước có công cụ xây dựng cấu hình tương đối hoàn "
         "chỉnh (chọn linh kiện theo từng nhóm, hiển thị tổng chi phí), thông số kỹ thuật được "
         "chuẩn hóa tốt, giao diện hiện đại. ", None, None),
        ("Nhược điểm: ", True, None),
        ("kiểm tra tương thích chưa mang tính ràng buộc — hệ thống chỉ cảnh báo nhưng vẫn cho phép "
         "tạo cấu hình không hợp lệ; chưa tích hợp đầy đủ chức năng tính tổng công suất tiêu thụ.",
         None, None),
    ])
    del_prefix(e, 'Nhược điểm: Việc kiểm tra tương thích chưa thực sự chặt chẽ')
    del_prefix(e, 'MemoryZone định vị là nhà bán lẻ chuyên sâu')
    set_by_prefix(e, 'Ưu điểm: Có công cụ Build PC cho phép chọn linh kiện theo từng khe', [
        ("Ưu điểm: có công cụ Build PC chọn linh kiện theo từng khe kèm dịch vụ lắp ráp, kiểm thử "
         "trước khi giao hàng; danh mục linh kiện chuyên dụng phong phú (máy chủ, máy trạm). ",
         None, None),
        ("Nhược điểm: ", True, None),
        ("giao diện phức tạp, mật độ thông tin dày đặc; luồng ráp cấu hình còn nhiều bước thủ công "
         "và chưa tự động lọc bỏ linh kiện không tương thích.", None, None),
    ])
    del_prefix(e, 'Nhược điểm: Giao diện người dùng tương đối phức tạp')
    del_prefix(e, 'HACOM là nhà phân phối và bán lẻ có thế mạnh')
    set_by_prefix(e, 'Nhận định thứ nhất', [
        ("Nhận định thứ nhất: ", True, None),
        ("các nền tảng thương mại có lợi thế áp đảo về quy mô dữ liệu, độ phủ hàng hóa, hạ tầng và "
         "mạng lưới hậu cần — những lợi thế mà một đồ án môn học không thể và không nên cạnh tranh.",
         None, None),
    ])
    set_by_prefix(e, 'Nhận định thứ hai', [
        ("Nhận định thứ hai: ", True, None),
        ("tất cả các nền tảng khảo sát đều có khoảng trống chung về hỗ trợ ra quyết định kỹ thuật. "
         "Không nền tảng nào cung cấp đồng thời ba năng lực: ràng buộc tương thích cứng ở mức "
         "không cho phép tạo cấu hình sai; tính tổng công suất tiêu thụ theo thời gian thực; và "
         "gợi ý cấu hình tự động theo ngân sách, mục đích sử dụng.", None, None),
    ])
    set_by_prefix(e, 'Nhận định thứ ba', [
        ("Nhận định thứ ba: ", True, None),
        ("không nền tảng nào công bố hợp đồng dịch vụ cho bên thứ ba, phản ánh kiến trúc đóng, khó "
         "mở rộng kênh bán và khó tích hợp với hệ sinh thái đối tác.", None, None),
    ])

    # ======================================================= 1.5
    set_by_prefix(e, 'Thứ nhất, chương đã xác lập cơ sở thực tiễn', [
        ("Thứ nhất, chương đã xác lập cơ sở thực tiễn của đề tài: bài toán bán lẻ linh kiện máy "
         "tính trực tuyến có bốn đặc thù khiến nó khác biệt căn bản so với thương mại điện tử "
         "thông thường — sản phẩm ràng buộc kỹ thuật lẫn nhau, không gian thuộc tính lớn và không "
         "đồng nhất, giá trị đơn hàng cao đi kèm tồn kho mỏng, và rủi ro thanh toán.", None, None),
    ])
    set_by_prefix(e, 'Thứ hai, chương đã hệ thống hóa cơ sở lý thuyết', [
        ("Thứ hai, chương đã hệ thống hóa cơ sở lý thuyết: tám nguyên lý SOA, sáu ràng buộc của "
         "chuẩn REST và quy chuẩn thiết kế tài nguyên được phân tích gắn với minh chứng cụ thể "
         "trong mã nguồn (CartService được nhiều bên tiêu thụ dùng lại, CheckoutService là dịch vụ "
         "tổ hợp, hệ thống đạt mức 2 của mô hình trưởng thành Richardson).", None, None),
    ])
    set_by_prefix(e, 'Những nội dung này tạo thành khung tham chiếu cho Chương 2', [
        ("Những nội dung này tạo thành khung tham chiếu cho Chương 2, nơi các yêu cầu được đặc tả "
         "hình thức và hệ thống được thiết kế chi tiết từ mô hình nghiệp vụ, kiến trúc phân tầng, "
         "lược đồ dữ liệu, hợp đồng dịch vụ cho tới ứng dụng client.", None, None),
    ])

    e.save()
    print("Đã rút gọn mục 1.3 – 1.5.")


if __name__ == '__main__':
    main()
