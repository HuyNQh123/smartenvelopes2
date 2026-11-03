package com.smartenvelope.controller.workspace;

import com.smartenvelope.dto.workspace.WorkspaceInviteRequest;
import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.entity.WorkspaceInvitation;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.WorkspaceInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceInvitationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkspaceInvitationService invitationService;

    @Mock
    private CurrentUserService currentUserService;

    private User currentUser;
    private Workspace workspace;
    private WorkspaceInvitation invitation;

    @BeforeEach
    void setUp() {
        WorkspaceInvitationController controller = new WorkspaceInvitationController(invitationService, currentUserService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        currentUser = new User("huy", "pass", "Nguyễn Quang Huy", "huy@example.com", Role.USER);
        currentUser.setId(1L);

        workspace = new Workspace("Quỹ Gia Đình", WorkspaceType.SHARED, "Mô tả", currentUser);
        workspace.setId(10L);

        invitation = new WorkspaceInvitation(
                workspace, "huy@example.com", currentUser, MemberRole.VIEWER, "token-xyz", LocalDateTime.now().plusDays(7));
        invitation.setId(55L);
    }

    @Test
    @DisplayName("STT 6: Gửi lời mời qua email hợp lệ -> Redirect về chi tiết Quỹ với thông báo thành công")
    void testInviteMember_success() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(invitationService.inviteMember(eq(10L), any(WorkspaceInviteRequest.class), eq(currentUser)))
                .thenReturn(invitation);

        mockMvc.perform(post("/workspaces/10/invitations")
                        .param("email", "friend@example.com")
                        .param("role", "WRITER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces/10"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Hủy lời mời -> Redirect về chi tiết Quỹ với thông báo thành công")
    void testCancelInvitation() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(invitationService).cancelInvitation(55L, 1L);

        mockMvc.perform(post("/workspaces/10/invitations/55/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces/10"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /invitations -> Hiển thị danh sách lời mời nhận được")
    void testViewInvitations() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(invitationService.getPendingInvitationsForEmail("huy@example.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/invitations"))
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/invitations"))
                .andExpect(model().attributeExists("invitations"));
    }

    @Test
    @DisplayName("GET /invitations/join?token=... -> Hiển thị trang xác nhận lời mời")
    void testJoinByToken() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(invitationService.getInvitationByToken("token-xyz")).thenReturn(invitation);

        mockMvc.perform(get("/invitations/join").param("token", "token-xyz"))
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/join"))
                .andExpect(model().attributeExists("invitation"))
                .andExpect(model().attribute("isEmailMatch", true));
    }

    @Test
    @DisplayName("STT 10: Chấp nhận lời mời -> Chuyển về /workspaces với thông báo chúc mừng")
    void testAcceptInvitation() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Membership membership = new Membership(workspace, currentUser, MemberRole.VIEWER, MembershipStatus.ACCEPTED);
        when(invitationService.acceptInvitation(55L, currentUser)).thenReturn(membership);

        mockMvc.perform(post("/invitations/55/accept"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Từ chối lời mời -> Chuyển về /workspaces với thông báo từ chối")
    void testDeclineInvitation() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(invitationService).declineInvitation(55L, currentUser);

        mockMvc.perform(post("/invitations/55/decline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

