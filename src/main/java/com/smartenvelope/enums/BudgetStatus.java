package com.smartenvelope.enums;

/**
 * Trạng thái của kỳ ngân sách.
 * DRAFT: Đang lên kế hoạch phân bổ.
 * CONFIRMED: Đã chốt phân bổ 100%, ngân sách chính thức có hiệu lực ghi nhận chi tiêu.
 * CLOSED: Đã đóng kỳ ngân sách cuối tháng.
 * Phụ trách bởi: Thành viên 4.
 */
public enum BudgetStatus {
    DRAFT,
    CONFIRMED,
    CLOSED
}

