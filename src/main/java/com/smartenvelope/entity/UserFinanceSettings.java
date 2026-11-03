package com.smartenvelope.entity;

import com.smartenvelope.enums.RolloverPolicy;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity đại diện cho cấu hình quản lý tài chính cá nhân của người dùng.
 * Bao gồm quy tắc ngân sách mặc định (ví dụ 50/30/20, 60/20/20), ngày nhận lương,
 * ngưỡng cảnh báo chi tiêu và giờ nhắc nhở ghi chép hàng ngày.
 * Phụ trách bởi: Thành viên 4 & Thành viên 1.
 */
@Entity
@Table(name = "user_finance_settings")
public class UserFinanceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "default_rule", nullable = false, length = 50)
    private String defaultRule = "50/30/20";

    @Column(name = "salary_day", nullable = false)
    private Integer salaryDay = 5;

    @Column(name = "warning_threshold", nullable = false)
    private Integer warningThreshold = 80;

    @Enumerated(EnumType.STRING)
    @Column(name = "rollover_policy", nullable = false, length = 30)
    private RolloverPolicy rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;

    @Column(name = "daily_reminder_time")
    private LocalTime dailyReminderTime;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "VND";

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserFinanceSettings() {
    }

    public UserFinanceSettings(User user, String defaultRule, Integer salaryDay, Integer warningThreshold) {
        this.user = user;
        this.defaultRule = defaultRule != null ? defaultRule : "50/30/20";
        this.salaryDay = salaryDay != null ? salaryDay : 5;
        this.warningThreshold = warningThreshold != null ? warningThreshold : 80;
        this.rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;
        this.currency = "VND";
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
        if (this.defaultRule == null) {
            this.defaultRule = "50/30/20";
        }
        if (this.salaryDay == null) {
            this.salaryDay = 5;
        }
        if (this.warningThreshold == null) {
            this.warningThreshold = 80;
        }
        if (this.rolloverPolicy == null) {
            this.rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;
        }
        if (this.currency == null) {
            this.currency = "VND";
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDefaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(String defaultRule) {
        this.defaultRule = defaultRule;
    }

    public Integer getSalaryDay() {
        return salaryDay;
    }

    public void setSalaryDay(Integer salaryDay) {
        this.salaryDay = salaryDay;
    }

    public Integer getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(Integer warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public RolloverPolicy getRolloverPolicy() {
        return rolloverPolicy;
    }

    public void setRolloverPolicy(RolloverPolicy rolloverPolicy) {
        this.rolloverPolicy = rolloverPolicy;
    }

    public LocalTime getDailyReminderTime() {
        return dailyReminderTime;
    }

    public void setDailyReminderTime(LocalTime dailyReminderTime) {
        this.dailyReminderTime = dailyReminderTime;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

