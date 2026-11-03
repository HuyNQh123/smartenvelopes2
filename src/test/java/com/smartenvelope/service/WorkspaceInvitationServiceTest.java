package com.smartenvelope.service;

import com.smartenvelope.dto.workspace.WorkspaceInviteRequest;
import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.entity.WorkspaceInvitation;
import com.smartenvelope.enums.InvitationStatus;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.NotificationRepository;
import com.smartenvelope.repository.UserRepository;
import com.smartenvelope.repository.WorkspaceInvitationRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceInvitationServiceTest {

    @Mock
    private WorkspaceInvitationRepository invitationRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WorkspaceContextService workspaceContextService;

    @Mock
    private EmailService emailService;

    private WorkspaceInvitationService invitationService;

    private User owner;
    private User manager;
    private User viewer;
    private User invitee;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        invitationService = new WorkspaceInvitationService(
                invitationRepository,
                workspaceRepository,
                membershipRepository,
                userRepository,
                notificationRepository,
                workspaceContextService,
                emailService
        );

        owner = new User("owner", "pass", "Chủ Quỹ", "owner@example.com", Role.USER);
        owner.setId(1L);

        manager = new User("manager", "pass", "Quản Lý", "manager@example.com", Role.USER);
        manager.setId(2L);

        viewer = new User("viewer", "pass", "Xem Thôi", "viewer@example.com", Role.USER);
        viewer.setId(3L);

        invitee = new User("invitee", "pass", "Người Được Mời", "invitee@example.com", Role.USER);
        invitee.setId(4L);

        workspace = new Workspace("Quỹ Gia Đình", WorkspaceType.SHARED, "Mô tả", owner);
        workspace.setId(10L);
    }

    @Test
    @DisplayName("STT 6: OWNER mời thành viên hợp lệ -> Lưu WorkspaceInvitation thành công")
    void testInviteMember_byOwner_success() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, owner.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(new Membership(workspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED)));
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, invitee.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.empty());
        when(invitationRepository.existsByWorkspaceIdAndEmailAndStatusAndExpiresAtAfter(
                eq(10L), eq("invitee@example.com"), eq(InvitationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(false);
        when(invitationRepository.save(any(WorkspaceInvitation.class))).thenAnswer(i -> i.getArgument(0));

        WorkspaceInviteRequest request = new WorkspaceInviteRequest("invitee@example.com", MemberRole.VIEWER);
        WorkspaceInvitation invitation = invitationService.inviteMember(10L, request, owner);

        assertNotNull(invitation);
        assertEquals("invitee@example.com", invitation.getEmail());
        assertEquals(MemberRole.VIEWER, invitation.getRole());
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());

        verify(emailService).sendWorkspaceInvitationEmail(eq("invitee@example.com"), eq("Quỹ Gia Đình"), eq("Chủ Quỹ"), anyString());
    }

    @Test
    @DisplayName("STT 8: MANAGER mời thành viên hợp lệ -> Thành công")
    void testInviteMember_byManager_success() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, manager.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(new Membership(workspace, manager, MemberRole.MANAGER, MembershipStatus.ACCEPTED)));
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, invitee.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.empty());
        when(invitationRepository.existsByWorkspaceIdAndEmailAndStatusAndExpiresAtAfter(
                eq(10L), eq("invitee@example.com"), eq(InvitationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(false);
        when(invitationRepository.save(any(WorkspaceInvitation.class))).thenAnswer(i -> i.getArgument(0));

        WorkspaceInviteRequest request = new WorkspaceInviteRequest("invitee@example.com", MemberRole.WRITER);
        WorkspaceInvitation invitation = invitationService.inviteMember(10L, request, manager);

        assertNotNull(invitation);
        assertEquals(MemberRole.WRITER, invitation.getRole());
    }

    @Test
    @DisplayName("STT 9: VIEWER cố gửi lời mời -> Ném AccessDeniedException")
    void testInviteMember_byViewer_rejected() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, viewer.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(new Membership(workspace, viewer, MemberRole.VIEWER, MembershipStatus.ACCEPTED)));

        WorkspaceInviteRequest request = new WorkspaceInviteRequest("invitee@example.com", MemberRole.VIEWER);

        assertThrows(AccessDeniedException.class, () ->
                invitationService.inviteMember(10L, request, viewer));
    }

    @Test
    @DisplayName("STT 7: Mời email đã là thành viên trong Quỹ -> Ném IllegalArgumentException")
    void testInviteMember_alreadyMember_rejected() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, owner.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(new Membership(workspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED)));
        when(userRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(invitee));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, invitee.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(new Membership(workspace, invitee, MemberRole.VIEWER, MembershipStatus.ACCEPTED)));

        WorkspaceInviteRequest request = new WorkspaceInviteRequest("invitee@example.com", MemberRole.VIEWER);

        assertThrows(IllegalArgumentException.class, () ->
                invitationService.inviteMember(10L, request, owner));
    }

    @Test
    @DisplayName("Không được tự mời chính mình -> Ném IllegalArgumentException")
    void testInviteMember_cannotInviteSelf() {
        when(workspaceRepository.findById(10L)).thenReturn(Optional.of(workspace));
        when(membershipRepository.findByWorkspaceIdAndUserIdAndStatus(10L, owner.getId(), MembershipStatus.ACCEPTED))
                .thenReturn(Optional.of(new Membership(workspace, owner, MemberRole.OWNER, MembershipStatus.ACCEPTED)));

        WorkspaceInviteRequest request = new WorkspaceInviteRequest("owner@example.com", MemberRole.VIEWER);

        assertThrows(IllegalArgumentException.class, () ->
                invitationService.inviteMember(10L, request, owner));
    }

    @Test
    @DisplayName("STT 10: Người nhận đúng email chấp nhận lời mời -> Tạo Membership ACCEPTED")
    void testAcceptInvitation_success() {
        WorkspaceInvitation invitation = new WorkspaceInvitation(
                workspace, "invitee@example.com", owner, MemberRole.WRITER, "token-123", LocalDateTime.now().plusDays(5));
        invitation.setId(50L);

        when(invitationRepository.findById(50L)).thenReturn(Optional.of(invitation));
        when(membershipRepository.findByWorkspaceIdAndUserId(10L, invitee.getId())).thenReturn(Optional.empty());
        when(membershipRepository.save(any(Membership.class))).thenAnswer(i -> i.getArgument(0));

        Membership membership = invitationService.acceptInvitation(50L, invitee);

        assertNotNull(membership);
        assertEquals(MemberRole.WRITER, membership.getRole());
        assertEquals(MembershipStatus.ACCEPTED, membership.getStatus());
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());

        verify(workspaceContextService).setCurrentWorkspaceId(10L);
    }

    @Test
    @DisplayName("STT 11: Người có email khác cố chấp nhận lời mời -> Ném AccessDeniedException")
    void testAcceptInvitation_emailMismatch_rejected() {
        WorkspaceInvitation invitation = new WorkspaceInvitation(
                workspace, "invitee@example.com", owner, MemberRole.WRITER, "token-123", LocalDateTime.now().plusDays(5));
        invitation.setId(50L);

        when(invitationRepository.findById(50L)).thenReturn(Optional.of(invitation));

        assertThrows(AccessDeniedException.class, () ->
                invitationService.acceptInvitation(50L, viewer)); // viewer có email viewer@example.com != invitee@example.com
    }

    @Test
    @DisplayName("STT 12: Chấp nhận lời mời đã hết hạn -> Ném IllegalStateException")
    void testAcceptInvitation_expired_rejected() {
        WorkspaceInvitation invitation = new WorkspaceInvitation(
                workspace, "invitee@example.com", owner, MemberRole.WRITER, "token-123", LocalDateTime.now().minusDays(1));
        invitation.setId(50L);

        when(invitationRepository.findById(50L)).thenReturn(Optional.of(invitation));

        assertThrows(IllegalStateException.class, () ->
                invitationService.acceptInvitation(50L, invitee));
    }
}

