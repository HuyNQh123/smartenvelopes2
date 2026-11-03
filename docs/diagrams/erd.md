# SƠ ĐỒ QUAN HỆ THỰC THỂ (ERD) - DỰ ÁN SMARTENVELOPE

Hệ thống cơ sở dữ liệu **SmartEnvelope** được thiết kế chuẩn hóa theo mô hình quan hệ (RDBMS) trên MySQL 8+, phục vụ triết lý quản lý tài chính **Zero-based Budgeting (ZBB)** và phương pháp chia tiền vào phong bì (**Envelope Budgeting**).

Hệ thống gồm 16 bảng thực thể được phân thành 4 phân hệ chính:
1. **Core & Access Control**: `users`, `workspaces`, `memberships`, `categories`.
2. **Accounts & Budgeting (Kỳ ngân sách & Phong bì)**: `accounts`, `budget_periods`, `envelopes`.
3. **Transactions & Audit Ledger (Giao dịch & Kiểm toán)**: `transactions`, `envelope_transfers`, `transaction_audits`.
4. **Savings & Notifications (Tích lũy & Cảnh báo)**: `user_finance_settings`, `savings_goals`, `piggy_banks`, `piggy_bank_entries`, `month_closes`, `notifications`.

---

## 1. Sơ đồ tổng thể toàn hệ thống (Mermaid ERD)

```mermaid
erDiagram
    users ||--o{ workspaces : "owns"
    users ||--o{ memberships : "participates"
    users ||--o{ categories : "creates_custom"
    users ||--o{ transactions : "records"
    users ||--o{ envelope_transfers : "initiates"
    users ||--o{ transaction_audits : "authors"
    users ||--o{ user_finance_settings : "configures"
    users ||--o{ month_closes : "finalizes"
    users ||--o{ notifications : "receives"
    users ||--o{ workspace_invitations : "invites"

    workspaces ||--o{ memberships : "contains"
    workspaces ||--o{ workspace_invitations : "issues"
    workspaces ||--o{ accounts : "manages"
    workspaces ||--o{ budget_periods : "schedules"
    workspaces ||--o{ transactions : "holds"
    workspaces ||--o{ savings_goals : "targets"
    workspaces ||--|| piggy_banks : "accumulates"

    accounts ||--o{ transactions : "funds"

    budget_periods ||--o{ envelopes : "allocates"
    budget_periods ||--o{ envelope_transfers : "scopes"
    budget_periods ||--o| month_closes : "concludes"

    categories ||--o{ envelopes : "classifies"
    categories ||--o{ transactions : "categorizes"

    envelopes ||--o{ transactions : "deducts"
    envelopes ||--o{ envelope_transfers : "source_target"
    envelopes ||--o{ piggy_bank_entries : "sweeps_surplus"

    transactions ||--o{ transaction_audits : "tracks"

    piggy_banks ||--o{ piggy_bank_entries : "logs"
    savings_goals ||--o{ piggy_bank_entries : "receives_funds"

    users {
        bigint id PK
        varchar username UK
        varchar password
        varchar full_name
        varchar email UK
        varchar phone
        varchar avatar_url
        varchar role
        varchar status
        datetime created_at
        datetime updated_at
    }

    workspaces {
        bigint id PK
        varchar name
        varchar type
        varchar description
        bigint owner_id FK
        datetime created_at
        datetime updated_at
    }

    memberships {
        bigint id PK
        bigint workspace_id FK
        bigint user_id FK
        varchar role
        varchar status
        datetime joined_at
    }

    categories {
        bigint id PK
        varchar name
        varchar type
        varchar icon
        varchar color
        bigint user_id FK
        boolean is_system
        datetime created_at
    }

    accounts {
        bigint id PK
        bigint workspace_id FK
        varchar name
        varchar type
        decimal balance
        varchar currency
        varchar account_number
        varchar bank_name
        int version
        datetime created_at
        datetime updated_at
    }

    budget_periods {
        bigint id PK
        bigint workspace_id FK
        varchar name
        int month
        int year
        date start_date
        date end_date
        decimal total_income
        decimal total_allocated
        varchar status
        varchar rollover_policy
        int version
        datetime created_at
        datetime updated_at
    }

    envelopes {
        bigint id PK
        bigint budget_period_id FK
        bigint category_id FK
        varchar name
        decimal allocated_amount
        decimal spent_amount
        decimal remaining_amount
        int warning_threshold
        int version
        datetime created_at
        datetime updated_at
    }

    transactions {
        bigint id PK
        bigint workspace_id FK
        bigint account_id FK
        bigint envelope_id FK
        bigint category_id FK
        bigint user_id FK
        decimal amount
        varchar type
        varchar status
        date transaction_date
        text note
        int version
        datetime created_at
        datetime updated_at
    }

    envelope_transfers {
        bigint id PK
        bigint budget_period_id FK
        bigint from_envelope_id FK
        bigint to_envelope_id FK
        decimal amount
        varchar reason
        varchar note
        bigint transferred_by FK
        datetime transferred_at
    }

    transaction_audits {
        bigint id PK
        bigint transaction_id FK
        varchar action
        text old_values
        text new_values
        bigint changed_by FK
        datetime changed_at
    }

    user_finance_settings {
        bigint id PK
        bigint user_id FK
        varchar default_rule
        int salary_day
        int warning_threshold
        varchar rollover_policy
        time daily_reminder_time
        varchar currency
        datetime updated_at
    }

    savings_goals {
        bigint id PK
        bigint workspace_id FK
        varchar name
        decimal target_amount
        decimal current_amount
        date target_date
        varchar status
        datetime created_at
        datetime updated_at
    }

    piggy_banks {
        bigint id PK
        bigint workspace_id FK
        decimal total_balance
        datetime updated_at
    }

    piggy_bank_entries {
        bigint id PK
        bigint piggy_bank_id FK
        decimal amount
        varchar type
        bigint source_envelope_id FK
        bigint savings_goal_id FK
        varchar note
        datetime created_at
    }

    month_closes {
        bigint id PK
        bigint budget_period_id FK
        bigint closed_by FK
        datetime closed_at
        decimal total_spent
        decimal total_saved
        text notes
    }

    notifications {
        bigint id PK
        bigint user_id FK
        varchar type
        varchar title
        text message
        varchar status
        varchar action_url
        datetime created_at
    }
```

---

## 2. Chi tiết luồng liên kết giữa các bảng

1. **Workspace Ownership & Phân quyền thành viên**:
   - `users (1) -> (N) workspaces`: Mỗi người dùng có thể tạo một hoặc nhiều workspace (ví dụ: "Quỹ cá nhân", "Quỹ gia đình").
   - `workspaces (1) -> (N) memberships <- (N) users`: Quan hệ nhiều-nhiều cho phép mời thành viên khác vào chung workspace với vai trò `OWNER`, `MANAGER`, `WRITER`, `VIEWER`.
2. **Kỳ ngân sách và Phong bì (Zero-based Budgeting)**:
   - `workspaces (1) -> (N) budget_periods`: Mỗi tháng một workspace lập 1 kỳ ngân sách.
   - `budget_periods (1) -> (N) envelopes`: Trong một kỳ, thu nhập được phân bổ 100% vào các phong bì (`allocated_amount`).
   - `categories (1) -> (N) envelopes`: Mỗi phong bì đại diện cho một danh mục chi tiêu cụ thể.
3. **Ghi nhận chi tiêu và Sổ cái kép**:
   - `accounts (1) -> (N) transactions`: Chi tiêu làm giảm số dư tài khoản thực tế (`accounts.balance`).
   - `envelopes (1) -> (N) transactions`: Chi tiêu làm tăng số đã chi (`envelopes.spent_amount`) và giảm số dư phong bì (`envelopes.remaining_amount`).
   - `transactions (1) -> (N) transaction_audits`: Mọi hành động SỬA/XÓA/HỦY giao dịch đều ghi nhật ký kiểm toán bất biến.
4. **Bù đắp ngân sách và Kết thúc kỳ (Rollover)**:
   - `envelopes (from/to) -> (N) envelope_transfers`: Chuyển tiền giữa các phong bì khi phát sinh chi vượt hạn mức.
   - `budget_periods (1) -> (1) month_closes`: Chốt sổ cuối tháng, tính toán tổng tiết kiệm.
   - `envelopes -> (N) piggy_bank_entries -> (1) piggy_banks`: Quét tiền dư các phong bì vào Heo đất tích lũy theo chính sách `SWEEP_TO_PIGGY`.

