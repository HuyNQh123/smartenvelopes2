package com.smartenvelope.repository;

import com.smartenvelope.entity.SavingsGoal;
import com.smartenvelope.enums.SavingsGoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng savings_goals. Phụ trách bởi: Thành viên
 * 5 (Savings, Piggy Bank & Analytics).
 */
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByWorkspaceId(Long workspaceId);

    List<SavingsGoal> findByWorkspaceIdAndStatus(Long workspaceId, SavingsGoalStatus status);
}
