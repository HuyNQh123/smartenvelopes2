# TỪ ĐIỂN DỮ LIỆU TOÀN DIỆN (DATA DICTIONARY) - SMARTENVELOPE

Hệ thống cơ sở dữ liệu **SmartEnvelope** lưu trữ bằng MySQL 8+, bảng mã `utf8mb4_unicode_ci`. Mọi trường tiền tệ được lưu dưới dạng `DECIMAL(19, 0)` đại diện cho VNĐ không có số lẻ thập phân.

---

## 1. Bảng `users` (Người dùng hệ thống)
Quản lý tài khoản đăng nhập, thông tin định danh và quyền quản trị hệ thống.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh duy nhất người dùng. |
| `username` | VARCHAR(50) | UK | NO | | Tên đăng nhập duy nhất. |
| `password` | VARCHAR(255) | | NO | | Mật khẩu đã được mã hóa BCrypt. |
| `full_name` | VARCHAR(100) | | NO | | Họ và tên hiển thị của người dùng. |
| `email` | VARCHAR(100) | UK | NO | | Email liên hệ, nhận thông báo và khôi phục mật khẩu. |
| `phone` | VARCHAR(20) | | YES | NULL | Số điện thoại liên hệ. |
| `avatar_url` | VARCHAR(255) | | YES | NULL | Đường dẫn ảnh đại diện cá nhân. |
| `role` | ENUM('USER', 'ADMIN') | | NO | 'USER' | Vai trò hệ thống: Người dùng phổ thông hoặc Quản trị viên. |
| `status` | ENUM('ACTIVE', 'LOCKED', 'INACTIVE') | | NO | 'ACTIVE' | Trạng thái tài khoản người dùng. |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm đăng ký tài khoản. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm cập nhật hồ sơ gần nhất. |

---

## 2. Bảng `workspaces` (Không gian quản lý tài chính)
Phân tách dữ liệu giữa các quỹ chi tiêu cá nhân hoặc quỹ chung của gia đình/nhóm.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh workspace. |
| `name` | VARCHAR(100) | | NO | | Tên không gian tài chính (vd: "Quỹ cá nhân", "Gia đình"). |
| `type` | ENUM('PERSONAL', 'SHARED') | | NO | 'PERSONAL' | Loại workspace: Cá nhân hoặc Chia sẻ chung. |
| `description` | VARCHAR(255) | | YES | NULL | Ghi chú hoặc mô tả ngắn về không gian tài chính. |
| `owner_id` | BIGINT | FK (users.id) | NO | | Người dùng sáng lập và sở hữu workspace này. |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm khởi tạo workspace. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm cập nhật gần nhất. |

---

## 3. Bảng `memberships` (Thành viên quỹ chung)
Quản lý quyền hạn và trạng thái của người dùng tham gia vào workspace.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh thành viên quỹ. |
| `workspace_id` | BIGINT | FK, UK(1) | NO | | Workspace tham gia. |
| `user_id` | BIGINT | FK, UK(2) | NO | | Người dùng được cấp quyền. |
| `role` | ENUM('OWNER', 'MANAGER', 'WRITER', 'VIEWER') | | NO | 'VIEWER' | Quyền hạn: Chủ quỹ, Quản lý, Nhập liệu, Chỉ xem. |
| `status` | ENUM('PENDING', 'ACCEPTED', 'REJECTED') | | NO | 'PENDING' | Trạng thái chấp thuận lời mời vào quỹ. |
| `joined_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tham gia quỹ. |

---

## 4. Bảng `categories` (Danh mục thu/chi)
Danh mục phân loại thu nhập hoặc chi tiêu.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh danh mục. |
| `name` | VARCHAR(100) | | NO | | Tên danh mục (Ăn uống, Tiền nhà, Lương...). |
| `type` | ENUM('EXPENSE', 'INCOME') | | NO | | Phân loại: Chi tiêu hoặc Thu nhập. |
| `icon` | VARCHAR(50) | | YES | NULL | Mã biểu tượng icon (Bootstrap Icons / FontAwesome). |
| `color` | VARCHAR(20) | | YES | NULL | Mã màu hiển thị trên giao diện (Hex code). |
| `user_id` | BIGINT | FK (users.id) | YES | NULL | Người dùng tự tạo danh mục này (NULL nếu hệ thống cấp). |
| `is_system` | BOOLEAN | | NO | FALSE | Đánh dấu danh mục mặc định toàn hệ thống. |
| `is_active` | BOOLEAN | | NO | TRUE | Trạng thái kích hoạt (TRUE: đang dùng, FALSE: vô hiệu hóa). |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tạo danh mục. |

---

## 5. Bảng `accounts` (Tài khoản thanh toán)
Tài khoản thực tế nơi tiền được lưu trữ vật lý (ngân hàng, ví tiền mặt, sổ tiết kiệm).

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh tài khoản. |
| `workspace_id` | BIGINT | FK | NO | | Không gian tài chính sở hữu tài khoản này. |
| `name` | VARCHAR(100) | | NO | | Tên tài khoản (vd: "Vietcombank", "Tiền mặt ví"). |
| `type` | ENUM('CASH', 'BANK', 'CREDIT', 'SAVINGS') | | NO | 'BANK' | Loại hình tài khoản. |
| `balance` | DECIMAL(19, 0) | | NO | 0 | Số dư khả dụng hiện tại (VNĐ). |
| `currency` | VARCHAR(10) | | NO | 'VND' | Đơn vị tiền tệ. |
| `account_number` | VARCHAR(50) | | YES | NULL | Số tài khoản ngân hàng hoặc số thẻ (tùy chọn). |
| `bank_name` | VARCHAR(100) | | YES | NULL | Tên ngân hàng phát hành. |
| `version` | INT | | NO | 0 | Phiên bản khóa lạc quan (Optimistic Locking). |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm khởi tạo tài khoản. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm số dư biến động gần nhất. |

---

## 6. Bảng `budget_periods` (Kỳ ngân sách tháng)
Kỳ ngân sách chu kỳ (tháng/năm) theo triết lý Zero-based Budgeting.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh kỳ ngân sách. |
| `workspace_id` | BIGINT | FK, UK(1) | NO | | Workspace áp dụng kỳ ngân sách. |
| `name` | VARCHAR(100) | | NO | | Tên kỳ ngân sách (vd: "Ngân sách Tháng 11/2026"). |
| `month` | INT | UK(2) | NO | | Tháng áp dụng (1 - 12). |
| `year` | INT | UK(3) | NO | | Năm áp dụng (vd: 2026). |
| `start_date` | DATE | | NO | | Ngày bắt đầu chu kỳ ngân sách. |
| `end_date` | DATE | | NO | | Ngày kết thúc chu kỳ ngân sách. |
| `total_income` | DECIMAL(19, 0) | | NO | 0 | Tổng thu nhập dự kiến đem đi phân bổ trong kỳ. |
| `total_allocated`| DECIMAL(19, 0) | | NO | 0 | Tổng số tiền đã phân bổ vào tất cả phong bì. |
| `status` | ENUM('DRAFT', 'CONFIRMED', 'CLOSED') | | NO | 'DRAFT' | Trạng thái: Đang soạn thảo, Đã chốt, Đã đóng kỳ. |
| `rollover_policy`| ENUM('SWEEP_TO_PIGGY', 'CARRY_FORWARD')| | NO | 'SWEEP_TO_PIGGY'| Chính sách xử lý số dư cuối tháng. |
| `version` | INT | | NO | 0 | Phiên bản khóa lạc quan (Optimistic Locking). |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tạo kỳ ngân sách. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm cập nhật kỳ ngân sách. |

---

## 7. Bảng `envelopes` (Chiếc phong bì ngân sách)
Phong bì giới hạn chi tiêu cho từng danh mục trong kỳ ngân sách.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh phong bì. |
| `budget_period_id`| BIGINT | FK | NO | | Kỳ ngân sách chứa phong bì này. |
| `category_id` | BIGINT | FK | NO | | Danh mục chi tiêu tương ứng với phong bì. |
| `name` | VARCHAR(100) | | NO | | Tên hiển thị phong bì (vd: "Phong bì Tiền ăn"). |
| `allocated_amount`| DECIMAL(19, 0)| | NO | 0 | Hạn mức phân bổ ban đầu cho phong bì. |
| `spent_amount` | DECIMAL(19, 0) | | NO | 0 | Tổng số tiền đã chi tiêu qua phong bì này. |
| `remaining_amount`| DECIMAL(19, 0)| | NO | 0 | Số dư khả dụng còn lại trong phong bì. |
| `warning_threshold`| INT | | NO | 80 | Ngưỡng chi tiêu cảnh báo (% so với hạn mức phân bổ). |
| `version` | INT | | NO | 0 | Phiên bản khóa lạc quan (Optimistic Locking). |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tạo phong bì. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm cập nhật số dư phong bì. |

---

## 8. Bảng `transactions` (Giao dịch thu/chi tài chính)
Nhật ký các lần người dùng chi tiêu hoặc nhận thu nhập.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh giao dịch. |
| `workspace_id` | BIGINT | FK | NO | | Workspace diễn ra giao dịch. |
| `account_id` | BIGINT | FK | NO | | Tài khoản nguồn thanh toán hoặc nhận tiền. |
| `envelope_id` | BIGINT | FK | YES | NULL | Phong bì chịu chi phí (bắt buộc với EXPENSE, NULL với INCOME). |
| `category_id` | BIGINT | FK | NO | | Danh mục chi tiêu / thu nhập. |
| `user_id` | BIGINT | FK | NO | | Người dùng thực hiện ghi nhận giao dịch. |
| `amount` | DECIMAL(19, 0) | | NO | | Số tiền giao dịch (luôn dương). |
| `type` | ENUM('INCOME', 'EXPENSE') | | NO | | Loại giao dịch: Thu nhập hoặc Chi tiêu. |
| `status` | ENUM('COMPLETED', 'ADJUSTED', 'CANCELLED') | | NO | 'COMPLETED' | Trạng thái: Thành công, Đã sửa, Đã hủy. |
| `transaction_date`| DATE | | NO | | Ngày giao dịch thực tế. |
| `note` | TEXT | | YES | NULL | Ghi chú chi tiết về khoản chi/thu. |
| `version` | INT | | NO | 0 | Phiên bản khóa lạc quan (Optimistic Locking). |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tạo giao dịch trên hệ thống. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm chỉnh sửa giao dịch. |

---

## 9. Bảng `envelope_transfers` (Điều chuyển ngân sách giữa các phong bì)
Lịch sử bù đắp thiếu hụt ngân sách hoặc tái phân bổ hạn mức giữa hai phong bì trong cùng một kỳ.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh lượt điều chuyển. |
| `budget_period_id`| BIGINT | FK | NO | | Kỳ ngân sách diễn ra điều chuyển. |
| `from_envelope_id`| BIGINT | FK | NO | | Phong bì trích tiền (bị giảm hạn mức/số dư). |
| `to_envelope_id` | BIGINT | FK | NO | | Phong bì nhận tiền bù đắp (được tăng hạn mức/số dư). |
| `amount` | DECIMAL(19, 0) | | NO | | Số tiền chuyển giao giữa hai phong bì. |
| `reason` | ENUM('COVER_EXPENSE', 'REPLAN') | | NO | 'COVER_EXPENSE' | Lý do: Bù khoản chi vượt hạn mức hoặc Tái lập kế hoạch. |
| `note` | VARCHAR(255) | | YES | NULL | Ghi chú lý do cụ thể. |
| `transferred_by` | BIGINT | FK (users.id) | NO | | Người dùng thực hiện điều chuyển. |
| `transferred_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm giao dịch điều chuyển hoàn tất. |

---

## 10. Bảng `transaction_audits` (Nhật ký kiểm toán giao dịch)
Lịch sử truy vết mọi hành vi thay đổi trên giao dịch (Audit Trail bất biến).

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh bản ghi kiểm toán. |
| `transaction_id`| BIGINT | FK | NO | | Giao dịch bị tác động. |
| `action` | VARCHAR(50) | | NO | | Hành động thực hiện: `CREATE`, `UPDATE`, `CANCEL`. |
| `old_values` | TEXT | | YES | NULL | Dữ liệu cũ trước khi thay đổi (JSON formatted). |
| `new_values` | TEXT | | YES | NULL | Dữ liệu mới sau khi thay đổi (JSON formatted). |
| `changed_by` | BIGINT | FK (users.id) | NO | | Người dùng thực hiện thao tác. |
| `changed_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm xảy ra thao tác kiểm toán. |

---

## 11. Bảng `user_finance_settings` (Cấu hình tài chính cá nhân)
Thiết lập ngân sách và thói quen tài chính của từng người dùng.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh cấu hình. |
| `user_id` | BIGINT | FK, UK | NO | | Người dùng sở hữu cấu hình. |
| `default_rule` | VARCHAR(50) | | NO | '50/30/20' | Quy tắc phân bổ ngân sách gợi ý mặc định. |
| `salary_day` | INT | | NO | 5 | Ngày nhận lương hàng tháng (1 - 31). |
| `warning_threshold`| INT | | NO | 80 | Ngưỡng cảnh báo chi tiêu mặc định khi tạo phong bì (%). |
| `rollover_policy`| ENUM('SWEEP_TO_PIGGY', 'CARRY_FORWARD')| | NO | 'SWEEP_TO_PIGGY'| Chính sách xử lý số dư dư thừa mặc định. |
| `daily_reminder_time`| TIME | | YES | NULL | Giờ hệ thống gửi thông báo nhắc ghi chép chi tiêu mỗi ngày. |
| `currency` | VARCHAR(10) | | NO | 'VND' | Đơn vị tiền tệ hiển thị. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm cấu hình được cập nhật. |

---

## 12. Bảng `savings_goals` (Mục tiêu tiết kiệm)
Theo dõi tiến độ tích lũy cho các kế hoạch tài chính trong tương lai.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh mục tiêu tiết kiệm. |
| `workspace_id` | BIGINT | FK | NO | | Không gian tài chính sở hữu mục tiêu. |
| `name` | VARCHAR(100) | | NO | | Tên mục tiêu (vd: "Mua laptop mới", "Quỹ khẩn cấp"). |
| `target_amount` | DECIMAL(19, 0) | | NO | | Số tiền mục tiêu cần tích lũy (VNĐ). |
| `current_amount`| DECIMAL(19, 0) | | NO | 0 | Số tiền hiện đã tích lũy được (VNĐ). |
| `target_date` | DATE | | YES | NULL | Hạn chót dự kiến hoàn thành mục tiêu. |
| `status` | ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') | | NO | 'ACTIVE' | Trạng thái của mục tiêu. |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tạo mục tiêu. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm cập nhật số tiền tích lũy gần nhất. |

---

## 13. Bảng `piggy_banks` (Heo đất tích lũy tự động)
Ví tích lũy trung gian nhận tiền dư tự động cuối tháng từ các phong bì chi tiêu.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh heo đất. |
| `workspace_id` | BIGINT | FK, UK | NO | | Mỗi workspace sở hữu duy nhất 1 heo đất. |
| `total_balance` | DECIMAL(19, 0) | | NO | 0 | Tổng số tiền hiện đang được tích lũy trong heo đất. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm số dư heo đất biến động gần nhất. |

---

## 14. Bảng `piggy_bank_entries` (Lịch sử biến động Heo đất)
Ghi nhận từng khoản tiền quét tự động cuối tháng hoặc nạp/rút thủ công vào heo đất.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh bản ghi giao dịch heo đất. |
| `piggy_bank_id` | BIGINT | FK | NO | | Heo đất tiếp nhận giao dịch. |
| `amount` | DECIMAL(19, 0) | | NO | | Số tiền biến động (VNĐ). |
| `type` | ENUM('SWEEP', 'MANUAL_DEPOSIT', 'WITHDRAW') | | NO | | Loại biến động: Quét dư cuối tháng, Nạp chủ động, Rút tiền. |
| `source_envelope_id`| BIGINT | FK | YES | NULL | Phong bì nguồn nếu là biến động quét dư tự động (`SWEEP`). |
| `savings_goal_id`| BIGINT | FK | YES | NULL | Mục tiêu tiết kiệm được rót vốn nếu là lệnh rút chuyển tiền. |
| `note` | VARCHAR(255) | | YES | NULL | Ghi chú chi tiết cho biến động heo đất. |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm phát sinh giao dịch heo đất. |

---

## 15. Bảng `month_closes` (Chốt kỳ ngân sách tháng)
Bản lưu trữ lịch sử chốt sổ cuối tháng, ghi nhận thành tích tiết kiệm của người dùng.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh phiên đóng kỳ. |
| `budget_period_id`| BIGINT | FK, UK | NO | | Kỳ ngân sách được đóng lại. |
| `closed_by` | BIGINT | FK (users.id) | NO | | Người dùng thực hiện thao tác đóng kỳ. |
| `closed_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm đóng kỳ ngân sách. |
| `total_spent` | DECIMAL(19, 0) | | NO | 0 | Tổng số tiền thực chi trong cả kỳ. |
| `total_saved` | DECIMAL(19, 0) | | NO | 0 | Tổng số tiền dư thừa được bảo toàn và chuyển sang tiết kiệm. |
| `notes` | TEXT | | YES | NULL | Nhận xét hoặc tổng kết kết quả tài chính tháng của người dùng. |

---

## 16. Bảng `notifications` (Thông báo & Cảnh báo)
Hệ thống thông báo đẩy, nhắc nhở ghi chép và cảnh báo bội chi.

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh thông báo. |
| `user_id` | BIGINT | FK | NO | | Người dùng nhận thông báo. |
| `type` | ENUM(...) | | NO | | Loại thông báo (`OVERSPENT`, `NEAR_LIMIT`, `SALARY_REMINDER`, `BUDGET_CONFIRMED`, `INVITATION`, `DAILY_CLOSE`, `SYSTEM`). |
| `title` | VARCHAR(150) | | NO | | Tiêu đề thông báo ngắn gọn. |
| `message` | TEXT | | NO | | Nội dung chi tiết thông báo gửi đến người dùng. |
| `status` | ENUM('PENDING', 'SENT', 'READ') | | NO | 'PENDING' | Trạng thái xử lý: Chờ gửi, Đã gửi, Đã đọc. |
| `action_url` | VARCHAR(255) | | YES | NULL | Đường dẫn điều hướng nhanh khi người dùng click vào thông báo. |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tạo thông báo. |

---

## 17. Bảng `workspace_invitations` (Lời mời tham gia Quỹ chung)
Quản lý các lời mời tham gia không gian tài chính chung qua email, mã bảo mật token và thời hạn kích hoạt.
Phụ trách bởi: Thành viên 1 (Auth, Workspace & Core Management).

| Tên cột | Kiểu dữ liệu | PK/FK | Nullable | Mặc định | Mô tả ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | BIGINT AUTO_INCREMENT | PK | NO | Auto | Định danh lời mời tham gia quỹ. |
| `workspace_id` | BIGINT | FK (workspaces.id) | NO | | Workspace gửi lời mời tham gia. |
| `email` | VARCHAR(100) | | NO | | Địa chỉ email người được mời tham gia. |
| `invited_by_id` | BIGINT | FK (users.id) | NO | | Thành viên (OWNER/MANAGER) đã tạo và gửi lời mời. |
| `role` | ENUM('MANAGER', 'WRITER', 'VIEWER') | | NO | 'VIEWER' | Vai trò dự kiến trao cho người dùng khi gia nhập quỹ. |
| `token` | VARCHAR(100) | UK | NO | | Chuỗi mã bí mật duy nhất bảo vệ đường link xác nhận lời mời. |
| `status` | ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'CANCELLED') | | NO | 'PENDING' | Trạng thái hiện tại của lời mời. |
| `expires_at` | DATETIME | | NO | | Thời điểm lời mời hết hiệu lực (mặc định 7 ngày). |
| `created_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm tạo lời mời. |
| `updated_at` | DATETIME | | NO | CURRENT_TIMESTAMP | Thời điểm cập nhật trạng thái gần nhất. |


