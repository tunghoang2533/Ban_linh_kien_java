package com.banlinhkien.repository;

import com.banlinhkien.entity.VietnamProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VietnamProvinceRepository extends JpaRepository<VietnamProvince, Long> {
    List<VietnamProvince> findAllByOrderByNameAsc();
    Optional<VietnamProvince> findByProvinceId(Long provinceId);
    Optional<VietnamProvince> findByNameIgnoreCase(String name);
}
