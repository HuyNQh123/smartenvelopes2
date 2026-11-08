package com.smartenvelope.repository;

import com.smartenvelope.entity.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng envelopes. Phụ trách bởi: Thành viên 4
 * (Budget & Envelope Logic).
 */

public interface EnvelopeRepository extends JpaRepository<Envelope, Long> {

    List<Envelope> findByBudgetPeriodId(Long budgetPeriodId);

    Optional<Envelope> findByBudgetPeriodIdAndCategoryId(Long budgetPeriodId, Long categoryId);

    @Query("SELECT e FROM Envelope e WHERE e.budgetPeriod.id = :budgetPeriodId AND e.spentAmount > e.allocatedAmount")
    List<Envelope> findOverspentEnvelopes(
            @Param("budgetPeriodId")
            Long budgetPeriodId);

    @Query("SELECT e FROM Envelope e WHERE e.budgetPeriod.id = :budgetPeriodId AND (e.spentAmount * 100 / e.allocatedAmount) >= e.warningThreshold")
    List<Envelope> findNearLimitEnvelopes(
            @Param("budgetPeriodId")
            Long budgetPeriodId);
}
