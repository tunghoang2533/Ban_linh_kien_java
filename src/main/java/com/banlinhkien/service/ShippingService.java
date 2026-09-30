package com.banlinhkien.service;

import com.banlinhkien.entity.ShippingZone;
import com.banlinhkien.repository.ShippingZoneRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    public static final BigDecimal DEFAULT_FEE = new BigDecimal("50000");
    public static final BigDecimal DEFAULT_FREE_THRESHOLD = new BigDecimal("500000");

    private final ShippingZoneRepository shippingZoneRepository;
    private final ObjectMapper objectMapper;

    public BigDecimal calculateFee(String province, BigDecimal subtotal, int itemCount) {
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (province == null || province.isBlank()) {
            return subtotal.compareTo(DEFAULT_FREE_THRESHOLD) >= 0 ? BigDecimal.ZERO : DEFAULT_FEE;
        }

        String cleanProvince = province.trim();
        List<ShippingZone> activeZones = shippingZoneRepository.findByIsActiveTrue();

        for (ShippingZone zone : activeZones) {
            List<String> provinceList = parseProvinces(zone.getProvinces());
            for (String p : provinceList) {
                if (matchesProvince(p, cleanProvince)) {
                    if (zone.getFreeShippingMin() != null
                            && zone.getFreeShippingMin().compareTo(BigDecimal.ZERO) > 0
                            && subtotal.compareTo(zone.getFreeShippingMin()) >= 0) {
                        return BigDecimal.ZERO;
                    }
                    return zone.getBaseFee() != null ? zone.getBaseFee() : DEFAULT_FEE;
                }
            }
        }

        return subtotal.compareTo(DEFAULT_FREE_THRESHOLD) >= 0 ? BigDecimal.ZERO : DEFAULT_FEE;
    }

    private boolean matchesProvince(String zoneProvince, String targetProvince) {
        if (zoneProvince == null || targetProvince == null) {
            return false;
        }
        String p1 = normalizeProvinceName(zoneProvince);
        String p2 = normalizeProvinceName(targetProvince);
        return p1.equalsIgnoreCase(p2);
    }

    private String normalizeProvinceName(String name) {
        return name.toLowerCase()
                .replaceAll("^(thành phố|tỉnh|tp\\.?)\\s+", "")
                .trim();
    }

    private List<String> parseProvinces(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.debug("Failed to parse shipping zone provinces JSON: {}", json);
            return Collections.emptyList();
        }
    }
}
