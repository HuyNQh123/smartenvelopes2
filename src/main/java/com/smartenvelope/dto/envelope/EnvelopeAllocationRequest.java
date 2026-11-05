package com.smartenvelope.dto.envelope;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public class EnvelopeAllocationRequest {

    @NotNull(message = "Vui lòng nhập số tiền.")
    @DecimalMin(value = "0", message = "Số tiền không được âm.")
    @Digits(
            integer = 19,
            fraction = 0,
            message = "Nhập số tiền nguyên VND, tối đa 19 chữ số.")
    private BigDecimal amount;

    @NotNull(message = "Thiếu phiên bản kế hoạch. Hãy tải lại trang.")
    private Integer periodVersion;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getPeriodVersion() {
        return periodVersion;
    }

    public void setPeriodVersion(Integer periodVersion) {
        this.periodVersion = periodVersion;
    }
}