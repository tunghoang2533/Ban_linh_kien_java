package com.banlinhkien.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Map<Long, CartItemDto> items = new LinkedHashMap<>();

    private String appliedVoucher;
    @Builder.Default
    private BigDecimal voucherDiscount = BigDecimal.ZERO;

    public Collection<CartItemDto> getItemList() {
        return items.values();
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(CartItemDto::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.values().stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public void clear() {
        items.clear();
        appliedVoucher = null;
        voucherDiscount = BigDecimal.ZERO;
    }
}
