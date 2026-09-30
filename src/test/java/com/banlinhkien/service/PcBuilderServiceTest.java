package com.banlinhkien.service;

import com.banlinhkien.dto.AiRecommendationResponseDto;
import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.PcBuilderSessionDto;
import com.banlinhkien.entity.Product;
import com.banlinhkien.entity.ProductSpec;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.repository.ProductSpecRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class PcBuilderServiceTest {

    @Autowired
    private PcBuilderService pcBuilderService;

    @Autowired
    private PcBuilderAiService pcBuilderAiService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSpecRepository productSpecRepository;

    private Product cpuIntel;
    private Product mbIntel;
    private Product mbAmd;
    private ProductSpec cpuIntelSpec;
    private ProductSpec mbIntelSpec;
    private ProductSpec mbAmdSpec;

    @BeforeEach
    void setUp() {
        // Create test CPU Intel (LGA 1700)
        cpuIntel = Product.builder()
                .name("CPU Intel Core i5-13400 Test")
                .price(new BigDecimal("5200000"))
                .quantity(10)
                .isActive(true)
                .build();
        cpuIntel = productRepository.save(cpuIntel);
        cpuIntelSpec = productSpecRepository.save(ProductSpec.builder()
                .productId(cpuIntel.getId())
                .specName("Socket")
                .specValue("LGA 1700")
                .build());

        // Create test Mainboard Intel (LGA 1700)
        mbIntel = Product.builder()
                .name("Mainboard MSI B760M Test")
                .price(new BigDecimal("3100000"))
                .quantity(5)
                .isActive(true)
                .build();
        mbIntel = productRepository.save(mbIntel);
        mbIntelSpec = productSpecRepository.save(ProductSpec.builder()
                .productId(mbIntel.getId())
                .specName("Socket")
                .specValue("LGA 1700")
                .build());

        // Create test Mainboard AMD (AM4)
        mbAmd = Product.builder()
                .name("Mainboard ASUS B550M Test")
                .price(new BigDecimal("2800000"))
                .quantity(8)
                .isActive(true)
                .build();
        mbAmd = productRepository.save(mbAmd);
        mbAmdSpec = productSpecRepository.save(ProductSpec.builder()
                .productId(mbAmd.getId())
                .specName("Socket")
                .specValue("AM4")
                .build());
    }

    @AfterEach
    void tearDown() {
        if (cpuIntelSpec != null) productSpecRepository.deleteById(cpuIntelSpec.getId());
        if (mbIntelSpec != null) productSpecRepository.deleteById(mbIntelSpec.getId());
        if (mbAmdSpec != null) productSpecRepository.deleteById(mbAmdSpec.getId());

        if (cpuIntel != null) productRepository.deleteById(cpuIntel.getId());
        if (mbIntel != null) productRepository.deleteById(mbIntel.getId());
        if (mbAmd != null) productRepository.deleteById(mbAmd.getId());
    }

    @Test
    @DisplayName("Should normalize socket strings consistently")
    void testNormalizeSocket() {
        assertEquals("LGA1700", PcBuilderService.normalizeSocket("LGA 1700"));
        assertEquals("LGA1700", PcBuilderService.normalizeSocket("lga-1700"));
        assertEquals("LGA1700", PcBuilderService.normalizeSocket("LGA_1700"));
        assertEquals("AM4", PcBuilderService.normalizeSocket("  am-4  "));
        assertEquals("", PcBuilderService.normalizeSocket(null));
        assertEquals("", PcBuilderService.normalizeSocket("   "));
    }

    @Test
    @DisplayName("Should keep both CPU and Mainboard when sockets match")
    void testSelectMatchingCpuAndMainboard() {
        MockHttpSession session = new MockHttpSession();

        // 1. Select CPU (slot 1)
        var res1 = pcBuilderService.selectComponent(session, 1, cpuIntel.getId(), 1);
        assertTrue(res1.success());
        assertNull(res1.warning());

        // 2. Select Mainboard matching socket (slot 3)
        var res2 = pcBuilderService.selectComponent(session, 3, mbIntel.getId(), 1);
        assertTrue(res2.success());
        assertNull(res2.warning());

        // Verify both slots exist
        PcBuilderSessionDto build = pcBuilderService.getBuildSession(session);
        assertEquals(2, build.getSelectedCount());
        assertTrue(build.getItems().containsKey(1));
        assertTrue(build.getItems().containsKey(3));
        assertEquals("LGA 1700", build.getItems().get(1).getSocket());
        assertEquals("LGA 1700", build.getItems().get(3).getSocket());
    }

    @Test
    @DisplayName("Should auto-evict incompatible Mainboard when selecting a CPU with different socket")
    void testAutoEvictIncompatibleMainboard() {
        MockHttpSession session = new MockHttpSession();

        // 1. Select AMD Mainboard (slot 3)
        pcBuilderService.selectComponent(session, 3, mbAmd.getId(), 1);
        PcBuilderSessionDto build = pcBuilderService.getBuildSession(session);
        assertTrue(build.getItems().containsKey(3));
        assertEquals("AM4", build.getItems().get(3).getSocket());

        // 2. Select Intel CPU (slot 1, LGA 1700) -> Should evict AMD Mainboard!
        var res = pcBuilderService.selectComponent(session, 1, cpuIntel.getId(), 1);
        assertTrue(res.success());
        assertNotNull(res.warning());
        assertTrue(res.warning().contains("Bo mạch chủ trước đó đã được gỡ bỏ"));

        // Slot 3 should be evicted, slot 1 should be present
        build = pcBuilderService.getBuildSession(session);
        assertEquals(1, build.getSelectedCount());
        assertTrue(build.getItems().containsKey(1));
        assertFalse(build.getItems().containsKey(3));
    }

    @Test
    @DisplayName("Should auto-evict incompatible CPU when selecting a Mainboard with different socket")
    void testAutoEvictIncompatibleCpu() {
        MockHttpSession session = new MockHttpSession();

        // 1. Select Intel CPU (slot 1)
        pcBuilderService.selectComponent(session, 1, cpuIntel.getId(), 1);
        PcBuilderSessionDto build = pcBuilderService.getBuildSession(session);
        assertTrue(build.getItems().containsKey(1));

        // 2. Select AMD Mainboard (slot 3, AM4) -> Should evict Intel CPU!
        var res = pcBuilderService.selectComponent(session, 3, mbAmd.getId(), 1);
        assertTrue(res.success());
        assertNotNull(res.warning());
        assertTrue(res.warning().contains("Vi xử lý (CPU) trước đó đã được gỡ bỏ"));

        // Slot 1 should be evicted, slot 3 should be present
        build = pcBuilderService.getBuildSession(session);
        assertEquals(1, build.getSelectedCount());
        assertTrue(build.getItems().containsKey(3));
        assertFalse(build.getItems().containsKey(1));
    }

    @Test
    @DisplayName("Should batch add configured PC parts into shopping cart session")
    void testBatchAddToCart() {
        MockHttpSession session = new MockHttpSession();

        pcBuilderService.selectComponent(session, 1, cpuIntel.getId(), 1);
        pcBuilderService.selectComponent(session, 3, mbIntel.getId(), 1);

        var result = pcBuilderService.batchAddToCart(session, cartService);
        assertTrue(result.success());
        assertTrue(result.cartCount() >= 2);

        CartDto cart = cartService.getCart(session);
        assertTrue(cart.getItems().containsKey(cpuIntel.getId()));
        assertTrue(cart.getItems().containsKey(mbIntel.getId()));
    }

    @Test
    @DisplayName("Should generate intelligent offline rule-based recommendation with budget and valid socket pair")
    void testAiRuleBasedFallback() {
        AiRecommendationResponseDto res = pcBuilderAiService.generateFallbackReply(
                "Build PC gaming 20 triệu", new BigDecimal("20000000"), "gaming"
        );

        assertTrue(res.isSuccess());
        assertNotNull(res.getReply());
        assertTrue(res.getReply().contains("Cấu hình PC đề xuất"));
        assertNotNull(res.getSuggestions());
        assertTrue(res.getSuggestions().contains("Áp dụng cấu hình này"));
        assertFalse(res.getBuildSuggestion().isEmpty());
    }
}
