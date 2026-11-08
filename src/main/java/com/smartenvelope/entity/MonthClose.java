package com.smartenvelope.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho phiên đóng kỳ ngân sách cuối tháng (Month-end Close).
 * Chốt số liệu tổng chi tiêu, tổng số tiền tiết kiệm được quét sang heo đất/mục
 * tiêu và ghi chú tổng kết. Phụ trách bởi: Thành viên 4 & Thành viên 5.
 */
@Getter
@Setter
@NoArgsConstructor
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

    public MonthClose(BudgetPeriod budgetPeriod, User closedBy, BigDecimal totalSpent,
            BigDecimal totalSaved, String notes) {

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

}
