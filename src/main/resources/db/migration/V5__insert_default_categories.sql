-- ========================================================
-- SMARTENVELOPE DATABASE MIGRATION - V5
-- Seed Data: 10 Danh mục thu, chi mặc định toàn hệ thống
-- Phụ trách: Thành viên 2
-- ========================================================

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
