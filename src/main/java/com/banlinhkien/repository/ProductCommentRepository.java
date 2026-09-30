package com.banlinhkien.repository;

import com.banlinhkien.entity.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCommentRepository extends JpaRepository<ProductComment, Long> {

    List<ProductComment> findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(Long productId);

    long countByProductIdAndIsHiddenFalse(Long productId);
}
