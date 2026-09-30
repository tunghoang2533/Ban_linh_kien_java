package com.banlinhkien.controller.api;

import com.banlinhkien.dto.LocationDto;
import com.banlinhkien.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationApiController {

    private final LocationService locationService;

    @GetMapping("/provinces")
    public ResponseEntity<List<LocationDto>> getProvinces() {
        return ResponseEntity.ok(locationService.getAllProvinces());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<LocationDto>> getDistricts(@RequestParam("province_id") Long provinceId) {
        return ResponseEntity.ok(locationService.getDistrictsByProvinceId(provinceId));
    }

    @GetMapping("/wards")
    public ResponseEntity<List<LocationDto>> getWards(@RequestParam("district_id") Long districtId) {
        return ResponseEntity.ok(locationService.getWardsByDistrictId(districtId));
    }
}
