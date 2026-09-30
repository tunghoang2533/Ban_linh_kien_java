package com.banlinhkien.service;

import com.banlinhkien.dto.LocationDto;
import com.banlinhkien.repository.VietnamDistrictRepository;
import com.banlinhkien.repository.VietnamProvinceRepository;
import com.banlinhkien.repository.VietnamWardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final VietnamProvinceRepository provinceRepository;
    private final VietnamDistrictRepository districtRepository;
    private final VietnamWardRepository wardRepository;

    public List<LocationDto> getAllProvinces() {
        return provinceRepository.findAllByOrderByNameAsc().stream()
                .map(p -> LocationDto.builder()
                        .id(p.getId())
                        .code(p.getProvinceId())
                        .name(p.getName())
                        .type(p.getType())
                        .build())
                .toList();
    }

    public List<LocationDto> getDistrictsByProvinceId(Long provinceId) {
        return districtRepository.findByProvinceIdOrderByNameAsc(provinceId).stream()
                .map(d -> LocationDto.builder()
                        .id(d.getId())
                        .code(d.getDistrictId())
                        .name(d.getName())
                        .type(d.getType())
                        .build())
                .toList();
    }

    public List<LocationDto> getWardsByDistrictId(Long districtId) {
        return wardRepository.findByDistrictIdOrderByNameAsc(districtId).stream()
                .map(w -> LocationDto.builder()
                        .id(w.getId())
                        .code(w.getWardId())
                        .name(w.getName())
                        .type(w.getType())
                        .build())
                .toList();
    }
}
