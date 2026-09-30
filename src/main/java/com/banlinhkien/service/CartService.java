package com.banlinhkien.service;

import com.banlinhkien.dto.CartDto;
import com.banlinhkien.dto.CartItemDto;
import com.banlinhkien.entity.Product;
import com.banlinhkien.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    public static final String SESSION_CART_KEY = "cart";

    private final ProductRepository productRepository;

    public CartDto getCart(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(SESSION_CART_KEY);
        if (cart == null) {
            cart = new CartDto();
            session.setAttribute(SESSION_CART_KEY, cart);
        }
        return cart;
    }

    public CartItemDto addItem(HttpSession session, Long productId, int quantity) {
        CartDto cart = getCart(session);
        CartItemDto item = addItemToCart(cart, productId, quantity);
        session.setAttribute(SESSION_CART_KEY, cart);
        return item;
    }

    public CartItemDto addItemToCart(CartDto cart, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng thêm vào giỏ hàng phải lớn hơn 0.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Sản phẩm không tồn tại (ID: " + productId + ")."));

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw new IllegalStateException("Sản phẩm '" + product.getName() + "' đã ngừng kinh doanh.");
        }

        int currentQty = 0;
        if (cart.getItems().containsKey(productId)) {
            currentQty = cart.getItems().get(productId).getQuantity();
        }
        int newQty = currentQty + quantity;

        int availableStock = product.getQuantity() != null ? product.getQuantity() : 0;
        if (newQty > availableStock) {
            throw new IllegalArgumentException("Sản phẩm '" + product.getName() + "' chỉ còn " + availableStock + " cái trong kho.");
        }

        BigDecimal unitPrice = product.getFinalPrice();

        CartItemDto item = CartItemDto.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(unitPrice)
                .originalPrice(product.getPrice())
                .discountPercent(product.getDiscountPercent())
                .image(product.getImage())
                .quantity(newQty)
                .maxStock(availableStock)
                .build();

        cart.getItems().put(productId, item);
        // Reset applied voucher if cart subtotal changes
        cart.setAppliedVoucher(null);
        cart.setVoucherDiscount(BigDecimal.ZERO);

        return item;
    }

    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        CartDto cart = getCart(session);
        updateQuantityInCart(cart, productId, quantity);
        session.setAttribute(SESSION_CART_KEY, cart);
    }

    public void updateQuantityInCart(CartDto cart, Long productId, int quantity) {
        if (quantity <= 0) {
            removeItemFromCart(cart, productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Sản phẩm không tồn tại."));

        if (Boolean.FALSE.equals(product.getIsActive())) {
            removeItemFromCart(cart, productId);
            throw new IllegalStateException("Sản phẩm không còn khả dụng.");
        }

        int availableStock = product.getQuantity() != null ? product.getQuantity() : 0;
        if (quantity > availableStock) {
            throw new IllegalArgumentException("Sản phẩm '" + product.getName() + "' chỉ còn " + availableStock + " cái trong kho.");
        }

        if (!cart.getItems().containsKey(productId)) {
            throw new NoSuchElementException("Sản phẩm không có trong giỏ hàng.");
        }

        CartItemDto item = cart.getItems().get(productId);
        item.setQuantity(quantity);
        item.setPrice(product.getFinalPrice());
        item.setMaxStock(availableStock);

        cart.setAppliedVoucher(null);
        cart.setVoucherDiscount(BigDecimal.ZERO);
    }

    public void removeItem(HttpSession session, Long productId) {
        CartDto cart = getCart(session);
        removeItemFromCart(cart, productId);
        session.setAttribute(SESSION_CART_KEY, cart);
    }

    public void removeItemFromCart(CartDto cart, Long productId) {
        cart.getItems().remove(productId);
        cart.setAppliedVoucher(null);
        cart.setVoucherDiscount(BigDecimal.ZERO);
    }

    public void clear(HttpSession session) {
        CartDto cart = getCart(session);
        cart.clear();
        session.setAttribute(SESSION_CART_KEY, cart);
    }

    public int getTotalCount(HttpSession session) {
        return getCart(session).getTotalItems();
    }

    public BigDecimal getSubtotal(HttpSession session) {
        return getCart(session).getSubtotal();
    }
}
