package com.smartenvelope.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Lớp bắt và xử lý ngoại lệ tập trung cho toàn bộ ứng dụng (ControllerAdvice).
 * Phụ trách bởi: Toàn nhóm (Dùng chung).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * Ngoại lệ bảo mật của Spring Security (AccessDeniedException, AuthenticationException)
     * PHẢI được ném tiếp ra ngoài (re-throw) để Spring Security Filter Chain tự động chuyển hướng
     * người dùng chưa đăng nhập về trang /login thay vì chặn lại và trả về lỗi 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex) throws AccessDeniedException {
        throw ex;
    }

    @ExceptionHandler(AuthenticationException.class)
    public void handleAuthenticationException(AuthenticationException ex) throws AuthenticationException {
        throw ex;
    }

    /**
     * Bắt các ngoại lệ chưa được xử lý khác và ghi log chi tiết phục vụ debug.
     */
    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception ex, Model model) {
        log.error("Unhandled Exception caught by GlobalExceptionHandler: ", ex);
        model.addAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        return "error/500";
    }
}
