package com.smartenvelope.entity;

import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.RolloverPolicy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho kỳ ngân sách (chu kỳ tháng theo triết lý Zero-based
 * Budgeting). Quản lý tổng thu nhập kỳ, tổng phân bổ, trạng thái chốt (DRAFT,
 * CONFIRMED, CLOSED) và chính sách dọn dẹp số dư cuối tháng (RolloverPolicy).
 * Phụ trách bởi: Thành viên 4 (Budget & Envelope Logic).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "budget_periods", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "workspace_id", "month", "year" }) })
public class BudgetPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_income", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "expected_income", precision = 19, scale = 0)
    private BigDecimal expectedIncome;

    @Column(name = "total_allocated", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalAllocated = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BudgetStatus status = BudgetStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "rollover_policy", nullable = false, length = 30)
    private RolloverPolicy rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BudgetPeriod(Workspace workspace, String name, Integer month, Integer year,
            LocalDate startDate, LocalDate endDate, BigDecimal totalIncome) {

        this.workspace = workspace;
        this.name = name;
        this.month = month;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalAllocated = BigDecimal.ZERO;
        this.status = BudgetStatus.DRAFT;
        this.rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;
    }

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.totalIncome == null) {

            this.totalIncome = BigDecimal.ZERO;
        }

        if (this.totalAllocated == null) {

            this.totalAllocated = BigDecimal.ZERO;
        }

        if (this.status == null) {

            this.status = BudgetStatus.DRAFT;
        }

        if (this.rolloverPolicy == null) {

            this.rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;
        }

        if (this.version == null) {

            this.version = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }

}
