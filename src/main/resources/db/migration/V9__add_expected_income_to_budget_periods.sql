-- Các kỳ cũ chưa có thông tin thu nhập dự kiến; giữ NULL thay vì suy đoán.
ALTER TABLE budget_periods
    ADD COLUMN expected_income DECIMAL(19, 0) NULL;
