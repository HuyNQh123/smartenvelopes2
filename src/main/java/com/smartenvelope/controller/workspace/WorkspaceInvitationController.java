package com.smartenvelope.controller.workspace;

import com.smartenvelope.dto.workspace.WorkspaceInviteRequest;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.WorkspaceInvitation;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.WorkspaceInvitationService;
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
 * Controller xử lý toàn bộ quy trình Lời mời tham gia Quỹ tài chính (Gửi, Chấp nhận, Từ chối, Hủy).
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
public class WorkspaceInvitationController {

    private final WorkspaceInvitationService invitationService;
    private final CurrentUserService currentUserService;

    @Autowired
    public WorkspaceInvitationController(WorkspaceInvitationService invitationService,
                                         CurrentUserService currentUserService) {
        this.invitationService = invitationService;
        this.currentUserService = currentUserService;
    }

    /**
     * Gửi lời mời thành viên qua email từ trang chi tiết Quỹ.
     */
    @PostMapping("/workspaces/{id}/invitations")
    public String inviteMember(@PathVariable("id") Long workspaceId,
                               @Valid @ModelAttribute("inviteForm") WorkspaceInviteRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email không hợp lệ.");
            return "redirect:/workspaces/" + workspaceId;
        }

        try {
            User currentUser = currentUserService.getCurrentUser();
            WorkspaceInvitation invitation = invitationService.inviteMember(workspaceId, request, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gửi lời mời tham gia tới " + invitation.getEmail() + " thành công!");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi lời mời: " + ex.getMessage());
        }

        return "redirect:/workspaces/" + workspaceId;
    }

    /**
     * Hủy lời mời tham gia đang chờ.
     */
    @PostMapping("/workspaces/{id}/invitations/{invitationId}/cancel")
    public String cancelInvitation(@PathVariable("id") Long workspaceId,
                                   @PathVariable("invitationId") Long invitationId,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            invitationService.cancelInvitation(invitationId, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy lời mời thành công.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy lời mời: " + ex.getMessage());
        }

        return "redirect:/workspaces/" + workspaceId;
    }

    /**
     * Xem danh sách các lời mời tham gia Quỹ gửi tới tài khoản hiện tại.
     */
    @GetMapping("/invitations")
    public String viewInvitations(Model model) {
        User currentUser = currentUserService.getCurrentUser();
        List<WorkspaceInvitation> pendingInvitations = invitationService.getPendingInvitationsForEmail(currentUser.getEmail());
        model.addAttribute("invitations", pendingInvitations);
        return "workspaces/invitations";
    }

    /**
     * Trang tiếp nhận lời mời khi người dùng bấm vào liên kết trong email: /invitations/join?token=...
     */
    @GetMapping("/invitations/join")
    public String joinByToken(@RequestParam(name = "token", required = false) String token,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đường dẫn lời mời không hợp lệ hoặc thiếu mã token.");
            return "redirect:/workspaces";
        }

        try {
            WorkspaceInvitation invitation = invitationService.getInvitationByToken(token.trim());
            User currentUser = currentUserService.getCurrentUser();

            model.addAttribute("invitation", invitation);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isEmailMatch", currentUser.getEmail().equalsIgnoreCase(invitation.getEmail()));
            return "workspaces/join";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/workspaces";
        }
    }

    /**
     * Chấp nhận lời mời trực tiếp bằng ID (từ trang danh sách lời mời hoặc trang chủ).
     */
    @PostMapping("/invitations/{id}/accept")
    public String acceptInvitation(@PathVariable("id") Long invitationId,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            invitationService.acceptInvitation(invitationId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Chúc mừng! Bạn đã tham gia vào Quỹ tài chính thành công.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể chấp nhận lời mời: " + ex.getMessage());
        }

        return "redirect:/workspaces";
    }

    /**
     * Chấp nhận lời mời từ đường dẫn token.
     */
    @PostMapping("/invitations/join/accept")
    public String acceptJoinToken(@RequestParam("token") String token,
                                  RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            invitationService.acceptInvitationByToken(token, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Chúc mừng! Bạn đã tham gia vào Quỹ tài chính thành công.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể chấp nhận lời mời: " + ex.getMessage());
        }

        return "redirect:/workspaces";
    }

    /**
     * Từ chối lời mời tham gia Quỹ tài chính.
     */
    @PostMapping("/invitations/{id}/decline")
    public String declineInvitation(@PathVariable("id") Long invitationId,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            invitationService.declineInvitation(invitationId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã từ chối lời mời tham gia Quỹ.");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể từ chối lời mời: " + ex.getMessage());
        }

        return "redirect:/workspaces";
    }
}

