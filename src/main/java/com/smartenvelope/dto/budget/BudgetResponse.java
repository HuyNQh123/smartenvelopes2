package com.smartenvelope.dto.budget;

import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.enums.BudgetStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class BudgetResponse {

    private final Long id;
    private final String name;
    private final Integer month;
    private final Integer year;
    private final BigDecimal totalIncome;
    private final BigDecimal totalAllocated;
    private final BigDecimal remainingBudget;
    private final BudgetStatus status;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public BudgetResponse(BudgetPeriod period) {
        id = period.getId();
        name = period.getName();
        month = period.getMonth();
        year = period.getYear();
        totalIncome = period.getTotalIncome();
        totalAllocated = period.getTotalAllocated();
        remainingBudget = totalIncome.subtract(totalAllocated);
        status = period.getStatus();
        startDate = period.getStartDate();
        endDate = period.getEndDate();
    }
}
