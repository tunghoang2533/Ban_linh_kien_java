package com.banlinhkien.controller.api;

import com.banlinhkien.dto.ProductQuickViewDto;
import com.banlinhkien.entity.Product;
import com.banlinhkien.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    @GetMapping("/{id}/quick-view")
    public ResponseEntity<ProductQuickViewDto> quickView(@PathVariable("id") Long id) {
        return productService.getProductById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private ProductQuickViewDto toDto(Product p) {
        return ProductQuickViewDto.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .finalPrice(p.getFinalPrice())
                .discountPercent(p.getDiscountPercent())
                .hasDiscount(p.hasDiscount())
                .quantity(p.getQuantity())
                .image(p.getImage())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : "")
                .brandName(p.getBrand() != null ? p.getBrand().getName() : "")
                .description(p.getDescription())
                .build();
    }
}
