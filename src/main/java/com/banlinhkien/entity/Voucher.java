package com.banlinhkien.entity;

import com.banlinhkien.enums.VoucherType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "personal_note", length = 500)
    private String personalNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type;

    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "min_order", precision = 10, scale = 2)
    private BigDecimal minOrder;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount;

    @Column(name = "expire_date", nullable = false)
    private LocalDate expireDate;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.usedCount == null) {
            this.usedCount = 0;
        }
        if (this.value == null) {
            this.value = BigDecimal.ZERO;
        }
        if (this.minOrder == null) {
            this.minOrder = BigDecimal.ZERO;
        }
        if (this.type == null) {
            this.type = VoucherType.fixed;
        }
    }

    public boolean isExpired() {
        return expireDate != null && LocalDate.now().isAfter(expireDate);
    }
}
