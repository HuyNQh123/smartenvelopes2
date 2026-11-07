package com.smartenvelope.dto.savings;

import com.smartenvelope.entity.SavingsGoal;
import com.smartenvelope.enums.SavingsGoalStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class SavingsGoalResponse {

    private final Long id;
    private final String name;
    private final BigDecimal targetAmount;
    private final BigDecimal currentAmount;
    private final BigDecimal remainingAmount;
    private final int progressPercent;
    private final Long remainingDays;
    private final LocalDate targetDate;
    private final SavingsGoalStatus status;

    public SavingsGoalResponse(SavingsGoal goal) {
        id = goal.getId();
        name = goal.getName();
        targetAmount = goal.getTargetAmount();
        currentAmount = goal.getCurrentAmount();
        remainingAmount = targetAmount.subtract(currentAmount).max(BigDecimal.ZERO);
        progressPercent = currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 0, RoundingMode.DOWN)
                .min(BigDecimal.valueOf(100)).intValue();
        targetDate = goal.getTargetDate();
        remainingDays = targetDate == null ? null : Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), targetDate));
        status = goal.getStatus();
    }
}
