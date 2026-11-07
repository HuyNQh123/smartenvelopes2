package com.smartenvelope.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity đại diện cho phong bì ngân sách (chứa hạn mức chi tiêu cho từng danh mục theo kỳ).
 * Phụ trách bởi: Thành viên 2 & tương tác bởi Thành viên 3.
 */
@Entity
@Table(name = "envelopes")
public class Envelope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "allocated_amount", precision = 15, scale = 2)
    private BigDecimal allocatedAmount = BigDecimal.ZERO;

    @Column(name = "spent_amount", precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_locked")
    private boolean locked = false;

    @Version
    @Column(name = "version")
    private Long version;

    public Envelope() {
    }

    public Envelope(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
        this.allocatedAmount = balance;
        this.spentAmount = BigDecimal.ZERO;
        this.locked = false;
    }

    public Envelope(String name, BigDecimal balance, boolean locked) {
        this.name = name;
        this.balance = balance;
        this.allocatedAmount = balance;
        this.spentAmount = BigDecimal.ZERO;
        this.locked = locked;
    }

    // --- Business methods ---

    public void spend(BigDecimal amount) {
        if (amount != null) {
            if (this.balance == null) {
                this.balance = BigDecimal.ZERO;
            }
            this.balance = this.balance.subtract(amount);
            if (this.spentAmount == null) {
                this.spentAmount = BigDecimal.ZERO;
            }
            this.spentAmount = this.spentAmount.add(amount);
        }
    }

    public void allocate(BigDecimal amount) {
        if (amount != null) {
            if (this.balance == null) {
                this.balance = BigDecimal.ZERO;
            }
            this.balance = this.balance.add(amount);
            if (this.allocatedAmount == null) {
                this.allocatedAmount = BigDecimal.ZERO;
            }
            this.allocatedAmount = this.allocatedAmount.add(amount);
        }
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getBalance() {
        return balance != null ? balance : BigDecimal.ZERO;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
