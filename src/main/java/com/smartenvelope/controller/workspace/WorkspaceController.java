package com.smartenvelope.controller.workspace;

import com.smartenvelope.dto.workspace.WorkspaceCreateRequest;
import com.smartenvelope.dto.workspace.WorkspaceMemberDTO;
import com.smartenvelope.dto.workspace.WorkspaceSummaryDTO;
import com.smartenvelope.dto.workspace.WorkspaceUpdateRequest;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.entity.WorkspaceInvitation;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.WorkspaceInvitationService;
import com.smartenvelope.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller xử lý Quản lý Quỹ tài chính (Workspace) và Thành viên.
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final WorkspaceInvitationService invitationService;
    private final CurrentUserService currentUserService;

    @Autowired
    public WorkspaceController(WorkspaceService workspaceService,
                               WorkspaceInvitationService invitationService,
                               CurrentUserService currentUserService) {
        this.workspaceService = workspaceService;
        this.invitationService = invitationService;
        this.currentUserService = currentUserService;
    }

    /**
     * Danh sách tất cả các Quỹ mà người dùng tham gia kèm lời mời đang chờ.
     */
    @GetMapping
    public String index(Model model) {
        User currentUser = currentUserService.getCurrentUser();
        List<WorkspaceSummaryDTO> workspaces = workspaceService.getUserWorkspaces(currentUser.getId());
        List<WorkspaceInvitation> pendingInvitations = invitationService.getPendingInvitationsForEmail(currentUser.getEmail());

        model.addAttribute("workspaces", workspaces);
        model.addAttribute("pendingInvitations", pendingInvitations);
        return "workspaces/index";
    }

    /**
     * Màn hình tạo Quỹ chung mới.
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("workspaceForm")) {
            model.addAttribute("workspaceForm", new WorkspaceCreateRequest());
        }
        return "workspaces/create";
    }

    /**
     * Xử lý tạo Quỹ chung mới.
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("workspaceForm") WorkspaceCreateRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "workspaces/create";
        }

        try {
            User currentUser = currentUserService.getCurrentUser();
            Workspace workspace = workspaceService.createSharedWorkspace(request, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo Quỹ chung \"" + workspace.getName() + "\" thành công!");
            return "redirect:/workspaces/" + workspace.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/workspaces/create";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo Quỹ tài chính: " + ex.getMessage());
            return "redirect:/workspaces/create";
        }
    }

    /**
     * Chi tiết Quỹ tài chính và Quản lý Thành viên.
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long workspaceId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            Workspace workspace = workspaceService.getWorkspaceByIdAndCheckAccess(workspaceId, currentUser.getId());
            MemberRole currentUserRole = workspaceService.getUserRoleInWorkspace(workspaceId, currentUser.getId());
            List<WorkspaceMemberDTO> members = workspaceService.getWorkspaceMembers(workspaceId, currentUser.getId());

            List<WorkspaceInvitation> workspaceInvitations = List.of();
            if (currentUserRole == MemberRole.OWNER || currentUserRole == MemberRole.MANAGER) {
                workspaceInvitations = invitationService.getPendingInvitationsForWorkspace(workspaceId, currentUser.getId());
            }

            model.addAttribute("workspace", workspace);
            model.addAttribute("currentUserRole", currentUserRole);
            model.addAttribute("members", members);
            model.addAttribute("invitations", workspaceInvitations);
            model.addAttribute("updateForm", new WorkspaceUpdateRequest(workspace.getName(), workspace.getDescription()));

            return "workspaces/detail";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/workspaces";
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Từ chối truy cập: Bạn không có quyền truy cập vào Quỹ này.");
            return "redirect:/workspaces";
        }
    }

    /**
     * Cập nhật thông tin Quỹ (Chỉ OWNER và MANAGER).
     */
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long workspaceId,
                       @Valid @ModelAttribute("updateForm") WorkspaceUpdateRequest request,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên Quỹ không hợp lệ.");
            return "redirect:/workspaces/" + workspaceId;
        }

        try {
            User currentUser = currentUserService.getCurrentUser();
            workspaceService.updateWorkspace(workspaceId, request, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin Quỹ thành công!");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa thông tin Quỹ này.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/workspaces/" + workspaceId;
    }

    /**
     * Xóa Quỹ tài chính (Chỉ OWNER).
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long workspaceId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            workspaceService.deleteWorkspace(workspaceId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Quỹ tài chính thành công!");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Từ chối truy cập: Chỉ Chủ sở hữu mới được xóa Quỹ.");
            return "redirect:/workspaces/" + workspaceId;
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/workspaces/" + workspaceId;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa Quỹ tài chính: " + ex.getMessage());
            return "redirect:/workspaces/" + workspaceId;
        }

        return "redirect:/workspaces";
    }

    /**
     * Thành viên tự rời khỏi Quỹ.
     */
    @PostMapping("/{id}/leave")
    public String leave(@PathVariable("id") Long workspaceId, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            workspaceService.leaveWorkspace(workspaceId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã rời khỏi Quỹ thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/workspaces/" + workspaceId;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể rời Quỹ: " + ex.getMessage());
            return "redirect:/workspaces/" + workspaceId;
        }

        return "redirect:/workspaces";
    }

    /**
     * Đổi vai trò thành viên (Phân quyền).
     */
    @PostMapping("/{id}/members/{membershipId}/role")
    public String updateMemberRole(@PathVariable("id") Long workspaceId,
                                   @PathVariable("membershipId") Long membershipId,
                                   @RequestParam("role") MemberRole role,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            workspaceService.updateMemberRole(workspaceId, membershipId, role, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vai trò thành viên thành công!");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật vai trò: " + ex.getMessage());
        }

        return "redirect:/workspaces/" + workspaceId;
    }

    /**
     * Xóa thành viên khỏi Quỹ.
     */
    @PostMapping("/{id}/members/{membershipId}/remove")
    public String removeMember(@PathVariable("id") Long workspaceId,
                               @PathVariable("membershipId") Long membershipId,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            workspaceService.removeMember(workspaceId, membershipId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thành viên khỏi Quỹ.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thành viên: " + ex.getMessage());
        }

        return "redirect:/workspaces/" + workspaceId;
    }
}

