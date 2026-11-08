package com.smartenvelope.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho Heo đất tích lũy của workspace (quỹ tiết kiệm tự động từ
 * tiền dư phong bì). Mỗi workspace sở hữu duy nhất 1 heo đất. Phụ trách bởi:
 * Thành viên 5 (Savings, Piggy Bank & Analytics).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "piggy_banks")
public class PiggyBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false, unique = true)
    private Workspace workspace;

    @Column(name = "total_balance", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PiggyBank(Workspace workspace, BigDecimal totalBalance) {
        this.workspace = workspace;
        this.totalBalance = totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {

        this.updatedAt = LocalDateTime.now();

        if (this.totalBalance == null) {

            this.totalBalance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = LocalDateTime.now();
    }

}
