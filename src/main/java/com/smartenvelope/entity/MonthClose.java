package com.smartenvelope.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho phiên đóng kỳ ngân sách cuối tháng (Month-end Close).
 * Chốt số liệu tổng chi tiêu, tổng số tiền tiết kiệm được quét sang heo đất/mục tiêu và ghi chú tổng kết.
 * Phụ trách bởi: Thành viên 4 & Thành viên 5.
 */
@Entity
@Table(name = "month_closes")
public class MonthClose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_period_id", nullable = false, unique = true)
    private BudgetPeriod budgetPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by", nullable = false)
    private User closedBy;

    @Column(name = "closed_at", nullable = false, updatable = false)
    private LocalDateTime closedAt;

    @Column(name = "total_spent", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "total_saved", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalSaved = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public MonthClose() {
    }

    public MonthClose(BudgetPeriod budgetPeriod, User closedBy, BigDecimal totalSpent, BigDecimal totalSaved, String notes) {
        this.budgetPeriod = budgetPeriod;
        this.closedBy = closedBy;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
        this.totalSaved = totalSaved != null ? totalSaved : BigDecimal.ZERO;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        this.closedAt = LocalDateTime.now();
        if (this.totalSpent == null) {
            this.totalSpent = BigDecimal.ZERO;
        }
        if (this.totalSaved == null) {
            this.totalSaved = BigDecimal.ZERO;
        }
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

    public User getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy = closedBy;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getTotalSaved() {
        return totalSaved;
    }

    public void setTotalSaved(BigDecimal totalSaved) {
        this.totalSaved = totalSaved;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

