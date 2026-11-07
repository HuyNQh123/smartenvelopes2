package com.smartenvelope.entity;

import com.smartenvelope.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho giao dịch tài chính (Thu, Chi, Chuyển khoản).
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type = TransactionType.EXPENSE;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id")
    private Envelope envelope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_transfer_id")
    private EnvelopeTransfer relatedTransfer;

    public Transaction() {
    }

    public Transaction(User user, Account account, Envelope envelope, Category category,
                       BigDecimal amount, TransactionType type, LocalDateTime transactionDate, String note) {
        this.user = user;
        this.account = account;
        this.envelope = envelope;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDateTime.now();
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(User user, Account account, Envelope envelope, Category category,
                       BigDecimal amount, TransactionType type, LocalDateTime transactionDate,
                       String note, String requestId) {
        this.user = user;
        this.account = account;
        this.envelope = envelope;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDateTime.now();
        this.note = note;
        this.requestId = requestId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public EnvelopeTransfer getRelatedTransfer() {
        return relatedTransfer;
    }

    public void setRelatedTransfer(EnvelopeTransfer relatedTransfer) {
        this.relatedTransfer = relatedTransfer;
    }
}
