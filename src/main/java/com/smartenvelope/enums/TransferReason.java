package com.smartenvelope.enums;

/**
 * Định nghĩa lý do điều chuyển tiền giữa các phong bì ngân sách.
 * Phụ trách bởi: Thành viên 3.
 */
public enum TransferReason {
    /**
     * Bù khoản chi thiếu khi chi tiêu vượt quá số dư phong bì.
     */
    COVER_EXPENSE("Bù khoản chi thiếu"),

    /**
     * Người dùng chủ động điều chuyển / tái lập kế hoạch ngân sách giữa 2 phong bì.
     */
    REPLAN("Người dùng tự chuyển tiền giữa 2 phong bì");

    private final String description;

    TransferReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
