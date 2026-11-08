package com.smartenvelope.entity;

import com.smartenvelope.enums.TransferReason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho lịch sử điều chuyển/bù đắp ngân sách giữa các phong bì
 * trong cùng một kỳ. Hỗ trợ nguyên tắc Zero-based Budgeting: khi một phong bì
 * bị thiếu hụt, người dùng có thể bù đắp từ phong bì khác. Phụ trách bởi: Thành
 * viên 3 (Transaction & Ledger Processing).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "envelope_transfers")
public class EnvelopeTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_period_id", nullable = false)
    private BudgetPeriod budgetPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_envelope_id", nullable = false)
    private Envelope fromEnvelope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_envelope_id", nullable = false)
    private Envelope toEnvelope;

    @Column(name = "amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private TransferReason reason = TransferReason.COVER_EXPENSE;

    @Column(name = "note", length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_by", nullable = false)
    private User transferredBy;

    @Column(name = "transferred_at", nullable = false, updatable = false)
    private LocalDateTime transferredAt;

    public EnvelopeTransfer(BudgetPeriod budgetPeriod, Envelope fromEnvelope, Envelope toEnvelope,
            BigDecimal amount, TransferReason reason, String note, User transferredBy) {

        this.budgetPeriod = budgetPeriod;
        this.fromEnvelope = fromEnvelope;
        this.toEnvelope = toEnvelope;
        this.amount = amount;
        this.reason = reason;
        this.note = note;
        this.transferredBy = transferredBy;
    }

    @PrePersist
    protected void onCreate() {

        this.transferredAt = LocalDateTime.now();

        if (this.reason == null) {

            this.reason = TransferReason.COVER_EXPENSE;
        }
    }

}
