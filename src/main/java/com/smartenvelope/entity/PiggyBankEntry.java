package com.smartenvelope.entity;

import com.smartenvelope.enums.PiggyBankEntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho lịch sử biến động dòng tiền trong Heo đất. Ghi nhận các
 * hoạt động quét dư cuối tháng (SWEEP), nạp thủ công (MANUAL_DEPOSIT) hoặc rút
 * tiền (WITHDRAW). Phụ trách bởi: Thành viên 5 (Savings, Piggy Bank &
 * Analytics).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "piggy_bank_entries")
public class PiggyBankEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piggy_bank_id", nullable = false)
    private PiggyBank piggyBank;

    @Column(name = "amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PiggyBankEntryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_envelope_id")
    private Envelope sourceEnvelope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_goal_id")
    private SavingsGoal savingsGoal;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PiggyBankEntry(PiggyBank piggyBank, BigDecimal amount, PiggyBankEntryType type,
            Envelope sourceEnvelope, SavingsGoal savingsGoal, String note) {

        this.piggyBank = piggyBank;
        this.amount = amount;
        this.type = type;
        this.sourceEnvelope = sourceEnvelope;
        this.savingsGoal = savingsGoal;
        this.note = note;
    }

    @PrePersist
    protected void onCreate() {

        this.createdAt = LocalDateTime.now();
    }

}
