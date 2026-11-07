package com.smartenvelope.repository;

import com.smartenvelope.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu với bảng Workspace (Quỹ chung).
 * Phụ trách bởi: Thành viên 3.
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}
