package com.smartenvelope.dto.budget;

import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.enums.BudgetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetResponse(
        Long id,
        String name,
        Integer month,
        Integer year,
        BigDecimal expectedIncome,
        BigDecimal totalIncome,
        BigDecimal totalAllocated,
        BigDecimal remainingBudget,
        BudgetStatus status,
        LocalDate startDate,
        LocalDate endDate) {

    public BudgetResponse(BudgetPeriod period) {
        this(period.getId(), period.getName(), period.getMonth(), period.getYear(),
                period.getExpectedIncome(), period.getTotalIncome(), period.getTotalAllocated(),
                period.getTotalIncome().subtract(period.getTotalAllocated()), period.getStatus(),
                period.getStartDate(), period.getEndDate());
    }
}
