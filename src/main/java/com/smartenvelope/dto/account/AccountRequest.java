package com.smartenvelope.dto.account;

import java.math.BigDecimal;

import com.smartenvelope.enums.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AccountRequest {

    @NotBlank(message = "Vui lòng nhập tên tài khoản.")
    @Size(max = 100, message = "Tên không được quá 100 ký tự.")
    private String name;

    @NotNull(message = "Vui lòng chọn loại tài khoản.")
    private AccountType type;

    @NotNull(message = "Vui lòng nhập số dư ban đầu.")
    @DecimalMin(value = "0", message = "Số dư không được âm.")
    @Digits(integer = 19, fraction = 0,
            message = "Số dư phải là số nguyên, tối đa 19 chữ số.")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}