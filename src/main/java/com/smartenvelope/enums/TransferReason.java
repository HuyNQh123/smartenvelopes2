package com.smartenvelope.enums;

/**
 * Lý do thực hiện nghiệp vụ bù đắp ngân sách giữa các phong bì:
 * COVER_EXPENSE: Bù tiền gấp để chi trả một khoản chi phát sinh vượt hạn mức.
 * REPLAN: Tái cơ cấu / điều chỉnh kế hoạch phân bổ giữa các phong bì.
 * Phụ trách bởi: Thành viên 3.
 */
public enum TransferReason {
    COVER_EXPENSE,
    REPLAN
}

