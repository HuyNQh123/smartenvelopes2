package com.smartenvelope.repository;

import com.smartenvelope.entity.Membership;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng memberships. Phụ trách bởi: Thành viên 1
 * & Thành viên 2.
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByWorkspaceId(Long workspaceId);

    List<Membership> findByWorkspaceIdAndStatus(Long workspaceId, MembershipStatus status);

    List<Membership> findByUserId(Long userId);

    List<Membership> findByUserIdAndStatus(Long userId, MembershipStatus status);

    Optional<Membership> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    Optional<Membership> findByWorkspaceIdAndUserIdAndStatus(Long workspaceId, Long userId,
            MembershipStatus status);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    boolean existsByWorkspaceIdAndUserIdAndStatus(Long workspaceId, Long userId,
            MembershipStatus status);

    long countByWorkspaceIdAndStatus(Long workspaceId, MembershipStatus status);

    long countByWorkspaceIdAndRoleAndStatus(Long workspaceId, MemberRole role,
            MembershipStatus status);
}
