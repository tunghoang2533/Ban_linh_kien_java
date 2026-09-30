package com.banlinhkien.repository;

import com.banlinhkien.entity.VietnamDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VietnamDistrictRepository extends JpaRepository<VietnamDistrict, Long> {
    List<VietnamDistrict> findByProvinceIdOrderByNameAsc(Long provinceId);
    Optional<VietnamDistrict> findByDistrictId(Long districtId);
}
