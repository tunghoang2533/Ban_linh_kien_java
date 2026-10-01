package com.banlinhkien.controller.view;

import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ComboViewController {

    private final ProductRepository productRepository;

    @Getter
    @Builder
    public static class ComboItem {
        private String name;
        private String category;
        private String image;
        private BigDecimal price;
        private Long productId; // optional link to DB product
    }

    @Getter
    @Builder
    public static class ComboPackage {
        private String id;
        private String title;
        private String categoryType; // cpu-main, case-psu, ram-ssd, gear
        private String badge;
        private String badgeColor;
        private String description;
        private List<ComboItem> items;
        private BigDecimal originalPrice;
        private BigDecimal comboPrice;
        private BigDecimal savings;
        private int discountPercent;
        private String primaryImage;
        private Long primaryProductId;
    }

    @GetMapping({"/combo", "/combos"})
    public String index(
            @RequestParam(required = false, defaultValue = "all") String category,
            Model model
    ) {
        // Query some real products for fallback IDs if available
        List<Product> sampleProducts = productRepository.findTop12ByIsActiveTrueOrderByCreatedAtDesc();
        Long defaultPid = sampleProducts.isEmpty() ? 1L : sampleProducts.get(0).getId();

        List<ComboPackage> allCombos = new ArrayList<>();

        // Combo 1: CPU + Main + RAM Gaming
        allCombos.add(ComboPackage.builder()
                .id("combo-1")
                .title("Combo Gaming Quốc Dân: i5-13400F + ASUS TUF B760M + RAM 16GB RGB")
                .categoryType("cpu-main")
                .badge("🔥 BÁN CHẠY NHẤT")
                .badgeColor("#ef4444")
                .description("Cặp đôi hiệu năng hoàn hảo cho mọi tựa game AAA và đồ họa trung cấp. Tương thích socket LGA1700, hỗ trợ PCIe 4.0 siêu tốc.")
                .primaryImage("/img/tintuc/tintuc2.png")
                .primaryProductId(sampleProducts.size() > 0 ? sampleProducts.get(0).getId() : defaultPid)
                .originalPrice(new BigDecimal("10200000"))
                .comboPrice(new BigDecimal("8990000"))
                .savings(new BigDecimal("1210000"))
                .discountPercent(12)
                .items(List.of(
                        ComboItem.builder().name("CPU Intel Core i5-13400F (10 Nhân 16 Luồng, Up to 4.6GHz)").category("Bộ vi xử lý (CPU)").price(new BigDecimal("4990000")).image("/img/products/i5.jpg").build(),
                        ComboItem.builder().name("Mainboard ASUS TUF GAMING B760M-PLUS DDR4").category("Bo mạch chủ").price(new BigDecimal("3690000")).image("/img/products/1778571058_6a0293d0cbb2168ec2b4fef0.png").build(),
                        ComboItem.builder().name("RAM Corsair Vengeance RGB PRO 16GB (2x8GB) 3200MHz").category("Bộ nhớ RAM").price(new BigDecimal("1520000")).image("/img/products/1778572386_6a0293d0cbb2168ec2b4fef0.png").build()
                ))
                .build());

        // Combo 2: AMD Ryzen 7 7800X3D + MSI B650 Tomahawk + 32GB DDR5
        allCombos.add(ComboPackage.builder()
                .id("combo-2")
                .title("Combo Đỉnh Cao Đồ Họa & Esport: AMD Ryzen 7 7800X3D + MSI B650 + 32GB DDR5")
                .categoryType("cpu-main")
                .badge("⚡ HIỆU NĂNG TỐI THƯỢNG")
                .badgeColor("#8b5cf6")
                .description("Vua CPU chơi game tốt nhất thế giới với công nghệ 3D V-Cache kết hợp cùng DDR5 6000MHz cho tốc độ khung hình vượt trội.")
                .primaryImage("/img/tintuc/tintuc5.png")
                .primaryProductId(sampleProducts.size() > 1 ? sampleProducts.get(1).getId() : defaultPid)
                .originalPrice(new BigDecimal("18500000"))
                .comboPrice(new BigDecimal("16690000"))
                .savings(new BigDecimal("1810000"))
                .discountPercent(10)
                .items(List.of(
                        ComboItem.builder().name("CPU AMD Ryzen 7 7800X3D (8 Nhân 16 Luồng, 104MB Cache)").category("Bộ vi xử lý (CPU)").price(new BigDecimal("10990000")).image("/img/products/i5.jpg").build(),
                        ComboItem.builder().name("Mainboard MSI MAG B650 TOMAHAWK WIFI AM5").category("Bo mạch chủ").price(new BigDecimal("4990000")).image("/img/products/1778571058_6a0293d0cbb2168ec2b4fef0.png").build(),
                        ComboItem.builder().name("RAM Kingston Fury Beast RGB 32GB (2x16GB) DDR5 6000MHz").category("Bộ nhớ RAM").price(new BigDecimal("2520000")).image("/img/products/1778572386_6a0293d0cbb2168ec2b4fef0.png").build()
                ))
                .build());

        // Combo 3: Case + PSU 750W + Tản AIO
        allCombos.add(ComboPackage.builder()
                .id("combo-3")
                .title("Combo Nguồn Tản & Vỏ Case: Case MSI Forge + Nguồn Corsair RM750e + Tản AIO 240")
                .categoryType("case-psu")
                .badge("❄️ TỐI ƯU NHIỆT ĐỘ")
                .badgeColor("#0ea5e9")
                .description("Giải pháp cấp nguồn chuẩn 80 Plus Gold kết hợp tản nhiệt nước AIO 240mm RGB giúp case luôn mát lạnh, hoạt động êm ái 24/7.")
                .primaryImage("/img/tintuc/tintuc4.png")
                .primaryProductId(sampleProducts.size() > 2 ? sampleProducts.get(2).getId() : defaultPid)
                .originalPrice(new BigDecimal("5500000"))
                .comboPrice(new BigDecimal("4490000"))
                .savings(new BigDecimal("1010000"))
                .discountPercent(18)
                .items(List.of(
                        ComboItem.builder().name("Nguồn máy tính Corsair RM750e 750W 80 Plus Gold Full Modular").category("Nguồn (PSU)").price(new BigDecimal("2690000")).image("/img/chitietsanpham/box.png").build(),
                        ComboItem.builder().name("Vỏ Case MSI MAG FORGE 100R Kèm 3 Fan ARGB").category("Vỏ máy tính").price(new BigDecimal("1290000")).image("/img/products/1778572401_6a0293d0cbb2168ec2b4fef0.png").build(),
                        ComboItem.builder().name("Tản nhiệt nước AIO DeepCool Castle 240EX A-RGB").category("Tản nhiệt").price(new BigDecimal("1520000")).image("/img/products/1780824291_6a25695625bf94a7337b51e0.png").build()
                ))
                .build());

        // Combo 4: RAM + SSD Siêu Tốc
        allCombos.add(ComboPackage.builder()
                .id("combo-4")
                .title("Combo Tăng Tốc Bộ Nhớ: SSD Samsung 990 Pro 1TB NVMe + 32GB RAM DDR5")
                .categoryType("ram-ssd")
                .badge("🚀 TỐC ĐỘ XÉ GIÓ")
                .badgeColor("#10b981")
                .description("Tăng tốc độ load game và khởi động ứng dụng chỉ trong 3 giây với SSD Gen 4 đạt tốc độ 7450MB/s cùng bộ nhớ đa nhiệm 32GB DDR5.")
                .primaryImage("/img/tintuc/tintuc1.png")
                .primaryProductId(sampleProducts.size() > 3 ? sampleProducts.get(3).getId() : defaultPid)
                .originalPrice(new BigDecimal("6200000"))
                .comboPrice(new BigDecimal("5250000"))
                .savings(new BigDecimal("950000"))
                .discountPercent(15)
                .items(List.of(
                        ComboItem.builder().name("Ổ cứng SSD Samsung 990 Pro 1TB PCIe 4.0 NVMe M.2 (7450MB/s)").category("Ổ cứng SSD").price(new BigDecimal("3490000")).image("/img/products/1778572395_6a0293d0cbb2168ec2b4fef0.png").build(),
                        ComboItem.builder().name("RAM Corsair Vengeance RGB 32GB (2x16GB) DDR5 5600MHz").category("Bộ nhớ RAM").price(new BigDecimal("2710000")).image("/img/products/1778572386_6a0293d0cbb2168ec2b4fef0.png").build()
                ))
                .build());

        // Combo 5: Gaming Gear Pro
        allCombos.add(ComboPackage.builder()
                .id("combo-5")
                .title("Combo Gaming Gear Pro: Phím Cơ Akko + Chuột Logitech G502 + Tai Nghe HyperX")
                .categoryType("gear")
                .badge("🎮 BỘ GEAR QUỐC DÂN")
                .badgeColor("#f59e0b")
                .description("Trọn bộ vũ khí gaming chuyên nghiệp cho game thủ FPS và MOBA, mang lại độ chính xác cao và cảm giác gõ bấm hoàn hảo.")
                .primaryImage("/img/tintuc/tintuc3.png")
                .primaryProductId(sampleProducts.size() > 4 ? sampleProducts.get(4).getId() : defaultPid)
                .originalPrice(new BigDecimal("4200000"))
                .comboPrice(new BigDecimal("3390000"))
                .savings(new BigDecimal("810000"))
                .discountPercent(19)
                .items(List.of(
                        ComboItem.builder().name("Bàn phím cơ Akko 3087 v2 Steam Engine Switch Pink").category("Gaming Gear").price(new BigDecimal("1390000")).image("/img/tintuc/tintuc3.png").build(),
                        ComboItem.builder().name("Chuột Gaming Logitech G502 HERO 25K DPI RGB").category("Gaming Gear").price(new BigDecimal("1090000")).image("/img/products/oppo-f9-red-600x600.jpg").build(),
                        ComboItem.builder().name("Tai nghe Gaming HyperX Cloud II Red Surround 7.1").category("Gaming Gear").price(new BigDecimal("1720000")).image("/img/products/oppo-a3s-32gb-600x600.jpg").build()
                ))
                .build());

        // Filter based on category if selected
        List<ComboPackage> filteredCombos;
        if (category != null && !category.equalsIgnoreCase("all")) {
            filteredCombos = allCombos.stream()
                    .filter(c -> c.getCategoryType().equalsIgnoreCase(category))
                    .toList();
        } else {
            filteredCombos = allCombos;
        }

        model.addAttribute("combos", filteredCombos);
        model.addAttribute("allCombosCount", allCombos.size());
        model.addAttribute("selectedCategory", category);

        return "combo/index";
    }
}
