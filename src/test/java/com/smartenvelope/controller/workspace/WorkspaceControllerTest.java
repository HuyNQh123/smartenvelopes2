package com.smartenvelope.controller.workspace;

import com.smartenvelope.dto.workspace.WorkspaceCreateRequest;
import com.smartenvelope.dto.workspace.WorkspaceMemberDTO;
import com.smartenvelope.dto.workspace.WorkspaceSummaryDTO;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.WorkspaceInvitationService;
import com.smartenvelope.service.WorkspaceService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private WorkspaceInvitationService invitationService;

    @Mock
    private CurrentUserService currentUserService;

    private User currentUser;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        WorkspaceController controller = new WorkspaceController(workspaceService, invitationService, currentUserService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        currentUser = new User("huy", "pass", "Nguyễn Quang Huy", "huy@example.com", Role.USER);
        currentUser.setId(1L);

        workspace = new Workspace("Quỹ Gia Đình", WorkspaceType.SHARED, "Mô tả", currentUser);
        workspace.setId(10L);
    }

    @Test
    @DisplayName("STT 2: Đăng nhập và truy cập /workspaces -> Hiển thị danh sách Quỹ")
    void testIndex() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceService.getUserWorkspaces(1L)).thenReturn(List.of(
                new WorkspaceSummaryDTO(10L, "Quỹ Gia Đình", WorkspaceType.SHARED, "Mô tả", "Huy", 1L, 2, MemberRole.OWNER, true, LocalDateTime.now())
        ));
        when(invitationService.getPendingInvitationsForEmail("huy@example.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/workspaces"))
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/index"))
                .andExpect(model().attributeExists("workspaces"))
                .andExpect(model().attributeExists("pendingInvitations"));
    }

    @Test
    @DisplayName("GET /workspaces/create -> Hiển thị form tạo Quỹ")
    void testCreateForm() throws Exception {
        mockMvc.perform(get("/workspaces/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/create"))
                .andExpect(model().attributeExists("workspaceForm"));
    }

    @Test
    @DisplayName("STT 3: POST /workspaces/create hợp lệ -> Tạo thành công và chuyển hướng về chi tiết Quỹ")
    void testCreate_success() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceService.createSharedWorkspace(any(WorkspaceCreateRequest.class), eq(currentUser))).thenReturn(workspace);

        mockMvc.perform(post("/workspaces/create")
                        .param("name", "Quỹ Gia Đình")
                        .param("description", "Mô tả"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces/10"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("STT 4: POST /workspaces/create với tên trống -> Báo lỗi và giữ nguyên form")
    void testCreate_blankName_validationError() throws Exception {
        mockMvc.perform(post("/workspaces/create")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/create"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("GET /workspaces/{id} -> Hiển thị chi tiết và thành viên Quỹ")
    void testDetail() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceService.getWorkspaceByIdAndCheckAccess(10L, 1L)).thenReturn(workspace);
        when(workspaceService.getUserRoleInWorkspace(10L, 1L)).thenReturn(MemberRole.OWNER);
        when(workspaceService.getWorkspaceMembers(10L, 1L)).thenReturn(List.of(
                new WorkspaceMemberDTO(1L, 1L, "huy", "Huy", "huy@example.com", null, MemberRole.OWNER, MembershipStatus.ACCEPTED, LocalDateTime.now(), true, false, false)
        ));
        when(invitationService.getPendingInvitationsForWorkspace(10L, 1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/workspaces/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/detail"))
                .andExpect(model().attributeExists("workspace"))
                .andExpect(model().attributeExists("members"))
                .andExpect(model().attributeExists("currentUserRole"));
    }

    @Test
    @DisplayName("POST /workspaces/{id}/delete -> Xóa Quỹ thành công và chuyển về /workspaces")
    void testDelete() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(workspaceService).deleteWorkspace(10L, 1L);

        mockMvc.perform(post("/workspaces/10/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /workspaces/{id}/leave -> Rời Quỹ thành công và chuyển về /workspaces")
    void testLeave() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(workspaceService).leaveWorkspace(10L, 1L);

        mockMvc.perform(post("/workspaces/10/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /workspaces/{id}/members/{mId}/role -> Đổi vai trò thành viên thành công")
    void testUpdateMemberRole() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(workspaceService).updateMemberRole(10L, 2L, MemberRole.WRITER, 1L);

        mockMvc.perform(post("/workspaces/10/members/2/role")
                        .param("role", "WRITER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces/10"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /workspaces/{id}/members/{mId}/remove -> Xóa thành viên thành công")
    void testRemoveMember() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(workspaceService).removeMember(10L, 2L, 1L);

        mockMvc.perform(post("/workspaces/10/members/2/remove"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workspaces/10"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

