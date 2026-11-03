package com.smartenvelope.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO tiếp nhận dữ liệu đăng nhập từ người dùng.
 * Phụ trách bởi: Thành viên 1.
 */
public class LoginRequest {

    @NotBlank(message = "Vui lòng nhập email hoặc tên đăng nhập")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
