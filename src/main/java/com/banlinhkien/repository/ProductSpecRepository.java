package com.banlinhkien.repository;

import com.banlinhkien.entity.ProductSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    List<ProductSpec> findByProductId(Long productId);

    Optional<ProductSpec> findByProductIdAndSpecName(Long productId, String specName);

    @Query("SELECT s.specValue FROM ProductSpec s WHERE s.productId = :productId AND s.specName = :specName")
    Optional<String> findSpecValue(@Param("productId") Long productId, @Param("specName") String specName);

    @Query("SELECT s.productId FROM ProductSpec s WHERE s.specName = 'Socket' AND " +
           "(UPPER(s.specValue) = UPPER(:socket) OR " +
           "REPLACE(REPLACE(REPLACE(UPPER(s.specValue), ' ', ''), '-', ''), '_', '') = :normalizedSocket)")
    List<Long> findProductIdsBySocket(@Param("socket") String socket, @Param("normalizedSocket") String normalizedSocket);
}
