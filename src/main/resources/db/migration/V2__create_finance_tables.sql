-- ========================================================
-- SMARTENVELOPE DATABASE MIGRATION - V2
-- Finance Tables: accounts, budget_periods, envelopes
-- Phụ trách: Thành viên 2 & 4
-- ========================================================

-- 5. Bảng accounts: Tài khoản thanh toán thực tế (tiền mặt, ngân hàng, thẻ, tiết kiệm)
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

-- 6. Bảng budget_periods: Kỳ ngân sách theo tháng (Zero-based Budgeting)
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

-- 7. Bảng envelopes: Chiếc phong bì hạn mức chi tiêu trong kỳ
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
