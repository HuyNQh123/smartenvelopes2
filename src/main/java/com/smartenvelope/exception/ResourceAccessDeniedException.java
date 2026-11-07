package com.smartenvelope.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ngoại lệ ném ra khi người dùng không đủ quyền hạn thực hiện thao tác
 * (Ví dụ: Thành viên có vai trò VIEWER trong Quỹ chung cố gắng tạo giao dịch).
 * Tự động trả về mã lỗi HTTP 403 Forbidden.
 * Phụ trách bởi: Thành viên 3 (Quang) - Mục 5.1 & Ca test M3-10.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ResourceAccessDeniedException extends RuntimeException {

    public ResourceAccessDeniedException(String message) {
        super(message);
    }

    public ResourceAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
