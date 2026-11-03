package com.smartenvelope.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho chiếc phong bì ngân sách (hạn mức chi tiêu cho một danh mục trong một kỳ).
 * Lưu giữ số tiền phân bổ (allocated_amount), số tiền đã tiêu (spent_amount), số dư còn lại (remaining_amount)
 * và ngưỡng cảnh báo (warning_threshold %).
 * Phụ trách bởi: Thành viên 4 (Budget & Envelope Logic).
 */
@Entity
@Table(name = "envelopes")
public class Envelope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_period_id", nullable = false)
    private BudgetPeriod budgetPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(name = "spent_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    @Column(name = "warning_threshold", nullable = false)
    private Integer warningThreshold = 80;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Envelope() {
    }

    public Envelope(BudgetPeriod budgetPeriod, Category category, String name, BigDecimal allocatedAmount, Integer warningThreshold) {
        this.budgetPeriod = budgetPeriod;
        this.category = category;
        this.name = name;
        this.allocatedAmount = allocatedAmount != null ? allocatedAmount : BigDecimal.ZERO;
        this.spentAmount = BigDecimal.ZERO;
        this.remainingAmount = this.allocatedAmount;
        this.warningThreshold = warningThreshold != null ? warningThreshold : 80;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.allocatedAmount == null) {
            this.allocatedAmount = BigDecimal.ZERO;
        }
        if (this.spentAmount == null) {
            this.spentAmount = BigDecimal.ZERO;
        }
        if (this.remainingAmount == null) {
            this.remainingAmount = this.allocatedAmount.subtract(this.spentAmount);
        }
        if (this.warningThreshold == null) {
            this.warningThreshold = 80;
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

    public BudgetPeriod getBudgetPeriod() {
        return budgetPeriod;
    }

    public void setBudgetPeriod(BudgetPeriod budgetPeriod) {
        this.budgetPeriod = budgetPeriod;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Integer getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(Integer warningThreshold) {
        this.warningThreshold = warningThreshold;
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
