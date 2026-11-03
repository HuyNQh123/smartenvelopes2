-- ========================================================
-- SMARTENVELOPE DATABASE MIGRATION - V4
-- Savings & Notification Tables
-- Phụ trách: Thành viên 4 & 5
-- ========================================================

-- 11. Bảng user_finance_settings: Cấu hình tài chính người dùng
CREATE TABLE IF NOT EXISTS user_finance_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    default_rule VARCHAR(50) NOT NULL DEFAULT '50/30/20',
    salary_day INT NOT NULL DEFAULT 5,
    warning_threshold INT NOT NULL DEFAULT 80,
    rollover_policy VARCHAR(30) NOT NULL DEFAULT 'SWEEP_TO_PIGGY',
    daily_reminder_time TIME NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Bảng savings_goals: Mục tiêu tiết kiệm dài hạn
CREATE TABLE IF NOT EXISTS savings_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_amount DECIMAL(19, 0) NOT NULL,
    current_amount DECIMAL(19, 0) NOT NULL DEFAULT 0,
    target_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_savings_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE RESTRICT,
    INDEX idx_savings_workspace (workspace_id),
    INDEX idx_savings_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Bảng piggy_banks: Heo đất tích lũy của workspace
CREATE TABLE IF NOT EXISTS piggy_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL UNIQUE,
    total_balance DECIMAL(19, 0) NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_piggy_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Bảng piggy_bank_entries: Biến động dòng tiền heo đất
CREATE TABLE IF NOT EXISTS piggy_bank_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    piggy_bank_id BIGINT NOT NULL,
    amount DECIMAL(19, 0) NOT NULL,
    type VARCHAR(20) NOT NULL,
    source_envelope_id BIGINT NULL,
    savings_goal_id BIGINT NULL,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entries_piggy FOREIGN KEY (piggy_bank_id) REFERENCES piggy_banks (id) ON DELETE CASCADE,
    CONSTRAINT fk_entries_envelope FOREIGN KEY (source_envelope_id) REFERENCES envelopes (id) ON DELETE SET NULL,
    CONSTRAINT fk_entries_savings FOREIGN KEY (savings_goal_id) REFERENCES savings_goals (id) ON DELETE SET NULL,
    INDEX idx_entries_piggy (piggy_bank_id),
    INDEX idx_entries_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Bảng month_closes: Chốt kỳ ngân sách cuối tháng
CREATE TABLE IF NOT EXISTS month_closes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    budget_period_id BIGINT NOT NULL UNIQUE,
    closed_by BIGINT NOT NULL,
    closed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_spent DECIMAL(19, 0) NOT NULL DEFAULT 0,
    total_saved DECIMAL(19, 0) NOT NULL DEFAULT 0,
    notes TEXT NULL,
    CONSTRAINT fk_closes_period FOREIGN KEY (budget_period_id) REFERENCES budget_periods (id) ON DELETE RESTRICT,
    CONSTRAINT fk_closes_user FOREIGN KEY (closed_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Bảng notifications: Thông báo và cảnh báo
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    action_url VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
