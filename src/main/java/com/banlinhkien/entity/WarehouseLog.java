package com.banlinhkien.entity;

import com.banlinhkien.enums.WarehouseLogType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 10)
    private WarehouseLogType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "unit_cost", precision = 15, scale = 0)
    private BigDecimal unitCost;

    @Column(length = 30)
    private String reason;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "po_id")
    private Long poId;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.warehouseId == null) {
            this.warehouseId = 1L;
        }
    }
}
