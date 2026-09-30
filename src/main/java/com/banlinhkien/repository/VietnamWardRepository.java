package com.banlinhkien.repository;

import com.banlinhkien.entity.VietnamWard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VietnamWardRepository extends JpaRepository<VietnamWard, Long> {
    List<VietnamWard> findByDistrictIdOrderByNameAsc(Long districtId);
    Optional<VietnamWard> findByWardId(Long wardId);
}
