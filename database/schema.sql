-- ========================================================
-- SMARTENVELOPE - BẢN THIẾT KẾ SCHEMA CƠ SỞ DỮ LIỆU ĐẦY ĐỦ
-- Hệ quản trị: MySQL 8.0+
-- Encoding: UTF-8 (utf8mb4 / utf8mb4_unicode_ci)
-- ========================================================

CREATE DATABASE IF NOT EXISTS smart_envelope
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_envelope;

-- 1. Bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NULL,
    avatar_url VARCHAR(255) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_username (username),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng workspaces
CREATE TABLE IF NOT EXISTS workspaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    description VARCHAR(255) NULL,
    owner_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workspaces_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_workspaces_owner (owner_id),
    INDEX idx_workspaces_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng memberships
CREATE TABLE IF NOT EXISTS memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_memberships_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE,
    CONSTRAINT fk_memberships_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY uk_workspace_user (workspace_id, user_id),
    INDEX idx_memberships_workspace (workspace_id),
    INDEX idx_memberships_user (user_id),
    INDEX idx_memberships_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon VARCHAR(50) NULL,
    color VARCHAR(20) NULL,
    user_id BIGINT NULL,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_categories_user (user_id),
    INDEX idx_categories_type (type),
    INDEX idx_categories_system (is_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng accounts
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'BANK',
    balance DECIMAL(19, 0) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    account_number VARCHAR(50) NULL,
    bank_name VARCHAR(100) NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE RESTRICT,
    INDEX idx_accounts_workspace (workspace_id),
    INDEX idx_accounts_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng budget_periods
CREATE TABLE IF NOT EXISTS budget_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_income DECIMAL(19, 0) NOT NULL DEFAULT 0,
    total_allocated DECIMAL(19, 0) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    rollover_policy VARCHAR(30) NOT NULL DEFAULT 'SWEEP_TO_PIGGY',
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_periods_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_budget_period_month_year (workspace_id, month, year),
    INDEX idx_budget_periods_workspace (workspace_id),
    INDEX idx_budget_periods_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng envelopes
CREATE TABLE IF NOT EXISTS envelopes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    budget_period_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    allocated_amount DECIMAL(19, 0) NOT NULL DEFAULT 0,
    spent_amount DECIMAL(19, 0) NOT NULL DEFAULT 0,
    remaining_amount DECIMAL(19, 0) NOT NULL DEFAULT 0,
    warning_threshold INT NOT NULL DEFAULT 80,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_envelopes_budget_period FOREIGN KEY (budget_period_id) REFERENCES budget_periods (id) ON DELETE RESTRICT,
    CONSTRAINT fk_envelopes_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    INDEX idx_envelopes_period (budget_period_id),
    INDEX idx_envelopes_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    envelope_id BIGINT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19, 0) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    transaction_date DATE NOT NULL,
    note TEXT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_envelope FOREIGN KEY (envelope_id) REFERENCES envelopes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_transactions_workspace (workspace_id),
    INDEX idx_transactions_account (account_id),
    INDEX idx_transactions_envelope (envelope_id),
    INDEX idx_transactions_date (transaction_date),
    INDEX idx_transactions_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng envelope_transfers
CREATE TABLE IF NOT EXISTS envelope_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    budget_period_id BIGINT NOT NULL,
    from_envelope_id BIGINT NOT NULL,
    to_envelope_id BIGINT NOT NULL,
    amount DECIMAL(19, 0) NOT NULL,
    reason VARCHAR(30) NOT NULL DEFAULT 'COVER_EXPENSE',
    note VARCHAR(255) NULL,
    transferred_by BIGINT NOT NULL,
    transferred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfers_period FOREIGN KEY (budget_period_id) REFERENCES budget_periods (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfers_from FOREIGN KEY (from_envelope_id) REFERENCES envelopes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfers_to FOREIGN KEY (to_envelope_id) REFERENCES envelopes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfers_user FOREIGN KEY (transferred_by) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_transfers_period (budget_period_id),
    INDEX idx_transfers_from (from_envelope_id),
    INDEX idx_transfers_to (to_envelope_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bảng transaction_audits
CREATE TABLE IF NOT EXISTS transaction_audits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_values TEXT NULL,
    new_values TEXT NULL,
    changed_by BIGINT NOT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audits_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE,
    CONSTRAINT fk_audits_user FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_audits_transaction (transaction_id),
    INDEX idx_audits_user (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bảng user_finance_settings
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

-- 12. Bảng savings_goals
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

-- 13. Bảng piggy_banks
CREATE TABLE IF NOT EXISTS piggy_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL UNIQUE,
    total_balance DECIMAL(19, 0) NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_piggy_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Bảng piggy_bank_entries
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

-- 15. Bảng month_closes
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

-- 16. Bảng notifications
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

-- Nạp 10 danh mục mặc định
INSERT INTO categories (name, type, icon, color, user_id, is_system, created_at) VALUES
('Ăn uống & Đi chợ', 'EXPENSE', 'bi-cup-hot', '#FF5722', NULL, TRUE, NOW()),
('Tiền nhà & Tiện ích', 'EXPENSE', 'bi-house-door', '#3F51B5', NULL, TRUE, NOW()),
('Học tập & Sách vở', 'EXPENSE', 'bi-book', '#009688', NULL, TRUE, NOW()),
('Xăng xe & Đi lại', 'EXPENSE', 'bi-bicycle', '#FF9800', NULL, TRUE, NOW()),
('Mua sắm & Giải trí', 'EXPENSE', 'bi-bag', '#E91E63', NULL, TRUE, NOW()),
('Quỹ dự phòng khẩn cấp', 'EXPENSE', 'bi-shield-check', '#4CAF50', NULL, TRUE, NOW()),
('Sức khỏe & Y tế', 'EXPENSE', 'bi-heart-pulse', '#F44336', NULL, TRUE, NOW()),
('Hiếu hỉ & Đám tiệc', 'EXPENSE', 'bi-gift', '#9C27B0', NULL, TRUE, NOW()),
('Lương & Thưởng chính', 'INCOME', 'bi-cash-stack', '#2E7D32', NULL, TRUE, NOW()),
('Thu nhập phụ & Làm thêm', 'INCOME', 'bi-graph-up-arrow', '#1565C0', NULL, TRUE, NOW());
