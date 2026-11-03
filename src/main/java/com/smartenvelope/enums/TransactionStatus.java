package com.smartenvelope.enums;

/**
 * Trạng thái của giao dịch thu chi:
 * COMPLETED: Đã ghi nhận thành công và trừ/cộng số dư.
 * ADJUSTED: Đã được chỉnh sửa thông tin.
 * CANCELLED: Đã hủy giao dịch (hoàn lại số dư).
 * Phụ trách bởi: Thành viên 3.
 */
public enum TransactionStatus {
    COMPLETED,
    ADJUSTED,
    CANCELLED
}

