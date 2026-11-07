package com.smartenvelope.repository;

import com.smartenvelope.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng WorkspaceMember (Thành viên Quỹ chung).
 * Phụ trách bởi: Thành viên 3.
 */
@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    /**
     * Tìm thông tin thành viên của một người dùng trong một Workspace cụ thể.
     */
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    /**
     * Lấy danh sách thành viên thuộc một Workspace.
     */
    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);
}
