package com.smartenvelope.entity;

import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.RolloverPolicy;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho kỳ ngân sách (chu kỳ tháng theo triết lý Zero-based Budgeting).
 * Quản lý tổng thu nhập kỳ, tổng phân bổ, trạng thái chốt (DRAFT, CONFIRMED, CLOSED)
 * và chính sách dọn dẹp số dư cuối tháng (RolloverPolicy).
 * Phụ trách bởi: Thành viên 4 (Budget & Envelope Logic).
 */
@Entity
@Table(name = "budget_periods", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "month", "year"})
})
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

    public BudgetPeriod() {
    }

    public BudgetPeriod(Workspace workspace, String name, Integer month, Integer year, LocalDate startDate, LocalDate endDate, BigDecimal totalIncome) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(BigDecimal totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public BudgetStatus getStatus() {
        return status;
    }

    public void setStatus(BudgetStatus status) {
        this.status = status;
    }

    public RolloverPolicy getRolloverPolicy() {
        return rolloverPolicy;
    }

    public void setRolloverPolicy(RolloverPolicy rolloverPolicy) {
        this.rolloverPolicy = rolloverPolicy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
