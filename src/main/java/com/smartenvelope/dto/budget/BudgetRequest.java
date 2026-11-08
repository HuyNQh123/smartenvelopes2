package com.smartenvelope.dto.budget;

import com.smartenvelope.enums.RolloverPolicy;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetRequest(
        @NotBlank(message = "Vui lòng nhập tên kỳ ngân sách.")
        @Size(max = 100, message = "Tên kỳ ngân sách tối đa 100 ký tự.")
        String name,

        @NotNull(message = "Vui lòng chọn tháng.")
        @Min(value = 1, message = "Tháng phải từ 1 đến 12.")
        @Max(value = 12, message = "Tháng phải từ 1 đến 12.")
        Integer month,

        @NotNull(message = "Vui lòng nhập năm.")
        @Min(value = 2000, message = "Năm không hợp lệ.")
        Integer year,

        @NotNull(message = "Vui lòng nhập thu nhập dự kiến.")
        @DecimalMin(value = "0.0", message = "Thu nhập dự kiến không được âm.")
        @Digits(integer = 19, fraction = 0, message = "Nhập số tiền nguyên VND, tối đa 19 chữ số.")
        BigDecimal expectedIncome,

        RolloverPolicy rolloverPolicy) {

    public BudgetRequest {
        if (rolloverPolicy == null) {
            rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;
        }
    }
}
