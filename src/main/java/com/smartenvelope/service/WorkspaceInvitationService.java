package com.smartenvelope.service;

import com.smartenvelope.dto.workspace.WorkspaceInviteRequest;
import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.Notification;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.entity.WorkspaceInvitation;
import com.smartenvelope.enums.InvitationStatus;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.NotificationStatus;
import com.smartenvelope.enums.NotificationType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.NotificationRepository;
import com.smartenvelope.repository.UserRepository;
import com.smartenvelope.repository.WorkspaceInvitationRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service quản lý Lời mời tham gia Quỹ tài chính (Workspace Invitations).
 * Phụ trách bởi: Thành viên 1.
 */
@Service
@Transactional(readOnly = true)
public class WorkspaceInvitationService {

    private final WorkspaceInvitationRepository invitationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final WorkspaceContextService workspaceContextService;
    private final EmailService emailService;

    @Autowired
    public WorkspaceInvitationService(WorkspaceInvitationRepository invitationRepository,
                                      WorkspaceRepository workspaceRepository,
                                      MembershipRepository membershipRepository,
                                      UserRepository userRepository,
                                      NotificationRepository notificationRepository,
                                      WorkspaceContextService workspaceContextService,
                                      EmailService emailService) {
        this.invitationRepository = invitationRepository;
        this.workspaceRepository = workspaceRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.workspaceContextService = workspaceContextService;
        this.emailService = emailService;
    }

    /**
     * Gửi lời mời tham gia Quỹ tới một địa chỉ email.
     */
    @Transactional
    public WorkspaceInvitation inviteMember(Long workspaceId, WorkspaceInviteRequest request, User inviter) {
        if (inviter == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập để gửi lời mời.");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Quỹ tài chính ID: " + workspaceId));

        // 1. Kiểm tra quyền hạn của người gửi (chỉ OWNER hoặc MANAGER)
        Optional<Membership> inviterMembership = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                workspaceId, inviter.getId(), MembershipStatus.ACCEPTED);

        if (inviterMembership.isEmpty() ||
                (inviterMembership.get().getRole() != MemberRole.OWNER && inviterMembership.get().getRole() != MemberRole.MANAGER)) {
            throw new AccessDeniedException("Chỉ Chủ sở hữu hoặc Quản lý mới có quyền gửi lời mời tham gia Quỹ.");
        }

        // 2. Kiểm tra email người nhận
        String recipientEmail = request.getEmail() != null ? request.getEmail().toLowerCase().trim() : "";
        if (recipientEmail.isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ email không được để trống.");
        }

        // 3. Không cho phép tự mời chính mình
        if (recipientEmail.equalsIgnoreCase(inviter.getEmail())) {
            throw new IllegalArgumentException("Bạn không thể tự gửi lời mời tham gia cho chính tài khoản của mình.");
        }

        // 4. Không cho phép mời người đã là thành viên của Quỹ
        Optional<User> existingUserOpt = userRepository.findByEmail(recipientEmail);
        if (existingUserOpt.isPresent()) {
            boolean isMember = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                    workspaceId, existingUserOpt.get().getId(), MembershipStatus.ACCEPTED).isPresent();
            if (isMember) {
                throw new IllegalArgumentException("Người dùng có email " + recipientEmail + " đã là thành viên của Quỹ này.");
            }
        }

        // 5. Không cho phép tạo lời mời trùng lặp đang còn hiệu lực
        boolean hasPending = invitationRepository.existsByWorkspaceIdAndEmailAndStatusAndExpiresAtAfter(
                workspaceId, recipientEmail, InvitationStatus.PENDING, LocalDateTime.now());
        if (hasPending) {
            throw new IllegalArgumentException("Đã có một lời mời đang chờ gửi tới email " + recipientEmail + ". Vui lòng chờ người nhận phản hồi hoặc hủy lời mời cũ.");
        }

        // 6. Vai trò mời không được là OWNER
        MemberRole roleToAssign = request.getRole() != null ? request.getRole() : MemberRole.VIEWER;
        if (roleToAssign == MemberRole.OWNER) {
            throw new IllegalArgumentException("Không thể mời thành viên trực tiếp với vai trò Chủ sở hữu. Bạn có thể chuyển quyền sau khi thành viên đã tham gia.");
        }

        // 7. Tạo mã bảo mật token và thời hạn lời mời (7 ngày)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        WorkspaceInvitation invitation = new WorkspaceInvitation(
                workspace, recipientEmail, inviter, roleToAssign, token, expiresAt);
        WorkspaceInvitation savedInvitation = invitationRepository.save(invitation);

        // 8. Gửi email qua EmailService
        String inviteUrl = "/invitations/join?token=" + token;
        emailService.sendWorkspaceInvitationEmail(
                recipientEmail, workspace.getName(), inviter.getFullName(), inviteUrl);

        // 9. Nếu người dùng đã có tài khoản, gửi thêm Notification hệ thống
        if (existingUserOpt.isPresent()) {
            try {
                Notification notif = new Notification(
                        existingUserOpt.get(),
                        NotificationType.INVITATION,
                        "Lời mời tham gia Quỹ tài chính",
                        inviter.getFullName() + " đã mời bạn tham gia Quỹ \"" + workspace.getName() + "\" với vai trò " + roleToAssign + ".",
                        "/invitations"
                );
                notificationRepository.save(notif);
            } catch (Exception ignored) {
            }
        }

        return savedInvitation;
    }

    /**
     * Lấy danh sách các lời mời còn hiệu lực gửi tới email của người dùng hiện tại.
     */
    public List<WorkspaceInvitation> getPendingInvitationsForEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        String normalizedEmail = email.toLowerCase().trim();
        List<WorkspaceInvitation> list = invitationRepository.findByEmailAndStatusOrderByCreatedAtDesc(
                normalizedEmail, InvitationStatus.PENDING);

        // Lọc bỏ các lời mời đã quá hạn
        return list.stream()
                .filter(inv -> !inv.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách lời mời của một Workspace (dành cho OWNER và MANAGER).
     */
    public List<WorkspaceInvitation> getPendingInvitationsForWorkspace(Long workspaceId, Long currentUserId) {
        // Kiểm tra quyền truy cập Quỹ
        Optional<Membership> membership = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                workspaceId, currentUserId, MembershipStatus.ACCEPTED);
        if (membership.isEmpty() ||
                (membership.get().getRole() != MemberRole.OWNER && membership.get().getRole() != MemberRole.MANAGER)) {
            throw new AccessDeniedException("Chỉ Chủ sở hữu hoặc Quản lý mới có quyền xem danh sách lời mời.");
        }

        List<WorkspaceInvitation> list = invitationRepository.findByWorkspaceIdAndStatusOrderByCreatedAtDesc(
                workspaceId, InvitationStatus.PENDING);

        return list.stream()
                .filter(inv -> !inv.isExpired())
                .collect(Collectors.toList());
    }

    /**
     * Hủy một lời mời đang chờ (Chỉ OWNER hoặc MANAGER của Quỹ có quyền).
     */
    @Transactional
    public void cancelInvitation(Long invitationId, Long currentUserId) {
        WorkspaceInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin lời mời."));

        Optional<Membership> membership = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                invitation.getWorkspace().getId(), currentUserId, MembershipStatus.ACCEPTED);

        if (membership.isEmpty() ||
                (membership.get().getRole() != MemberRole.OWNER && membership.get().getRole() != MemberRole.MANAGER)) {
            throw new AccessDeniedException("Bạn không có quyền hủy lời mời này.");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
    }

    /**
     * Lấy thông tin lời mời theo Token bí mật.
     */
    public WorkspaceInvitation getInvitationByToken(String token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Mã lời mời không tồn tại hoặc đường link không hợp lệ."));
    }

    /**
     * Chấp nhận lời mời tham gia Quỹ tài chính.
     */
    @Transactional
    public Membership acceptInvitation(Long invitationId, User user) {
        if (user == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập để chấp nhận lời mời.");
        }

        WorkspaceInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin lời mời."));

        return processAcceptInvitation(invitation, user);
    }

    /**
     * Chấp nhận lời mời thông qua Token bí mật trên đường dẫn liên kết.
     */
    @Transactional
    public Membership acceptInvitationByToken(String token, User user) {
        if (user == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập để chấp nhận lời mời.");
        }

        WorkspaceInvitation invitation = getInvitationByToken(token);
        return processAcceptInvitation(invitation, user);
    }

    /**
     * Logic chấp nhận lời mời chung (kiểm tra hạn, email, cập nhật Membership và Session).
     */
    private Membership processAcceptInvitation(WorkspaceInvitation invitation, User user) {
        // 1. Kiểm tra trạng thái và hạn sử dụng
        if (!invitation.canBeAccepted()) {
            throw new IllegalStateException("Lời mời này đã hết hạn hoặc không còn ở trạng thái chờ chấp nhận.");
        }

        // 2. Kiểm tra email tài khoản đăng nhập phải trùng khớp với email được mời
        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new AccessDeniedException("Tài khoản đang đăng nhập (" + user.getEmail() +
                    ") không khớp với địa chỉ email nhận lời mời (" + invitation.getEmail() + ").");
        }

        Workspace workspace = invitation.getWorkspace();

        // 3. Tạo hoặc kích hoạt Membership
        Optional<Membership> existingMembership = membershipRepository.findByWorkspaceIdAndUserId(
                workspace.getId(), user.getId());

        Membership membership;
        if (existingMembership.isPresent()) {
            membership = existingMembership.get();
            membership.setRole(invitation.getRole());
            membership.setStatus(MembershipStatus.ACCEPTED);
            membership.setJoinedAt(LocalDateTime.now());
        } else {
            membership = new Membership(
                    workspace,
                    user,
                    invitation.getRole(),
                    MembershipStatus.ACCEPTED
            );
        }
        Membership savedMembership = membershipRepository.save(membership);

        // 4. Cập nhật trạng thái lời mời thành ACCEPTED
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        // 5. Tự động chuyển Workspace hiện tại sang Quỹ vừa tham gia
        try {
            workspaceContextService.setCurrentWorkspaceId(workspace.getId());
        } catch (Exception ignored) {
        }

        return savedMembership;
    }

    /**
     * Từ chối lời mời tham gia Quỹ tài chính.
     */
    @Transactional
    public void declineInvitation(Long invitationId, User user) {
        if (user == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập để thực hiện thao tác.");
        }

        WorkspaceInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin lời mời."));

        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new AccessDeniedException("Bạn không có quyền từ chối lời mời gửi tới địa chỉ email khác.");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }
}
