package com.banlinhkien.slice2;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.CartItemDto;
import com.banlinhkien.entity.Order;
import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.OrderRepository;
import com.banlinhkien.repository.ProductRepository;
import com.banlinhkien.service.CartService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class Slice2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Cart API: Should add item to cart via AJAX and return cart count")
    void testAddToCartApi() throws Exception {
        List<Product> products = productRepository.findByIsActiveTrue(org.springframework.data.domain.PageRequest.of(0, 1)).getContent();
        assertFalse(products.isEmpty());
        Product product = products.get(0);

        mockMvc.perform(post("/api/cart/add")
                        .param("product_id", product.getId().toString())
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cart_count", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.item.name").value(product.getName()));
    }

    @Test
    @DisplayName("Location API: Should return list of provinces")
    void testGetProvincesApi() throws Exception {
        mockMvc.perform(get("/api/location/provinces"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("View: Should render cart page successfully")
    void testCartPageView() throws Exception {
        mockMvc.perform(get("/gio-hang"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/index"))
                .andExpect(model().attributeExists("cart"))
                .andExpect(model().attributeExists("subtotal"));
    }

    @Test
    @DisplayName("Checkout Flow: Should place order via POST /thanh-toan and redirect to success page")
    void testCompleteCheckoutFlow() throws Exception {
        List<Product> products = productRepository.findByIsActiveTrue(org.springframework.data.domain.PageRequest.of(0, 1)).getContent();
        assertFalse(products.isEmpty());
        Product product = products.get(0);

        MockHttpSession session = new MockHttpSession();

        // 1. Add item to session cart
        CartDto cart = new CartDto();
        cart.getItems().put(product.getId(), CartItemDto.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getFinalPrice())
                .quantity(1)
                .maxStock(product.getQuantity() != null ? product.getQuantity() : 10)
                .build());
        session.setAttribute(CartService.SESSION_CART_KEY, cart);

        // 2. View checkout page
        mockMvc.perform(get("/thanh-toan").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/index"))
                .andExpect(model().attributeExists("provinces"));

        // 3. Submit checkout form
        String redirectUrl = mockMvc.perform(post("/thanh-toan")
                        .session(session)
                        .with(csrf())
                        .param("customerName", "Khách Hàng Test")
                        .param("customerPhone", "0901234567")
                        .param("customerEmail", "test@banlinhkien.vn")
                        .param("customerAddress", "123 Đường Cầu Giấy")
                        .param("province", "Hà Nội")
                        .param("paymentMethod", "cod"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("/dat-hang-thanh-cong/"));

        // Extract order ID
        String orderIdStr = redirectUrl.substring("/dat-hang-thanh-cong/".length());
        Long orderId = Long.parseLong(orderIdStr);

        // 4. Verify order in DB
        Order order = orderRepository.findWithItemsById(orderId).orElseThrow();
        assertEquals("Khách Hàng Test", order.getCustomerName());
        assertEquals("0901234567", order.getCustomerPhone());
        assertFalse(order.getItems().isEmpty());

        // 5. View success confirmation page
        mockMvc.perform(get(redirectUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout/success"))
                .andExpect(model().attributeExists("order"));
    }
}
