package com.banlinhkien.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PcBuilderSessionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Map<Integer, PcBuilderItemDto> items = new LinkedHashMap<>();

    public BigDecimal getTotalPrice() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (PcBuilderItemDto item : items.values()) {
            if (item != null && item.getPrice() != null) {
                int qty = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(qty)));
            }
        }
        return total;
    }

    public int getSelectedCount() {
        return items != null ? items.size() : 0;
    }

    public String getSocketForCategory(int categoryId) {
        if (items != null && items.containsKey(categoryId)) {
            PcBuilderItemDto item = items.get(categoryId);
            return item != null ? item.getSocket() : null;
        }
        return null;
    }
}
