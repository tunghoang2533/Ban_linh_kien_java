package com.banlinhkien.slice4;

import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class Slice4PcBuilderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Endpoint GET /build-pc should render PC Builder page with 7 hardware category slots")
    void testPcBuilderViewRenders7Slots() throws Exception {
        mockMvc.perform(get("/build-pc"))
                .andExpect(status().isOk())
                .andExpect(view().name("buildpc/index"))
                .andExpect(model().attributeExists("buildCategories"))
                .andExpect(model().attributeExists("build"))
                .andExpect(content().string(containsString("Xây Dựng Cấu Hình PC")))
                .andExpect(content().string(containsString("Vi xử lý (CPU)")))
                .andExpect(content().string(containsString("Bo mạch chủ (Mainboard)")))
                .andExpect(content().string(containsString("Bộ nhớ trong (RAM)")))
                .andExpect(content().string(containsString("Card màn hình (VGA)")))
                .andExpect(content().string(containsString("Nguồn máy tính (PSU)")))
                .andExpect(content().string(containsString("Vỏ máy tính (Case)")));
    }

    @Test
    @DisplayName("API GET /build-pc/components should return JSON products list for requested category")
    void testGetComponentsApi() throws Exception {
        mockMvc.perform(get("/build-pc/components").param("category_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.products").isArray());
    }

    @Test
    @DisplayName("API POST /build-pc/select should store component in session and return JSON")
    void testSelectComponentApi() throws Exception {
        List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(1L);
        assertFalse(products.isEmpty());
        Product cpu = products.get(0);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/build-pc/select")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category_id\": 1, \"product_id\": " + cpu.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.build.1.id").value(cpu.getId()));
    }

    @Test
    @DisplayName("API POST /build-pc/add-to-cart should batch add configured items to shopping cart")
    void testBatchAddToCartApi() throws Exception {
        List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(1L);
        assertFalse(products.isEmpty());
        Product cpu = products.get(0);

        MockHttpSession session = new MockHttpSession();

        // 1. Select CPU
        mockMvc.perform(post("/build-pc/select")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category_id\": 1, \"product_id\": " + cpu.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isOk());

        // 2. Batch add to cart
        mockMvc.perform(post("/build-pc/add-to-cart")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cart_count", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("API POST /build-pc/ai should return structured AI recommendation JSON")
    void testAiRecommendationApi() throws Exception {
        mockMvc.perform(post("/build-pc/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"budget\": 15000000, \"purpose\": \"gaming\", \"message\": \"Tư vấn PC 15 triệu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.reply", containsString("Cấu hình PC đề xuất")))
                .andExpect(jsonPath("$.suggestions", hasItem("Áp dụng cấu hình này")))
                .andExpect(jsonPath("$.build_suggestion").isArray());
    }
}
