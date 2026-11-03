package com.smartenvelope.controller.advice;

import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.WorkspaceContextService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * ControllerAdvice nạp các thuộc tính dùng chung vào Model cho toàn bộ các trang giao diện Thymeleaf:
 * - currentUser: Thực thể User đang đăng nhập.
 * - currentWorkspace: Đối tượng Workspace hiện tại đang được chọn.
 * - currentWorkspaceId: ID của Workspace hiện tại.
 * - availableWorkspaces: Danh sách tất cả các Quỹ mà người dùng có quyền truy cập.
 *
 * Giúp Thành viên 2, 3, 4, 5 khi viết Controller không cần phải tự thêm các thuộc tính này vào Model lặp đi lặp lại.
 * Phụ trách bởi: Thành viên 1.
 */
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    private final CurrentUserService currentUserService;
    private final WorkspaceContextService workspaceContextService;

    public GlobalModelAttributeAdvice(CurrentUserService currentUserService,
                                      WorkspaceContextService workspaceContextService) {
        this.currentUserService = currentUserService;
        this.workspaceContextService = workspaceContextService;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        if (!currentUserService.isAuthenticated()) {
            return null;
        }
        try {
            return currentUserService.getCurrentUser();
        } catch (Exception ignored) {
            return null;
        }
    }

    @ModelAttribute("currentWorkspace")
    public Workspace currentWorkspace() {
        if (!currentUserService.isAuthenticated()) {
            return null;
        }
        try {
            return workspaceContextService.getCurrentWorkspace();
        } catch (Exception ignored) {
            return null;
        }
    }

    @ModelAttribute("currentWorkspaceId")
    public Long currentWorkspaceId() {
        if (!currentUserService.isAuthenticated()) {
            return null;
        }
        try {
            return workspaceContextService.getCurrentWorkspaceId();
        } catch (Exception ignored) {
            return null;
        }
    }

    @ModelAttribute("availableWorkspaces")
    public List<Workspace> availableWorkspaces() {
        if (!currentUserService.isAuthenticated()) {
            return Collections.emptyList();
        }
        try {
            return workspaceContextService.getAvailableWorkspaces();
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}

