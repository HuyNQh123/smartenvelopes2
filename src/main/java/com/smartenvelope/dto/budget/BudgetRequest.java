package com.smartenvelope.dto.budget;

import com.smartenvelope.enums.RolloverPolicy;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class BudgetRequest {

    @NotBlank(message = "Vui lòng nhập tên kỳ ngân sách.")
    @Size(max = 100, message = "Tên kỳ ngân sách tối đa 100 ký tự.")
    private String name;

    @NotNull(message = "Vui lòng chọn tháng.")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12.")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12.")
    private Integer month;

    @NotNull(message = "Vui lòng nhập năm.")
    @Min(value = 2000, message = "Năm không hợp lệ.")
    private Integer year;

    @NotNull(message = "Vui lòng nhập tổng thu nhập.")
    @DecimalMin(value = "0.0", message = "Thu nhập không được âm.")
    private BigDecimal totalIncome;

    private RolloverPolicy rolloverPolicy = RolloverPolicy.SWEEP_TO_PIGGY;
}
