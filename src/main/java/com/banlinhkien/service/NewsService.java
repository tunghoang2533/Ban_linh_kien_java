package com.banlinhkien.service;

import com.banlinhkien.entity.NewsArticle;
import com.banlinhkien.repository.NewsArticleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsArticleRepository newsArticleRepository;

    @PostConstruct
    public void initDefaultNews() {
        try {
            if (newsArticleRepository.count() == 0) {
                log.info("Khởi tạo danh sách tin tức mẫu công nghệ linh kiện...");
                NewsArticle n1 = NewsArticle.builder()
                        .title("Đánh giá chi tiết RTX 4070 Super: Chiến mượt mọi tựa game 2K & 4K Ray Tracing")
                        .slug("danh-gia-chi-tiet-rtx-4070-super")
                        .thumbnail("/img/tintuc/tintuc1.png")
                        .summary("Card đồ họa NVIDIA GeForce RTX 4070 Super mang lại hiệu năng vượt trội hơn 20% so với thế hệ tiền nhiệm, mức giá hấp dẫn cho game thủ.")
                        .content("<p>NVIDIA GeForce RTX 4070 Super là bản nâng cấp đáng giá nhất trong phân khúc tầm trung - cao cấp hiện nay. Sở hữu kiến trúc Ada Lovelace tân tiến cùng công nghệ DLSS 3.5 Frame Generation, card đồ họa này dễ dàng cán mốc 100+ FPS ở độ phân giải 2K max settings.</p><h3>1. Thông số kỹ thuật ấn tượng</h3><p>Được trang bị 7168 nhân CUDA, bộ nhớ 12GB GDDR6X băng thông cao và mức tiêu thụ điện năng chỉ 220W, RTX 4070 Super tối ưu hoàn hảo giữa hiệu năng và nhiệt độ hoạt động.</p><h3>2. Hiệu năng thực tế qua các bài test</h3><p>Trong tựa game Cyberpunk 2077 bật Ray Tracing Overdrive, sự hỗ trợ từ DLSS 3 giúp FPS tăng gấp đôi từ 38 FPS lên tới hơn 85 FPS mượt mà.</p><p>Đây là sự lựa chọn không thể bỏ qua cho các cấu hình PC Gaming và Đồ họa chuyên nghiệp trong năm nay.</p>")
                        .metaTitle("Đánh giá chi tiết RTX 4070 Super")
                        .metaDescription("Đánh giá hiệu năng chi tiết RTX 4070 Super với các tựa game AAA")
                        .isPublished(true)
                        .publishedAt(LocalDateTime.now().minusDays(1))
                        .build();

                NewsArticle n2 = NewsArticle.builder()
                        .title("Hướng dẫn tự Build PC Gaming từ A-Z cho người mới bắt đầu năm 2026")
                        .slug("huong-dan-tu-build-pc-gaming-tu-a-z")
                        .thumbnail("/img/tintuc/tintuc2.png")
                        .summary("Từng bước lựa chọn CPU, Mainboard, RAM, VGA, Nguồn PSU và cách lắp ráp an toàn tại nhà mà không lo hỏng hóc linh kiện.")
                        .content("<p>Tự tay lắp ráp một cỗ máy PC theo sở thích là trải nghiệm tuyệt vời của mọi tín đồ công nghệ. Dưới đây là các bước cơ bản giúp bạn tự tin hoàn thiện dàn máy của mình:</p><h3>Bước 1: Xác định ngân sách và nhu cầu</h3><p>Xác định bạn cần máy để chơi game Esports, game AAA, đồ họa 3D hay làm việc văn phòng đa nhiệm để phân bổ chi phí hợp lý nhất.</p><h3>Bước 2: Chọn CPU và Bo Mạch Chủ (Mainboard) tương thích</h3><p>Chú ý Socket tương thích (ví dụ LGA1700 cho Intel Gen 12/13/14, AM5 cho AMD Ryzen 7000/8000 series).</p><h3>Bước 3: Chọn Nguồn PSU đạt chuẩn 80 Plus</h3><p>Nguồn là trái tim của hệ thống. Luôn chọn nguồn từ các thương hiệu uy tín như Corsair, Seasonic, MSI với công suất dư tải 20-30%.</p>")
                        .metaTitle("Hướng dẫn tự Build PC Gaming từ A-Z")
                        .metaDescription("Cẩm nang tự build PC gaming chi tiết và an toàn nhất")
                        .isPublished(true)
                        .publishedAt(LocalDateTime.now().minusDays(2))
                        .build();

                NewsArticle n3 = NewsArticle.builder()
                        .title("Top 5 Mẫu Bàn Phím Cơ và Chuột Gaming Đáng Mua Nhất Phân Khúc 1 Triệu")
                        .slug("top-5-ban-phim-co-chuot-gaming-dang-mua-nhat")
                        .thumbnail("/img/tintuc/tintuc3.png")
                        .summary("Tổng hợp các mẫu gaming gear bền bỉ, switch cơ học gõ êm, đèn LED RGB rực rỡ và mắt đọc quang học chuẩn xác.")
                        .content("<p>Phụ kiện gear đóng vai trò quyết định đến cảm giác trải nghiệm và thao tác của game thủ. Dưới đây là top 5 combo gear giá tốt bán chạy nhất tại Bán Linh Kiện:</p><ul><li><strong>Bàn phím Akko 3087:</strong> Switch v3 gõ cực đã, keycap PBT siêu bền.</li><li><strong>Chuột Logitech G102 Lightsync:</strong> Thiết kế công thái học quốc dân, LED RGB 16.8 triệu màu.</li><li><strong>Bàn phím cơ DareU EK87:</strong> Giá học sinh sinh viên, switch quang cơ chống bụi nước.</li><li><strong>Chuột Razer DeathAdder Essential:</strong> Mắt đọc 6400 DPI chính xác cho game FPS.</li></ul>")
                        .metaTitle("Top 5 Bàn Phím Chuột Gaming Giá Rẻ")
                        .metaDescription("Bàn phím cơ và chuột gaming tốt nhất dưới 1 triệu")
                        .isPublished(true)
                        .publishedAt(LocalDateTime.now().minusDays(3))
                        .build();

                NewsArticle n4 = NewsArticle.builder()
                        .title("Chương trình Tri Ân Khách Hàng: Giảm đến 40% & Tặng Voucher 500K tháng này")
                        .slug("chuong-trinh-tri-an-khach-hang-thang-nay")
                        .thumbnail("/img/tintuc/tintuc4.png")
                        .summary("Hàng loạt linh kiện CPU, RAM, SSD, VGA chính hãng giảm giá sốc kèm quà tặng chuột không dây, bàn di chuột cỡ lớn.")
                        .content("<p>Nhằm tri ân sự ủng hộ của quý khách hàng, Bán Linh Kiện tưng bừng khởi động chuỗi ưu đãi lớn nhất tháng:</p><ul><li>Giảm trực tiếp tới 40% cho các dòng Ổ cứng SSD NVMe Gen 4 tốc độ cao.</li><li>Tặng ngay Voucher 500.000₫ cho đơn hàng Build PC trọn bộ từ 15 triệu đồng.</li><li>Miễn phí vận chuyển toàn quốc cho đơn hàng thanh toán online qua VNPay.</li><li>Bảo hành 1 đổi 1 trong vòng 30 ngày đầu tiên nếu phát sinh lỗi nhà sản xuất.</li></ul><p>Hãy nhanh tay ghé thăm gian hàng hoặc đặt online ngay hôm nay để nhận trọn vẹn ưu đãi!</p>")
                        .metaTitle("Khuyến Mãi Tri Ân Khách Hàng Tháng Này")
                        .metaDescription("Siêu sale linh kiện máy tính giảm đến 40%")
                        .isPublished(true)
                        .publishedAt(LocalDateTime.now().minusDays(4))
                        .build();

                NewsArticle n5 = NewsArticle.builder()
                        .title("So sánh RAM DDR4 và DDR5: Có thực sự đáng để bạn nâng cấp ngay lúc này?")
                        .slug("so-sanh-ram-ddr4-va-ddr5")
                        .thumbnail("/img/tintuc/tintuc5.png")
                        .summary("Phân tích chi tiết về tốc độ băng thông, độ trễ CL, mức giá thành và sự chênh lệch hiệu năng thực tế khi chơi game.")
                        .content("<p>Với việc giá RAM DDR5 ngày càng hạ nhiệt tiệm cận với DDR4, câu hỏi liệu có nên đầu tư nền tảng DDR5 cho dàn máy mới được rất nhiều anh em quan tâm.</p><h3>1. Băng thông vượt trội</h3><p>DDR5 khởi điểm từ mức bus 4800MHz - 6000MHz so với mức 3200MHz phổ thông của DDR4, giúp tăng băng thông truyền tải dữ liệu gần gấp đôi.</p><h3>2. Khi nào bạn nên chọn DDR5?</h3><p>Nếu bạn xây dựng cấu hình mới với Intel Gen 14 hoặc AMD Ryzen 7000/9000 series, DDR5 là sự đầu tư đón đầu công nghệ hợp lý nhất để máy hoạt động lâu dài 4-5 năm tới.</p>")
                        .metaTitle("So sánh RAM DDR4 vs DDR5")
                        .metaDescription("Nên mua RAM DDR4 hay DDR5 thời điểm hiện tại")
                        .isPublished(true)
                        .publishedAt(LocalDateTime.now().minusDays(5))
                        .build();

                newsArticleRepository.saveAll(List.of(n1, n2, n3, n4, n5));
                log.info("Khởi tạo thành công 5 bài viết tin tức mẫu.");
            }
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo tin tức mẫu: {}", e.getMessage());
        }
    }

    public List<NewsArticle> getAllPublishedNews() {
        return newsArticleRepository.findByIsPublishedTrueOrderByPublishedAtDesc();
    }

    public Page<NewsArticle> getNewsPage(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return newsArticleRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
                    keyword.trim(), keyword.trim(), pageable
            );
        }
        return newsArticleRepository.findByIsPublishedTrueOrderByPublishedAtDesc(pageable);
    }

    public Optional<NewsArticle> getNewsByIdOrSlug(String idOrSlug) {
        try {
            Long id = Long.parseLong(idOrSlug);
            Optional<NewsArticle> article = newsArticleRepository.findById(id);
            if (article.isPresent()) return article;
        } catch (NumberFormatException ignored) {}
        return newsArticleRepository.findBySlug(idOrSlug);
    }

    public List<NewsArticle> getLatestNews(int count) {
        return newsArticleRepository.findByIsPublishedTrueOrderByPublishedAtDesc()
                .stream().limit(count).toList();
    }

    @Transactional
    public NewsArticle save(NewsArticle article) {
        return newsArticleRepository.save(article);
    }

    @Transactional
    public void delete(Long id) {
        newsArticleRepository.deleteById(id);
    }
}
