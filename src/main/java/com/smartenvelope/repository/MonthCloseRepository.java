package com.smartenvelope.repository;

import com.smartenvelope.entity.MonthClose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng month_closes.
 * Phụ trách bởi: Thành viên 4 & Thành viên 5.
 */

public interface MonthCloseRepository extends JpaRepository<MonthClose, Long> {

    Optional<MonthClose> findByBudgetPeriodId(Long budgetPeriodId);

    boolean existsByBudgetPeriodId(Long budgetPeriodId);
}
