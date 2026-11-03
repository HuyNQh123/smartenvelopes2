-- ========================================================
-- SMARTENVELOPE - DỮ LIỆU KIỂM THỬ MẪU (DEMO DATA)
-- Dành cho môi trường phát triển (Development / Testing)
-- Mật khẩu chung cho tất cả tài khoản demo là: password123
-- Hash BCrypt: $2a$10$wE99k2dGz0WjZJ3nQ2G2oO9b8xYQj1m8WnZ9iE7Y3yFqF9qB7Jk5m
-- ========================================================

USE smart_envelope_dev;

-- 1. Thêm Người dùng mẫu (User A, User B, User C, Admin)
INSERT INTO users (id, full_name, email, password, role, status, created_at) VALUES
(1, 'Nguyễn Văn A (Chủ Quỹ)', 'user.a@smartenvelope.test', '$2a$10$wE99k2dGz0WjZJ3nQ2G2oO9b8xYQj1m8WnZ9iE7Y3yFqF9qB7Jk5m', 'USER', 'ACTIVE', NOW()),
(2, 'Trần Thị B (Thành viên Quỹ)', 'user.b@smartenvelope.test', '$2a$10$wE99k2dGz0WjZJ3nQ2G2oO9b8xYQj1m8WnZ9iE7Y3yFqF9qB7Jk5m', 'USER', 'ACTIVE', NOW()),
(3, 'Lê Văn C (Người ngoài Quỹ)', 'user.c@smartenvelope.test', '$2a$10$wE99k2dGz0WjZJ3nQ2G2oO9b8xYQj1m8WnZ9iE7Y3yFqF9qB7Jk5m', 'USER', 'ACTIVE', NOW()),
(4, 'Quản Trị Viên Hệ Thống', 'admin@smartenvelope.test', '$2a$10$wE99k2dGz0WjZJ3nQ2G2oO9b8xYQj1m8WnZ9iE7Y3yFqF9qB7Jk5m', 'ADMIN', 'ACTIVE', NOW());

-- 2. Thêm Không gian làm việc (Workspaces)
-- User A sở hữu: 1 Workspace cá nhân (id=1) và 1 Quỹ chung bạn cùng phòng (id=2)
-- User B sở hữu: 1 Workspace cá nhân (id=3)
-- User C sở hữu: 1 Workspace cá nhân (id=4)
INSERT INTO workspaces (id, name, workspace_type, created_by, created_at) VALUES
(1, 'Tài chính cá nhân - Nguyễn Văn A', 'PERSONAL', 1, NOW()),
(2, 'Quỹ chung Phòng 402 - Nhóm A & B', 'SHARED', 1, NOW()),
(3, 'Tài chính cá nhân - Trần Thị B', 'PERSONAL', 2, NOW()),
(4, 'Tài chính cá nhân - Lê Văn C', 'PERSONAL', 3, NOW());

-- 3. Phân quyền thành viên (Memberships)
INSERT INTO memberships (id, workspace_id, user_id, member_role, status, invited_by, invited_at, accepted_at) VALUES
(1, 1, 1, 'OWNER', 'ACCEPTED', NULL, NOW(), NOW()),
(2, 2, 1, 'OWNER', 'ACCEPTED', NULL, NOW(), NOW()),
(3, 2, 2, 'VIEWER', 'ACCEPTED', 1, NOW(), NOW()),
(4, 3, 2, 'OWNER', 'ACCEPTED', NULL, NOW(), NOW()),
(5, 4, 3, 'OWNER', 'ACCEPTED', NULL, NOW(), NOW());

-- 4. Cài đặt tài chính cá nhân (UserFinanceSettings)
INSERT INTO user_finance_settings (id, user_id, salary_day, expected_salary, hourly_income, budget_reminder_time, daily_close_time, timezone) VALUES
(1, 1, 5, 10000000, 50000, '09:00', '22:00', 'Asia/Ho_Chi_Minh'),
(2, 2, 10, 8000000, 40000, '08:30', '22:00', 'Asia/Ho_Chi_Minh');

-- 5. Tài khoản nguồn tiền thanh toán (Accounts) của Workspace cá nhân User A
-- Tổng tiền thực tế có: 4.000.000 (tiền mặt) + 6.000.000 (Techcombank) = 10.000.000 VNĐ
INSERT INTO accounts (id, workspace_id, account_name, account_type, opening_balance, current_balance, description, status, created_at) VALUES
(1, 1, 'Ví tiền mặt', 'CASH', 4000000, 3600000, 'Tiền mặt chi tiêu hàng ngày', 'ACTIVE', NOW()),
(2, 1, 'Techcombank Cá Nhân', 'BANK', 6000000, 6000000, 'Tài khoản nhận lương chính', 'ACTIVE', NOW());

-- 6. Kỳ ngân sách mẫu (BudgetPeriod) của Workspace 1
-- Tháng 11/2026, tiền khả dụng = 10.000.000 VNĐ, trạng thái CONFIRMED
INSERT INTO budget_periods (id, workspace_id, month, year, start_date, end_date, carry_forward_amount, confirmed_available_amount, status, confirmed_at, version, created_at) VALUES
(1, 1, 11, 2026, '2026-11-01', '2026-11-30', 0, 10000000, 'CONFIRMED', NOW(), 1, NOW());

-- 7. Danh sách Phong bì (Envelopes) thuộc Kỳ 1 (Tổng phân bổ đúng 10.000.000 VNĐ)
INSERT INTO envelopes (id, budget_period_id, category_id, purpose_key, name, allocated_amount, remaining_balance, is_locked, is_essential, pacing_enabled, rollover_policy, description, version, created_at) VALUES
(1, 1, 1, 'FOOD', 'Ăn uống hàng ngày', 2500000, 2100000, FALSE, TRUE, TRUE, 'SWEEP_TO_PIGGY', 'Cơm trưa, đi chợ, đồ ăn sáng', 1, NOW()),
(2, 1, 2, 'HOUSING', 'Tiền phòng trọ & Điện nước', 3000000, 3000000, TRUE, TRUE, FALSE, 'CARRY_FORWARD', 'Khoản cố định bảo vệ, không cho rút bù', 0, NOW()),
(3, 1, 3, 'EDUCATION', 'Học tập & Sách vở', 1000000, 1000000, FALSE, FALSE, FALSE, 'SWEEP_TO_PIGGY', 'Sách tiếng Anh, tài liệu ôn thi', 0, NOW()),
(4, 1, 5, 'ENTERTAINMENT', 'Đi chơi & Cafe', 1000000, 1000000, FALSE, FALSE, TRUE, 'SWEEP_TO_PIGGY', 'Cafe gặp gỡ bạn bè cuối tuần', 0, NOW()),
(5, 1, 6, 'SHOPPING', 'Mua sắm cá nhân', 1000000, 1000000, FALSE, FALSE, FALSE, 'SWEEP_TO_PIGGY', 'Đồ gia dụng nhỏ, quần áo', 0, NOW()),
(6, 1, 7, 'EMERGENCY', 'Quỹ khẩn cấp rủi ro', 1500000, 1500000, TRUE, TRUE, FALSE, 'CARRY_FORWARD', 'Dự phòng bất khả kháng', 0, NOW());

-- 8. Giao dịch mẫu (Transactions)
-- Chi 400.000đ ăn uống từ Ví tiền mặt: Account 1 giảm từ 4.000.000 -> 3.600.000, Envelope 1 giảm từ 2.500.000 -> 2.100.000
INSERT INTO transactions (id, workspace_id, budget_period_id, account_id, envelope_id, category_id, transaction_type, amount, description, occurred_at, status, request_id, created_by, version, created_at) VALUES
(1, 1, 1, 1, 1, 1, 'EXPENSE', 400000, 'Đi siêu thị mua thực phẩm nấu ăn tuần 1', '2026-11-02 18:30:00', 'COMPLETED', 'REQ-DEMO-TX-001', 1, 0, NOW());

-- 9. Ghi nhận audit cho giao dịch
INSERT INTO transaction_audits (id, transaction_id, action, old_amount, new_amount, reason, changed_by, changed_at) VALUES
(1, 1, 'CREATED', NULL, 400000, 'Khởi tạo khoản chi tiêu siêu thị', 1, NOW());

-- 10. Mục tiêu tiết kiệm (SavingsGoal) & Heo đất (PiggyBank)
INSERT INTO savings_goals (id, workspace_id, created_by, name, target_amount, current_amount, target_date, status, description, created_at) VALUES
(1, 1, 1, 'Mua Laptop lập trình mới', 15000000, 3000000, '2027-06-30', 'ACTIVE', 'Tiết kiệm mua MacBook M3 phục vụ làm đồ án tốt nghiệp', NOW());

INSERT INTO piggy_banks (id, workspace_id, balance, created_at) VALUES
(1, 1, 500000, NOW()),
(2, 2, 0, NOW());

INSERT INTO piggy_bank_entries (id, piggy_bank_id, source_envelope_id, entry_type, amount, note, occurred_at) VALUES
(1, 1, NULL, 'MANUAL_DEPOSIT', 500000, 'Bỏ tiền lẻ tiết kiệm đầu tháng', NOW());

-- 11. Thông báo mẫu (Notifications)
INSERT INTO notifications (id, user_id, workspace_id, notification_type, title, content, status, scheduled_at, sent_at, deduplication_key, created_at) VALUES
(1, 1, 1, 'BUDGET_CONFIRMED', 'Kế hoạch ngân sách đã duyệt', 'Kỳ ngân sách Tháng 11/2026 đã được xác nhận phân bổ 100% thành công.', 'READ', NOW(), NOW(), 'NOTIF-CONFIRM-202611-1', NOW()),
(2, 2, 2, 'INVITATION', 'Lời mời tham gia Quỹ chung', 'Nguyễn Văn A đã mời bạn vào Quỹ chung Phòng 402 với vai trò Người xem (VIEWER).', 'READ', NOW(), NOW(), 'NOTIF-INVITE-WS2-U2', NOW());

