package com.smartenvelope.service;

import com.smartenvelope.dto.workspace.WorkspaceCreateRequest;
import com.smartenvelope.dto.workspace.WorkspaceMemberDTO;
import com.smartenvelope.dto.workspace.WorkspaceSummaryDTO;
import com.smartenvelope.dto.workspace.WorkspaceUpdateRequest;
import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.AccountRepository;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private WorkspaceContextService workspaceContextService;

    @Mock
    private AccountRepository accountRepository;

    private WorkspaceService workspaceService;

    private User owner;
    private User manager;
    private User member;
    private Workspace sharedWorkspace;
    private Workspace personalWorkspace;

    @BeforeEach
    void setUp() {
        workspaceService = new WorkspaceService(
                workspaceRepository, membershipRepository, workspaceContextService, accountRepository);

        owner = new User("owner", "pass", "Chủ Quỹ", "owner@example.com", Role.USER);
        owner.setId(1L);

        manager = new User("manager", "pass", "Quản Lý", "manager@example.com", Role.USER);
        manager.setId(2L);

        member = new User("member", "pass", "Thành Viên", "member@example.com", Role.USER);
        member.setId(3L);

        sharedWorkspace = new Workspace("Quỹ Chung", WorkspaceType.SHARED, "Mô tả", owner);
        sharedWorkspace.setId(10L);

        personalWorkspace = new Workspace("Quỹ Cá Nhân", WorkspaceType.PERSONAL, "Mô tả", owner);
        personalWorkspace.setId(1L);
    }

    @Test
    @DisplayName("STT 3: Tạo quỹ chung hợp lệ -> Lưu Workspace và gán Membership OWNER")
    void testCreateSharedWorkspace_success() {
        WorkspaceCreateRequest request = new WorkspaceCreateRequest("Quỹ Gia Đình", "Chi tiêu gia đình");
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            Workspace ws = invocation.getArgument(0);
            ws.setId(99L);
            return ws;
        });

        Workspace created = workspaceService.createSharedWorkspace(request, owner);

        assertNotNull(created);
        assertEquals("Quỹ Gia Đình", created.getName());
        assertEquals(WorkspaceType.SHARED, created.getType());
        assertEquals(owner, created.getOwner());

        verify(membershipRepository).save(argThat(m ->
                m.getWorkspace().getId().equals(99L) &&
                m.getUser().getId().equals(owner.getId()) &&
                m.getRole() == MemberRole.OWNER &&
                m.getStatus() == MembershipStatus.ACCEPTED
        ));
        verify(workspaceContextService).setCurrentWorkspaceId(99L);
    }

    @Test
    @DisplayName("STT 4: Tạo quỹ với tên trống -> Ném IllegalArgumentException")
    void testCreateSharedWorkspace_blankName() {
        WorkspaceCreateRequest request = new WorkspaceCreateRequest("   ", "Mô tả");
        assertThrows(IllegalArgumentException.class, () ->
                workspaceService.createSharedWorkspace(request, owner));
    }

    @Test
    @DisplayName("STT 5: Người dùng không thuộc Quỹ truy cập -> Ném AccessDeniedException")
    void testGetWorkspaceByIdAndCheckAccess_accessDenied() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(sharedWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, 999L, MembershipStatus.ACCEPTED))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
                workspaceService.getWorkspaceByIdAndCheckAccess(10L, 999L));
    }

    @Test
    @DisplayName("STT 14: Xóa OWNER duy nhất -> Bị từ chối (Ném IllegalArgumentException)")
    void testRemoveMember_cannotRemoveSoleOwner() {
        Membership ownerMembership = new Membership(sharedWorkspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED);
        ownerMembership.setId(100L);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(sharedWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, owner.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findById(100L)).thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.countByWorkspaceIdAndRoleAndStatus(10L, MemberRole.OWNER, MembershipStatus.ACCEPTED))
                .thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () ->
                workspaceService.removeMember(10L, 100L, owner.getId()));
    }

    @Test
    @DisplayName("STT 13: OWNER xóa thành viên hợp lệ -> Thành công")
    void testRemoveMember_byOwner_success() {
        Membership ownerMembership = new Membership(sharedWorkspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED);
        Membership viewerMembership = new Membership(sharedWorkspace, member, MemberRole.VIEWER, MembershipStatus.ACCEPTED);
        viewerMembership.setId(200L);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(sharedWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, owner.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findById(200L)).thenReturn(Optional.of(viewerMembership));

        workspaceService.removeMember(10L, 200L, owner.getId());

        verify(membershipRepository).delete(viewerMembership);
    }

    @Test
    @DisplayName("STT 15: MANAGER cố thay đổi quyền của OWNER -> Bị từ chối")
    void testUpdateMemberRole_managerCannotModifyOwner() {
        Membership managerMembership = new Membership(sharedWorkspace, manager, MemberRole.MANAGER, MembershipStatus.ACCEPTED);
        Membership ownerMembership = new Membership(sharedWorkspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED);
        ownerMembership.setId(100L);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(sharedWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, manager.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(managerMembership));
        when(membershipRepository.findById(100L)).thenReturn(Optional.of(ownerMembership));

        assertThrows(AccessDeniedException.class, () ->
                workspaceService.updateMemberRole(10L, 100L, MemberRole.VIEWER, manager.getId()));
    }

    @Test
    @DisplayName("STT 16: VIEWER cố chỉnh sửa Workspace -> Bị từ chối")
    void testUpdateWorkspace_viewerCannotEdit() {
        Membership viewerMembership = new Membership(sharedWorkspace, member, MemberRole.VIEWER, MembershipStatus.ACCEPTED);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(sharedWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, member.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(viewerMembership));

        WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("Tên Mới", "Mô tả mới");

        assertThrows(AccessDeniedException.class, () ->
                workspaceService.updateWorkspace(10L, request, member.getId()));
    }

    @Test
    @DisplayName("STT 17: Thành viên rời khỏi Workspace -> Xóa Membership thành công")
    void testLeaveWorkspace_memberSuccess() {
        Membership viewerMembership = new Membership(sharedWorkspace, member, MemberRole.VIEWER, MembershipStatus.ACCEPTED);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(sharedWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, member.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(viewerMembership));

        workspaceService.leaveWorkspace(10L, member.getId());

        verify(membershipRepository).delete(viewerMembership);
    }

    @Test
    @DisplayName("Chủ sở hữu duy nhất không thể rời khỏi Workspace mà chưa chuyển quyền")
    void testLeaveWorkspace_soleOwnerCannotLeave() {
        Membership ownerMembership = new Membership(sharedWorkspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED);

        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(sharedWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, owner.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.countByWorkspaceIdAndRoleAndStatus(10L, MemberRole.OWNER, MembershipStatus.ACCEPTED))
                .thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () ->
                workspaceService.leaveWorkspace(10L, owner.getId()));
    }

    @Test
    @DisplayName("Không được xóa Quỹ cá nhân mặc định")
    void testDeleteWorkspace_cannotDeletePersonal() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(personalWorkspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(1L, owner.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(new Membership(personalWorkspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED)));

        assertThrows(IllegalArgumentException.class, () ->
                workspaceService.deleteWorkspace(1L, owner.getId()));
    }
}

