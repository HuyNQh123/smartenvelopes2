package com.smartenvelope.exception;

/**
 * Ngoại lệ ném ra khi có lỗi vi phạm quy tắc nghiệp vụ trong hệ thống.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
