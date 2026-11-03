package com.smartenvelope.enums;

/**
 * Loại biến động dòng tiền trong Heo đất tích lũy:
 * SWEEP: Quét tiền dư tự động cuối tháng từ các phong bì.
 * MANUAL_DEPOSIT: Nạp tiền chủ động vào heo đất.
 * WITHDRAW: Rút tiền từ heo đất để chuyển sang mục tiêu hoặc tài khoản.
 * Phụ trách bởi: Thành viên 5.
 */
public enum PiggyBankEntryType {
    SWEEP,
    MANUAL_DEPOSIT,
    WITHDRAW
}

