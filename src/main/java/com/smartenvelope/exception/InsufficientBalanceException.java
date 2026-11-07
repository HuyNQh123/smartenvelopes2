package com.smartenvelope.exception;

/**
 * Ngoại lệ ném ra khi số dư phong bì hoặc tài khoản không đủ để thực hiện giao dịch.
 */
public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
