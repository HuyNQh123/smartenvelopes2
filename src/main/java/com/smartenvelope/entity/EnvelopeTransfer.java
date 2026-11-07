package com.smartenvelope.entity;

import com.smartenvelope.enums.TransferReason;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ghi nhận lịch sử điều chuyển tiền giữa các phong bì ngân sách.
 * (Bao gồm điều chuyển bù chi vượt COVER_EXPENSE hoặc tái phân bổ ngân sách REPLAN).
 * Phụ trách bởi: Thành viên 3.
 */
@Entity
@Table(name = "envelope_transfers")
public class EnvelopeTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_envelope_id", nullable = false)
    private Envelope sourceEnvelope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_envelope_id", nullable = false)
    private Envelope targetEnvelope;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private TransferReason reason;

    @Column(name = "transferred_at", nullable = false)
    private LocalDateTime transferredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public EnvelopeTransfer() {
    }

    public EnvelopeTransfer(Envelope sourceEnvelope, Envelope targetEnvelope, BigDecimal amount, TransferReason reason, User user) {
        this.sourceEnvelope = sourceEnvelope;
        this.targetEnvelope = targetEnvelope;
        this.amount = amount;
        this.reason = reason;
        this.user = user;
        this.transferredAt = LocalDateTime.now();
    }

    public EnvelopeTransfer(Long id, Envelope sourceEnvelope, Envelope targetEnvelope, BigDecimal amount, TransferReason reason, LocalDateTime transferredAt, User user) {
        this.id = id;
        this.sourceEnvelope = sourceEnvelope;
        this.targetEnvelope = targetEnvelope;
        this.amount = amount;
        this.reason = reason;
        this.transferredAt = transferredAt;
        this.user = user;
    }

    @PrePersist
    public void prePersist() {
        if (this.transferredAt == null) {
            this.transferredAt = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Envelope getSourceEnvelope() {
        return sourceEnvelope;
    }

    public void setSourceEnvelope(Envelope sourceEnvelope) {
        this.sourceEnvelope = sourceEnvelope;
    }

    public Envelope getTargetEnvelope() {
        return targetEnvelope;
    }

    public void setTargetEnvelope(Envelope targetEnvelope) {
        this.targetEnvelope = targetEnvelope;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransferReason getReason() {
        return reason;
    }

    public void setReason(TransferReason reason) {
        this.reason = reason;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
