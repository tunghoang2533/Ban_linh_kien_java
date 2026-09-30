package com.banlinhkien.repository;

import com.banlinhkien.entity.WarehouseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseLogRepository extends JpaRepository<WarehouseLog, Long> {
    List<WarehouseLog> findByReferenceIdOrderByCreatedAtDesc(Long referenceId);
    List<WarehouseLog> findByProductIdOrderByCreatedAtDesc(Long productId);
}
