-- ========================================================
-- SMARTENVELOPE DATABASE MIGRATION - V3
-- Transaction Tables: transactions, envelope_transfers, transaction_audits
-- Phụ trách: Thành viên 3
-- ========================================================

-- 8. Bảng transactions: Giao dịch thu chi tài chính
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

-- 9. Bảng envelope_transfers: Lịch sử điều chuyển và bù đắp ngân sách giữa các phong bì
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

-- 10. Bảng transaction_audits: Nhật ký kiểm toán bất biến
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
