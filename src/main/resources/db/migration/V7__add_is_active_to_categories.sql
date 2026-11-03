-- ========================================================
-- SMARTENVELOPE - DATABASE MIGRATION V7
-- Bổ sung cột is_active vào bảng categories phục vụ tính năng
-- vô hiệu hóa và kích hoạt lại danh mục thu chi (Thành viên 1 - Bước 6)
-- ========================================================

ALTER TABLE categories 
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER is_system;

