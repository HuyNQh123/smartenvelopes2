package com.smartenvelope.dto.workspace;

import com.smartenvelope.enums.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO nhận dữ liệu gửi lời mời tham gia Quỹ qua email.
 * Phụ trách bởi: Thành viên 1.
 */
public class WorkspaceInviteRequest {

    @NotBlank(message = "Địa chỉ email không được để trống")
    @Email(message = "Email không đúng định dạng hợp lệ")
    private String email;

    @NotNull(message = "Vai trò không được để trống")
    private MemberRole role = MemberRole.VIEWER;

    public WorkspaceInviteRequest() {
    }

    public WorkspaceInviteRequest(String email, MemberRole role) {
        this.email = email;
        this.role = role != null ? role : MemberRole.VIEWER;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }
}

