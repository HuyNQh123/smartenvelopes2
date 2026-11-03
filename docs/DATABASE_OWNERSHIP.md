# PHÂN CHIA QUYỀN SỞ HỮU CƠ SỞ DỮ LIỆU & THỰC THỂ (DATABASE OWNERSHIP)

Tài liệu này xác định ranh giới trách nhiệm, bảng CSDL và các Entity Java tương ứng cho từng thành viên trong nhóm 5 người của dự án **SmartEnvelope**.

---

## 1. Bảng ma trận tổng hợp quyền sở hữu (Ownership Matrix)

| Bảng CSDL | JPA Entity tương ứng | Spring Data Repository | Thành viên phụ trách chính | Thành viên phối hợp |
| :--- | :--- | :--- | :---: | :---: |
| `users` | `User.java` | `UserRepository` | **Thành viên 1** | Thành viên 2, 3, 4, 5 |
| `workspaces` | `Workspace.java` | `WorkspaceRepository` | **Thành viên 1** | Thành viên 2 |
| `memberships` | `Membership.java` | `MembershipRepository` | **Thành viên 1** | Thành viên 2 |
| `categories` | `Category.java` | `CategoryRepository` | **Thành viên 2** | Thành viên 3, 4 |
| `accounts` | `Account.java` | `AccountRepository` | **Thành viên 2** | Thành viên 3 |
| `budget_periods` | `BudgetPeriod.java` | `BudgetPeriodRepository`| **Thành viên 4** | Thành viên 3, 5 |
| `envelopes` | `Envelope.java` | `EnvelopeRepository` | **Thành viên 4** | Thành viên 3 |
| `transactions` | `Transaction.java` | `TransactionRepository` | **Thành viên 3** | Thành viên 2, 4 |
| `envelope_transfers`| `EnvelopeTransfer.java`| `EnvelopeTransferRepository`| **Thành viên 3** | Thành viên 4 |
| `transaction_audits`| `TransactionAudit.java`| `TransactionAuditRepository`| **Thành viên 3** | Thành viên 1 |
| `user_finance_settings`| `UserFinanceSettings.java`| `UserFinanceSettingsRepository`| **Thành viên 4** | Thành viên 1 |
| `savings_goals` | `SavingsGoal.java` | `SavingsGoalRepository` | **Thành viên 5** | Thành viên 2 |
| `piggy_banks` | `PiggyBank.java` | `PiggyBankRepository` | **Thành viên 5** | Thành viên 4 |
| `piggy_bank_entries`| `PiggyBankEntry.java` | `PiggyBankEntryRepository` | **Thành viên 5** | Thành viên 4 |
| `month_closes` | `MonthClose.java` | `MonthCloseRepository` | **Thành viên 4** | Thành viên 5 |
| `notifications` | `Notification.java` | `NotificationRepository`| **Thành viên 4 & 5**| Thành viên 1, 3 |

---

## 2. Chi tiết nhiệm vụ theo từng thành viên

### 👤 Thành viên 1: Authentication, Authorization & User Management
- **Thực thể sở hữu chính**: `User`, `Workspace`, `Membership`.
- **Phối hợp**: `UserFinanceSettings` (cùng Thành viên 4), `TransactionAudit` (cùng Thành viên 3).
- **Phạm vi trách nhiệm trong CSDL**:
  - Quản lý vòng đời tài khoản người dùng (`ACTIVE`, `LOCKED`, `INACTIVE`), phân quyền hệ thống (`USER`, `ADMIN`).
  - Quản lý việc khởi tạo Workspace cá nhân mặc định khi người dùng đăng ký mới.
  - Xây dựng nghiệp vụ lời mời tham gia Workspace chia sẻ (quỹ chung/gia đình) và phân quyền vai trò trong quỹ (`OWNER`, `MANAGER`, `WRITER`, `VIEWER`).

### 👤 Thành viên 2: Account & Category Management
- **Thực thể sở hữu chính**: `Category`, `Account`.
- **Phối hợp**: `Workspace` (truy vấn danh sách tài khoản theo workspace), `Transaction` (cung cấp tài khoản nguồn).
- **Phạm vi trách nhiệm trong CSDL**:
  - Quản lý danh mục thu/chi hệ thống (`is_system = true`) và danh mục tùy chỉnh do người dùng tự tạo.
  - Quản lý các loại tài khoản tiền tệ (tiền mặt, tài khoản ngân hàng, thẻ tín dụng, tài khoản tiết kiệm).
  - Cập nhật số dư tài khoản khi có giao dịch thu/chi, đảm bảo xử lý đồng thời qua trường `version` (Optimistic Locking).

### 👤 Thành viên 3: Transaction & Ledger Processing
- **Thực thể sở hữu chính**: `Transaction`, `EnvelopeTransfer`, `TransactionAudit`.
- **Phối hợp**: `Envelope` (trừ số dư phong bì khi chi tiêu), `Account` (trừ số dư tài khoản), `BudgetPeriod` (kiểm tra kỳ đang áp dụng).
- **Phạm vi trách nhiệm trong CSDL**:
  - Ghi nhận giao dịch chi tiêu (`EXPENSE`) gắn với phong bì cụ thể và tài khoản thanh toán.
  - Ghi nhận giao dịch thu nhập (`INCOME`) nạp tiền vào tài khoản.
  - Nghiệp vụ điều chuyển/bù đắp ngân sách (`EnvelopeTransfer`): trừ tiền phong bì A, bù tiền phong bì B khi có nhu cầu bù đắp chi phí phát sinh.
  - Tự động ghi nhận `TransactionAudit` mỗi khi một giao dịch được tạo, sửa hoặc hủy.

### 👤 Thành viên 4: Budget Planning & Envelope Logic
- **Thực thể sở hữu chính**: `BudgetPeriod`, `Envelope`, `UserFinanceSettings`, `MonthClose` (logic đóng kỳ).
- **Phối hợp**: `Notification` (bắn cảnh báo bội chi), `PiggyBank` (quét tiền dư cuối tháng sang Thành viên 5).
- **Phạm vi trách nhiệm trong CSDL**:
  - Thiết lập kỳ ngân sách tháng (`DRAFT`) theo triết lý Zero-based Budgeting.
  - Phân bổ ngân sách vào các phong bì theo quy tắc tài chính gợi ý (50/30/20, 60/20/20).
  - Kiểm tra điều kiện chốt ngân sách (`CONFIRMED`): Tổng tiền phân bổ phải bằng đúng 100% thu nhập dự kiến.
  - Theo dõi tỷ lệ chi tiêu của từng phong bì, kích hoạt cảnh báo khi đạt ngưỡng (`warning_threshold`, mặc định 80%).
  - Kích hoạt nghiệp vụ đóng kỳ (`MonthClose`): đóng băng kỳ ngân sách và quét tiền dư sang heo đất.

### 👤 Thành viên 5: Savings, Piggy Bank & Financial Analytics
- **Thực thể sở hữu chính**: `SavingsGoal`, `PiggyBank`, `PiggyBankEntry`.
- **Phối hợp**: `MonthClose` (tiếp nhận kết quả tiền tiết kiệm), `Notification` (nhắc nhở ghi chép, chúc mừng hoàn thành mục tiêu), `Transaction` & `Envelope` (tính toán báo cáo thống kê, biểu đồ tài chính).
- **Phạm vi trách nhiệm trong CSDL**:
  - Quản lý các mục tiêu tiết kiệm dài hạn (`SavingsGoal`) và theo dõi tiến độ hoàn thành.
  - Quản lý Heo đất tích lũy (`PiggyBank`) và lịch sử biến động (`PiggyBankEntry`).
  - Tiếp nhận dòng tiền quét dư tự động (`SWEEP`) từ nghiệp vụ đóng kỳ cuối tháng của Thành viên 4.
  - Thực hiện các truy vấn phân tích, tổng hợp dữ liệu báo cáo chi tiêu, dòng tiền và phân tích xu hướng cho trang Dashboard và Report.

