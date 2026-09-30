package com.banlinhkien.service;

import com.banlinhkien.dto.VoucherResultDto;
import com.banlinhkien.entity.Voucher;
import com.banlinhkien.entity.VoucherUsage;
import com.banlinhkien.enums.VoucherType;
import com.banlinhkien.repository.VoucherRepository;
import com.banlinhkien.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    @Transactional(readOnly = true)
    public VoucherResultDto validate(String code, BigDecimal subtotal, Long userId) {
        if (code == null || code.isBlank()) {
            return VoucherResultDto.builder()
                    .success(false)
                    .message("Vui lòng nhập mã giảm giá.")
                    .discount(BigDecimal.ZERO)
                    .build();
        }

        String cleanCode = code.trim().toUpperCase();

        if (userId == null) {
            return VoucherResultDto.builder()
                    .success(false)
                    .message("Bạn cần đăng nhập để sử dụng voucher.")
                    .discount(BigDecimal.ZERO)
                    .build();
        }

        Optional<Voucher> voucherOpt = voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue(cleanCode);
        if (voucherOpt.isEmpty()) {
            return VoucherResultDto.builder()
                    .success(false)
                    .message("Mã giảm giá không tồn tại hoặc đã hết hạn!")
                    .discount(BigDecimal.ZERO)
                    .build();
        }

        Voucher voucher = voucherOpt.get();

        if (voucher.isExpired()) {
            return VoucherResultDto.builder()
                    .success(false)
                    .message("Mã giảm giá này đã hết hạn sử dụng.")
                    .discount(BigDecimal.ZERO)
                    .build();
        }

        if (voucher.getUsageLimit() != null && voucher.getUsageLimit() > 0) {
            int used = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
            if (used >= voucher.getUsageLimit()) {
                return VoucherResultDto.builder()
                        .success(false)
                        .message("Mã giảm giá đã hết lượt sử dụng.")
                        .discount(BigDecimal.ZERO)
                        .build();
            }
        }

        if (voucher.getUserId() != null && !voucher.getUserId().equals(userId)) {
            return VoucherResultDto.builder()
                    .success(false)
                    .message("Mã giảm giá này không áp dụng cho tài khoản của bạn.")
                    .discount(BigDecimal.ZERO)
                    .build();
        }

        if (voucherUsageRepository.existsByVoucherIdAndUserId(voucher.getId(), userId)) {
            return VoucherResultDto.builder()
                    .success(false)
                    .message("Bạn đã sử dụng mã giảm giá này rồi.")
                    .discount(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal minOrder = voucher.getMinOrder() != null ? voucher.getMinOrder() : BigDecimal.ZERO;
        if (subtotal != null && subtotal.compareTo(minOrder) < 0) {
            NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
            return VoucherResultDto.builder()
                    .success(false)
                    .message("Đơn hàng tối thiểu " + nf.format(minOrder) + "₫ để áp dụng voucher này.")
                    .discount(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal discount = calculateDiscount(voucher, subtotal != null ? subtotal : BigDecimal.ZERO);

        return VoucherResultDto.builder()
                .success(true)
                .message("Áp dụng mã giảm giá thành công!")
                .voucherCode(voucher.getCode())
                .discount(discount)
                .build();
    }

    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal subtotal) {
        if (voucher.getType() == VoucherType.percent) {
            BigDecimal rate = voucher.getValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal discount = subtotal.multiply(rate).setScale(0, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscount() != null && voucher.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(voucher.getMaxDiscount());
            }
            return discount;
        }

        // Fixed amount or freeship
        return voucher.getValue() != null ? voucher.getValue().min(subtotal) : BigDecimal.ZERO;
    }

    @Transactional
    public void recordUsage(Voucher voucher, Long userId, Long orderId) {
        int currentUsed = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
        voucher.setUsedCount(currentUsed + 1);
        voucherRepository.save(voucher);

        VoucherUsage usage = VoucherUsage.builder()
                .voucherId(voucher.getId())
                .userId(userId)
                .orderId(orderId)
                .usedAt(LocalDateTime.now())
                .build();
        voucherUsageRepository.save(usage);
    }
}
