package com.smartenvelope.enums;

/**
 * Chính sách xử lý tiền dư trong phong bì khi kết thúc kỳ ngân sách:
 * SWEEP_TO_PIGGY: Quét toàn bộ tiền dư vào Heo đất tích lũy.
 * CARRY_FORWARD: Chuyển tiếp số dư sang phong bì tương ứng của kỳ sau.
 * Phụ trách bởi: Thành viên 4.
 */
public enum RolloverPolicy {
    SWEEP_TO_PIGGY,
    CARRY_FORWARD
}

