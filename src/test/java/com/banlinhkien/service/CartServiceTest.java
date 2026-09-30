package com.banlinhkien.service;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.CartItemDto;
import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        List<Product> products = productRepository.findByIsActiveTrue(org.springframework.data.domain.PageRequest.of(0, 1)).getContent();
        assertFalse(products.isEmpty(), "Database must have at least one active product");
        testProduct = products.get(0);
    }

    @Test
    @DisplayName("Should successfully add product to CartDto and compute subtotal")
    void testAddToCart() {
        CartDto cart = new CartDto();
        CartItemDto item = cartService.addItemToCart(cart, testProduct.getId(), 1);

        assertNotNull(item);
        assertEquals(testProduct.getId(), item.getProductId());
        assertEquals(1, item.getQuantity());
        assertEquals(1, cart.getTotalItems());
        assertEquals(testProduct.getFinalPrice(), cart.getSubtotal());
    }

    @Test
    @DisplayName("Should update quantity and recalculate subtotal")
    void testUpdateQuantity() {
        CartDto cart = new CartDto();
        cartService.addItemToCart(cart, testProduct.getId(), 1);

        if (testProduct.getQuantity() != null && testProduct.getQuantity() >= 2) {
            cartService.updateQuantityInCart(cart, testProduct.getId(), 2);
            assertEquals(2, cart.getTotalItems());
            assertEquals(testProduct.getFinalPrice().multiply(BigDecimal.valueOf(2)), cart.getSubtotal());
        }
    }

    @Test
    @DisplayName("Should reject adding more than available stock")
    void testStockLimitExceeded() {
        CartDto cart = new CartDto();
        int overStock = (testProduct.getQuantity() != null ? testProduct.getQuantity() : 0) + 100;

        assertThrows(IllegalArgumentException.class, () -> {
            cartService.addItemToCart(cart, testProduct.getId(), overStock);
        });
    }

    @Test
    @DisplayName("Should remove item from cart")
    void testRemoveItem() {
        CartDto cart = new CartDto();
        cartService.addItemToCart(cart, testProduct.getId(), 1);
        assertFalse(cart.isEmpty());

        cartService.removeItemFromCart(cart, testProduct.getId());
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotalItems());
        assertEquals(BigDecimal.ZERO, cart.getSubtotal());
    }
}
