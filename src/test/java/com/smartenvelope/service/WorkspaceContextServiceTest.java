package com.smartenvelope.service;

import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceContextServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private MembershipRepository membershipRepository;

    private MockHttpSession session;
    private WorkspaceContextService workspaceContextService;

    private User testUser;
    private Workspace personalWorkspace;
    private Workspace familyWorkspace;
    private Membership personalMembership;
    private Membership familyMembership;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        workspaceContextService = new WorkspaceContextService(
                currentUserService, workspaceRepository, membershipRepository, session);

        testUser = new User("nguyenvana", "pass", "Nguyễn Văn A", "vana@example.com", Role.USER);
        testUser.setId(1L);

        personalWorkspace = new Workspace("Quỹ cá nhân", WorkspaceType.PERSONAL, "Ví cá nhân", testUser);
        personalWorkspace.setId(101L);

        familyWorkspace = new Workspace("Quỹ gia đình", WorkspaceType.SHARED, "Quỹ chung", testUser);
        familyWorkspace.setId(202L);

        personalMembership = new Membership(personalWorkspace, testUser, MemberRole.OWNER, MembershipStatus.ACCEPTED);
        personalMembership.setId(11L);

        familyMembership = new Membership(familyWorkspace, testUser, MemberRole.MANAGER, MembershipStatus.ACCEPTED);
        familyMembership.setId(22L);
    }

    @Test
    @DisplayName("STT 3 & 4: Khi Session chưa có currentWorkspaceId, tự động chọn Quỹ cá nhân và lưu vào Session")
    void testGetCurrentWorkspaceId_whenSessionEmpty_shouldPickDefaultPersonalWorkspace() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.findByOwnerIdAndType(1L, WorkspaceType.PERSONAL))
                .thenReturn(List.of(personalWorkspace));
        when(workspaceRepository.existsById(101L)).thenReturn(true);
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(101L, 1L, MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(personalMembership));

        Long workspaceId = workspaceContextService.getCurrentWorkspaceId();

        assertNotNull(workspaceId);
        assertEquals(101L, workspaceId);
        // Kiểm tra thuộc tính Session có tên chính xác currentWorkspaceId
        assertEquals(101L, session.getAttribute(WorkspaceContextService.CURRENT_WORKSPACE_ID_SESSION_KEY));
    }

    @Test
    @DisplayName("STT 4b: Khi Session đã có currentWorkspaceId hợp lệ, trả về ID đó")
    void testGetCurrentWorkspaceId_whenSessionHasValidId() {
        session.setAttribute(WorkspaceContextService.CURRENT_WORKSPACE_ID_SESSION_KEY, 202L);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.existsById(202L)).thenReturn(true);
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(202L, 1L, MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(familyMembership));

        Long workspaceId = workspaceContextService.getCurrentWorkspaceId();

        assertEquals(202L, workspaceId);
        verify(workspaceRepository, never()).findByOwnerIdAndType(anyLong(), any());
    }

    @Test
    @DisplayName("STT 9: Khi Session chứa Workspace ID cũ hoặc người dùng không còn quyền, loại bỏ và chọn mặc định")
    void testGetCurrentWorkspaceId_whenSessionHasStaleId_shouldRemoveAndPickDefault() {
        // Giả sử session cũ còn lưu ID 999L mà người dùng không có quyền
        session.setAttribute(WorkspaceContextService.CURRENT_WORKSPACE_ID_SESSION_KEY, 999L);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.existsById(999L)).thenReturn(false);

        // Fallback default sang personal workspace
        when(workspaceRepository.findByOwnerIdAndType(1L, WorkspaceType.PERSONAL))
                .thenReturn(List.of(personalWorkspace));
        when(workspaceRepository.existsById(101L)).thenReturn(true);
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(101L, 1L, MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(personalMembership));

        Long workspaceId = workspaceContextService.getCurrentWorkspaceId();

        assertEquals(101L, workspaceId);
        assertEquals(101L, session.getAttribute(WorkspaceContextService.CURRENT_WORKSPACE_ID_SESSION_KEY));
    }

    @Test
    @DisplayName("STT 5 & 10: Người dùng chuyển sang Workspace khác có Membership hợp lệ thành công")
    void testSetCurrentWorkspaceId_whenValidMembership_shouldSucceed() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.existsById(202L)).thenReturn(true);
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(202L, 1L, MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(familyMembership));

        workspaceContextService.setCurrentWorkspaceId(202L);

        assertEquals(202L, session.getAttribute(WorkspaceContextService.CURRENT_WORKSPACE_ID_SESSION_KEY));
    }

    @Test
    @DisplayName("STT 6: Người dùng chuyển sang Workspace không thuộc quyền truy cập -> Bị từ chối bằng AccessDeniedException")
    void testSetCurrentWorkspaceId_whenNoMembership_shouldThrowAccessDenied() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.existsById(303L)).thenReturn(true);
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(303L, 1L, MembershipStatus.ACCEPTED))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> workspaceContextService.setCurrentWorkspaceId(303L));
        assertNull(session.getAttribute(WorkspaceContextService.CURRENT_WORKSPACE_ID_SESSION_KEY));
    }

    @Test
    @DisplayName("STT 7: Người dùng gửi Workspace ID không tồn tại -> Bị từ chối bằng AccessDeniedException")
    void testSetCurrentWorkspaceId_whenWorkspaceDoesNotExist_shouldThrowAccessDenied() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.existsById(404L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> workspaceContextService.setCurrentWorkspaceId(404L));
    }

    @Test
    @DisplayName("getCurrentWorkspace() trả về đối tượng Workspace đầy đủ")
    void testGetCurrentWorkspace_shouldReturnEntity() {
        session.setAttribute(WorkspaceContextService.CURRENT_WORKSPACE_ID_SESSION_KEY, 101L);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.existsById(101L)).thenReturn(true);
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(101L, 1L, MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(personalMembership));
        when(workspaceRepository.findById(101L)).thenReturn(Optional.of(personalWorkspace));

        Workspace result = workspaceContextService.getCurrentWorkspace();

        assertNotNull(result);
        assertEquals("Quỹ cá nhân", result.getName());
    }

    @Test
    @DisplayName("getAvailableWorkspaces() chỉ trả về các Workspace có Membership ACCEPTED")
    void testGetAvailableWorkspaces_returnsOnlyAccepted() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.findWorkspacesJoinedByUser(1L))
                .thenReturn(List.of(personalWorkspace, familyWorkspace));

        List<Workspace> available = workspaceContextService.getAvailableWorkspaces();

        assertEquals(2, available.size());
        assertTrue(available.contains(personalWorkspace));
        assertTrue(available.contains(familyWorkspace));
    }

    @Test
    @DisplayName("Người dùng không có bất kỳ Workspace nào -> Ném ResourceNotFoundException")
    void testResolveDefaultWorkspace_whenNoWorkspace_shouldThrowException() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(workspaceRepository.findByOwnerIdAndType(1L, WorkspaceType.PERSONAL))
                .thenReturn(Collections.emptyList());
        when(workspaceRepository.findWorkspacesJoinedByUser(1L))
                .thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> workspaceContextService.getCurrentWorkspaceId());
    }
}
