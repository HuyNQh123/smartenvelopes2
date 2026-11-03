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
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.AccountRepository;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service xử lý toàn bộ nghiệp vụ Quản lý Quỹ tài chính (Workspace) và Phân quyền Thành viên.
 * Phụ trách bởi: Thành viên 1.
 */
@Service
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final MembershipRepository membershipRepository;
    private final WorkspaceContextService workspaceContextService;
    private final AccountRepository accountRepository;

    @Autowired
    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            MembershipRepository membershipRepository,
                            WorkspaceContextService workspaceContextService,
                            AccountRepository accountRepository) {
        this.workspaceRepository = workspaceRepository;
        this.membershipRepository = membershipRepository;
        this.workspaceContextService = workspaceContextService;
        this.accountRepository = accountRepository;
    }

    /**
     * Lấy danh sách tóm tắt tất cả các Workspace mà người dùng đang tham gia (Membership ACCEPTED).
     */
    public List<WorkspaceSummaryDTO> getUserWorkspaces(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<Workspace> workspaces = workspaceRepository.findWorkspacesJoinedByUser(userId);
        Long currentWorkspaceId = null;
        try {
            currentWorkspaceId = workspaceContextService.getCurrentWorkspaceId();
        } catch (Exception ignored) {
        }

        List<WorkspaceSummaryDTO> summaries = new ArrayList<>();
        for (Workspace ws : workspaces) {
            long memberCount = membershipRepository.countByWorkspaceIdAndStatus(ws.getId(), MembershipStatus.ACCEPTED);
            Optional<Membership> membership = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                    ws.getId(), userId, MembershipStatus.ACCEPTED);
            MemberRole role = membership.map(Membership::getRole).orElse(MemberRole.VIEWER);
            boolean isCurrent = Objects.equals(ws.getId(), currentWorkspaceId);

            summaries.add(new WorkspaceSummaryDTO(
                    ws.getId(),
                    ws.getName(),
                    ws.getType(),
                    ws.getDescription(),
                    ws.getOwner() != null ? ws.getOwner().getFullName() : "Không xác định",
                    ws.getOwner() != null ? ws.getOwner().getId() : null,
                    memberCount,
                    role,
                    isCurrent,
                    ws.getCreatedAt()
            ));
        }

        return summaries;
    }

    /**
     * Lấy thông tin Workspace theo ID và kiểm tra quyền truy cập của người dùng.
     */
    public Workspace getWorkspaceByIdAndCheckAccess(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Quỹ tài chính với ID: " + workspaceId));

        boolean hasAccess = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                workspaceId, userId, MembershipStatus.ACCEPTED).isPresent();

        if (!hasAccess) {
            throw new AccessDeniedException("Bạn không có quyền truy cập vào Quỹ tài chính này.");
        }

        return workspace;
    }

    /**
     * Tạo mới một Quỹ tài chính chung (SHARED).
     * Tự động gán quyền OWNER cho người tạo trong cùng một Transaction.
     */
    @Transactional
    public Workspace createSharedWorkspace(WorkspaceCreateRequest request, User creator) {
        if (creator == null) {
            throw new AccessDeniedException("Vui lòng đăng nhập để tạo Quỹ tài chính.");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên Quỹ tài chính không được để trống.");
        }

        // 1. Tạo thực thể Workspace loại SHARED
        Workspace workspace = new Workspace(
                request.getName().trim(),
                WorkspaceType.SHARED,
                request.getDescription() != null ? request.getDescription().trim() : null,
                creator
        );
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // 2. Tự động cấp quyền OWNER cho người tạo
        Membership ownerMembership = new Membership(
                savedWorkspace,
                creator,
                MemberRole.OWNER,
                MembershipStatus.ACCEPTED
        );
        membershipRepository.save(ownerMembership);

        // 3. Tự động chuyển Workspace hiện tại sang Quỹ vừa tạo
        try {
            workspaceContextService.setCurrentWorkspaceId(savedWorkspace.getId());
        } catch (Exception ignored) {
        }

        return savedWorkspace;
    }

    /**
     * Cập nhật tên và mô tả Quỹ tài chính (Chỉ OWNER hoặc MANAGER có quyền).
     */
    @Transactional
    public Workspace updateWorkspace(Long workspaceId, WorkspaceUpdateRequest request, Long userId) {
        Workspace workspace = getWorkspaceByIdAndCheckAccess(workspaceId, userId);
        MemberRole role = getUserRoleInWorkspace(workspaceId, userId);

        if (role != MemberRole.OWNER && role != MemberRole.MANAGER) {
            throw new AccessDeniedException("Chỉ Chủ sở hữu (OWNER) hoặc Quản lý (MANAGER) mới được chỉnh sửa thông tin Quỹ.");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên Quỹ không được để trống.");
        }

        workspace.setName(request.getName().trim());
        workspace.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        return workspaceRepository.save(workspace);
    }

    /**
     * Lấy danh sách thành viên trong Workspace kèm trạng thái và quyền quản lý.
     */
    public List<WorkspaceMemberDTO> getWorkspaceMembers(Long workspaceId, Long currentUserId) {
        Workspace workspace = getWorkspaceByIdAndCheckAccess(workspaceId, currentUserId);
        MemberRole currentUserRole = getUserRoleInWorkspace(workspaceId, currentUserId);

        List<Membership> memberships = membershipRepository.findByWorkspaceIdAndStatus(
                workspaceId, MembershipStatus.ACCEPTED);

        List<WorkspaceMemberDTO> memberDTOs = new ArrayList<>();
        for (Membership m : memberships) {
            User u = m.getUser();
            boolean isOwner = (m.getRole() == MemberRole.OWNER);
            boolean isSelf = Objects.equals(u.getId(), currentUserId);

            boolean canBeManaged = false;
            boolean canBeRemoved = false;

            if (currentUserRole == MemberRole.OWNER) {
                // OWNER có thể quản lý và xóa tất cả thành viên khác trừ chính mình
                canBeManaged = !isSelf;
                canBeRemoved = !isSelf;
            } else if (currentUserRole == MemberRole.MANAGER) {
                // MANAGER chỉ quản lý hoặc xóa thành viên cấp thấp hơn (WRITER, VIEWER)
                if (!isOwner && m.getRole() != MemberRole.MANAGER && !isSelf) {
                    canBeManaged = true;
                    canBeRemoved = true;
                }
            }

            memberDTOs.add(new WorkspaceMemberDTO(
                    m.getId(),
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getAvatarUrl(),
                    m.getRole(),
                    m.getStatus(),
                    m.getJoinedAt(),
                    isOwner,
                    canBeManaged,
                    canBeRemoved
            ));
        }

        return memberDTOs;
    }

    /**
     * Thay đổi vai trò (Role) của một thành viên trong Quỹ.
     */
    @Transactional
    public void updateMemberRole(Long workspaceId, Long targetMembershipId, MemberRole newRole, Long currentUserId) {
        Workspace workspace = getWorkspaceByIdAndCheckAccess(workspaceId, currentUserId);
        MemberRole currentUserRole = getUserRoleInWorkspace(workspaceId, currentUserId);

        Membership targetMembership = membershipRepository.findById(targetMembershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thành viên."));

        if (!Objects.equals(targetMembership.getWorkspace().getId(), workspaceId)) {
            throw new IllegalArgumentException("Thành viên không thuộc Quỹ này.");
        }

        if (newRole == null) {
            throw new IllegalArgumentException("Vai trò mới không được để trống.");
        }

        // Kiểm tra quyền hạn cơ bản đối với OWNER
        if (currentUserRole != MemberRole.OWNER && targetMembership.getRole() == MemberRole.OWNER) {
            throw new AccessDeniedException("Chỉ Chủ sở hữu mới có quyền thay đổi vai trò của Chủ sở hữu.");
        }

        // Kiểm tra không được hạ quyền OWNER cuối cùng của Quỹ
        if (targetMembership.getRole() == MemberRole.OWNER && newRole != MemberRole.OWNER) {
            long ownerCount = membershipRepository.countByWorkspaceIdAndRoleAndStatus(
                    workspaceId, MemberRole.OWNER, MembershipStatus.ACCEPTED);
            if (ownerCount <= 1) {
                throw new IllegalArgumentException("Không thể hạ quyền Chủ sở hữu duy nhất của Quỹ.");
            }
        }

        if (currentUserRole == MemberRole.OWNER) {
            // Nếu OWNER chuyển quyền sở hữu cho người khác
            if (newRole == MemberRole.OWNER && !Objects.equals(targetMembership.getUser().getId(), currentUserId)) {
                // 1. Chuyển OWNER cũ thành MANAGER
                Optional<Membership> currentOwnerMembership = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                        workspaceId, currentUserId, MembershipStatus.ACCEPTED);
                currentOwnerMembership.ifPresent(m -> {
                    m.setRole(MemberRole.MANAGER);
                    membershipRepository.save(m);
                });

                // 2. Chuyển người mới thành OWNER
                targetMembership.setRole(MemberRole.OWNER);
                membershipRepository.save(targetMembership);

                // 3. Cập nhật trường owner trong bảng workspaces
                workspace.setOwner(targetMembership.getUser());
                workspaceRepository.save(workspace);
                return;
            }

            // Các vai trò khác
            targetMembership.setRole(newRole);
            membershipRepository.save(targetMembership);

        } else if (currentUserRole == MemberRole.MANAGER) {
            // MANAGER không được đổi quyền OWNER hoặc MANAGER
            if (targetMembership.getRole() == MemberRole.OWNER || targetMembership.getRole() == MemberRole.MANAGER) {
                throw new AccessDeniedException("Quản lý không có quyền thay đổi vai trò của Chủ sở hữu hoặc Quản lý khác.");
            }
            if (newRole == MemberRole.OWNER || newRole == MemberRole.MANAGER) {
                throw new AccessDeniedException("Quản lý không có quyền nâng thành viên lên cấp Quản lý hoặc Chủ sở hữu.");
            }
            targetMembership.setRole(newRole);
            membershipRepository.save(targetMembership);
        } else {
            throw new AccessDeniedException("Bạn không có quyền thay đổi vai trò thành viên.");
        }
    }

    /**
     * Xóa thành viên khỏi Quỹ tài chính.
     */
    @Transactional
    public void removeMember(Long workspaceId, Long targetMembershipId, Long currentUserId) {
        Workspace workspace = getWorkspaceByIdAndCheckAccess(workspaceId, currentUserId);
        MemberRole currentUserRole = getUserRoleInWorkspace(workspaceId, currentUserId);

        Membership targetMembership = membershipRepository.findById(targetMembershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thành viên cần xóa."));

        if (!Objects.equals(targetMembership.getWorkspace().getId(), workspaceId)) {
            throw new IllegalArgumentException("Thành viên không thuộc Quỹ này.");
        }

        // Không được xóa OWNER cuối cùng
        if (targetMembership.getRole() == MemberRole.OWNER) {
            long ownerCount = membershipRepository.countByWorkspaceIdAndRoleAndStatus(
                    workspaceId, MemberRole.OWNER, MembershipStatus.ACCEPTED);
            if (ownerCount <= 1) {
                throw new IllegalArgumentException("Không thể xóa Chủ sở hữu duy nhất của Quỹ.");
            }
        }

        if (currentUserRole == MemberRole.OWNER) {
            if (Objects.equals(targetMembership.getUser().getId(), currentUserId)) {
                throw new IllegalArgumentException("Chủ sở hữu không thể tự xóa chính mình khỏi danh sách. Vui lòng chuyển quyền hoặc Xóa Quỹ.");
            }
            membershipRepository.delete(targetMembership);
        } else if (currentUserRole == MemberRole.MANAGER) {
            if (targetMembership.getRole() == MemberRole.OWNER || targetMembership.getRole() == MemberRole.MANAGER) {
                throw new AccessDeniedException("Quản lý không có quyền xóa Chủ sở hữu hoặc Quản lý khác.");
            }
            membershipRepository.delete(targetMembership);
        } else {
            throw new AccessDeniedException("Bạn không có quyền xóa thành viên khỏi Quỹ.");
        }

        // Nếu thành viên bị xóa đang chọn Workspace này trong Session, tự động fallback
        handleSessionFallbackIfActive(workspaceId, targetMembership.getUser().getId());
    }

    /**
     * Tự rời khỏi Quỹ tài chính.
     */
    @Transactional
    public void leaveWorkspace(Long workspaceId, Long currentUserId) {
        Workspace workspace = getWorkspaceByIdAndCheckAccess(workspaceId, currentUserId);

        Membership myMembership = membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                workspaceId, currentUserId, MembershipStatus.ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn không phải thành viên của Quỹ này."));

        // Nếu là Quỹ cá nhân: Không cho phép rời
        if (workspace.getType() == WorkspaceType.PERSONAL) {
            throw new IllegalArgumentException("Bạn không thể rời khỏi Quỹ cá nhân của chính mình.");
        }

        // Nếu là OWNER duy nhất: Không cho phép rời mà phải chuyển quyền trước
        if (myMembership.getRole() == MemberRole.OWNER) {
            long ownerCount = membershipRepository.countByWorkspaceIdAndRoleAndStatus(
                    workspaceId, MemberRole.OWNER, MembershipStatus.ACCEPTED);
            if (ownerCount <= 1) {
                throw new IllegalArgumentException("Bạn là Chủ sở hữu duy nhất của Quỹ. Vui lòng chuyển quyền Chủ sở hữu cho thành viên khác trước khi rời Quỹ.");
            }
        }

        membershipRepository.delete(myMembership);

        // Fallback Workspace trong Session nếu người dùng đang dùng Quỹ này
        handleSessionFallbackIfActive(workspaceId, currentUserId);
    }

    /**
     * Xóa hoàn toàn một Workspace (Chỉ OWNER có quyền).
     */
    @Transactional
    public void deleteWorkspace(Long workspaceId, Long currentUserId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Quỹ tài chính cần xóa."));

        // 1. Kiểm tra quyền OWNER
        MemberRole role = getUserRoleInWorkspace(workspaceId, currentUserId);
        if (role != MemberRole.OWNER) {
            throw new AccessDeniedException("Chỉ Chủ sở hữu (OWNER) mới có quyền xóa Quỹ tài chính.");
        }

        // 2. Không cho phép xóa Quỹ cá nhân
        if (workspace.getType() == WorkspaceType.PERSONAL) {
            throw new IllegalArgumentException("Không thể xóa Quỹ cá nhân mặc định của hệ thống.");
        }

        // 3. Kiểm tra xem có tài khoản tiền (accounts) đang liên kết không
        long accountCount = accountRepository.findByWorkspaceId(workspaceId).size();
        if (accountCount > 0) {
            throw new IllegalStateException("Quỹ này hiện đang có " + accountCount + " tài khoản tiền. Vui lòng xử lý hoặc xóa các tài khoản tiền trước khi xóa Quỹ.");
        }

        // 4. Nếu Quỹ đang được chọn trong Session của người dùng hiện tại, chuyển sang Quỹ khác trước
        handleSessionFallbackIfActive(workspaceId, currentUserId);

        // 5. Xóa Workspace (Cascade sẽ tự động xóa memberships và workspace_invitations)
        workspaceRepository.delete(workspace);
    }

    /**
     * Tiện ích lấy vai trò của người dùng trong một Workspace cụ thể.
     */
    public MemberRole getUserRoleInWorkspace(Long workspaceId, Long userId) {
        if (workspaceId == null || userId == null) {
            return MemberRole.VIEWER;
        }
        return membershipRepository.findByWorkspaceIdAndUserIdAndStatus(
                workspaceId, userId, MembershipStatus.ACCEPTED)
                .map(Membership::getRole)
                .orElse(MemberRole.VIEWER);
    }

    /**
     * Xử lý chuyển đổi Session về Quỹ mặc định nếu Workspace vừa rời/xóa đang được kích hoạt trong Session.
     */
    private void handleSessionFallbackIfActive(Long workspaceId, Long userId) {
        try {
            Long currentSessionWsId = workspaceContextService.getCurrentWorkspaceId();
            if (Objects.equals(currentSessionWsId, workspaceId)) {
                workspaceContextService.clearWorkspaceSession();
                // Tự động kích hoạt lại Quỹ mặc định
                workspaceContextService.getCurrentWorkspaceId();
            }
        } catch (Exception ignored) {
        }
    }
}
