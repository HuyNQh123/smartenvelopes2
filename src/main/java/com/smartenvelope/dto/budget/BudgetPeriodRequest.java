package com.smartenvelope.dto.budget;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BudgetPeriodRequest {

    @NotNull(message = "Vui lòng nhập tháng.")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12.")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12.")
    private Integer month;

    @NotNull(message = "Vui lòng nhập năm.")
    @Min(value = 1900, message = "Năm phải từ 1900 đến 9999.")
    @Max(value = 9999, message = "Năm phải từ 1900 đến 9999.")
    private Integer year;

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}