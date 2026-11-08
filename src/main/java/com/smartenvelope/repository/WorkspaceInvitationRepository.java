package com.smartenvelope.repository;

import com.smartenvelope.entity.WorkspaceInvitation;
import com.smartenvelope.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng workspace_invitations. Phụ trách bởi:
 * Thành viên 1.
 */
@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, Long> {

    Optional<WorkspaceInvitation> findByToken(String token);

    List<WorkspaceInvitation> findByEmailAndStatusOrderByCreatedAtDesc(String email,
            InvitationStatus status);

    List<WorkspaceInvitation> findByWorkspaceIdAndStatusOrderByCreatedAtDesc(Long workspaceId,
            InvitationStatus status);

    List<WorkspaceInvitation> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    Optional<WorkspaceInvitation> findByWorkspaceIdAndEmailAndStatus(Long workspaceId, String email,
            InvitationStatus status);

    boolean existsByWorkspaceIdAndEmailAndStatusAndExpiresAtAfter(Long workspaceId, String email,
            InvitationStatus status, LocalDateTime now);
}
