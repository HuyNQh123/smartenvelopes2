package com.smartenvelope.repository;

import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.WorkspaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng workspaces. Phụ trách bởi: Thành viên 1
 * & Thành viên 2.
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findByOwnerId(Long ownerId);

    List<Workspace> findByOwnerIdAndType(Long ownerId, WorkspaceType type);

    @Query("SELECT w FROM Workspace w JOIN Membership m ON m.workspace.id = w.id WHERE m.user.id = :userId AND m.status = com.smartenvelope.enums.MembershipStatus.ACCEPTED")
    List<Workspace> findWorkspacesJoinedByUser(
            @Param("userId")
            Long userId);
}
