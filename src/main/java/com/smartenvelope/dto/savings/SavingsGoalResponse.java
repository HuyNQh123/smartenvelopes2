package com.smartenvelope.dto.savings;

import com.smartenvelope.entity.SavingsGoal;
import com.smartenvelope.enums.SavingsGoalStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record SavingsGoalResponse(
        Long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        int progressPercent,
        Long remainingDays,
        LocalDate targetDate,
        SavingsGoalStatus status) {

    public SavingsGoalResponse(SavingsGoal goal) {

        this(goal.getId(), goal.getName(), goal.getTargetAmount(), goal.getCurrentAmount(),
                goal.getTargetAmount().subtract(goal.getCurrentAmount()).max(BigDecimal.ZERO),
                goal.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                        .divide(goal.getTargetAmount(), 0, RoundingMode.DOWN)
                        .min(BigDecimal.valueOf(100)).intValue(),
                goal.getTargetDate() == null ? null
                        : Math.max(0,
                                ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate())),
                goal.getTargetDate(), goal.getStatus());
    }
}
