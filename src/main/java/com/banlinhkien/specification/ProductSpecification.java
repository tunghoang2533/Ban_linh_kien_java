package com.banlinhkien.specification;

import com.banlinhkien.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filter(String q, Long categoryId, Long brandId, String stock, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword search (name or ID)
            if (q != null && !q.isBlank()) {
                String cleanQ = q.trim();
                Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + cleanQ.toLowerCase() + "%");

                try {
                    Long idVal = Long.parseLong(cleanQ);
                    Predicate idEqual = cb.equal(root.get("id"), idVal);
                    predicates.add(cb.or(nameLike, idEqual));
                } catch (NumberFormatException ignored) {
                    predicates.add(nameLike);
                }
            }

            // 2. Category filter
            if (categoryId != null && categoryId > 0) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // 3. Brand filter
            if (brandId != null && brandId > 0) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }

            // 4. Stock status filter ("low", "out")
            if (stock != null && !stock.isBlank()) {
                if ("low".equalsIgnoreCase(stock)) {
                    predicates.add(cb.and(
                            cb.greaterThan(root.get("quantity"), 0),
                            cb.lessThanOrEqualTo(root.get("quantity"), 5)
                    ));
                } else if ("out".equalsIgnoreCase(stock)) {
                    predicates.add(cb.equal(root.get("quantity"), 0));
                }
            }

            // 5. Active status filter
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
