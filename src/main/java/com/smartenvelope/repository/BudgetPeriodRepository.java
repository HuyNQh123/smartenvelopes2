package com.smartenvelope.repository;

import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.enums.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng budget_periods.
 * Phụ trách bởi: Thành viên 4 (Budget & Envelope Logic).
 */

public interface BudgetPeriodRepository extends JpaRepository<BudgetPeriod, Long> {

    List<BudgetPeriod> findByWorkspaceId(Long workspaceId);

    List<BudgetPeriod> findByWorkspaceIdOrderByYearDescMonthDesc(Long workspaceId);

    Optional<BudgetPeriod> findByWorkspaceIdAndMonthAndYear(Long workspaceId, Integer month, Integer year);

    List<BudgetPeriod> findByWorkspaceIdAndStatus(Long workspaceId, BudgetStatus status);
}
