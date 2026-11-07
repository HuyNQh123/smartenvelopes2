package com.smartenvelope.dto.savings;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SavingsGoalRequest {

    @NotBlank(message = "Vui lòng nhập tên mục tiêu.")
    @Size(max = 100, message = "Tên mục tiêu tối đa 100 ký tự.")
    private String name;

    @NotNull(message = "Vui lòng nhập số tiền mục tiêu.")
    @DecimalMin(value = "1", message = "Mục tiêu phải lớn hơn 0.")
    private BigDecimal targetAmount;

    @DecimalMin(value = "0", message = "Số tiền ban đầu không được âm.")
    private BigDecimal initialAmount = BigDecimal.ZERO;

    private LocalDate targetDate;
}
