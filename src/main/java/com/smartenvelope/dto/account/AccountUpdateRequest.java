package com.smartenvelope.dto.account;

import com.smartenvelope.enums.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AccountUpdateRequest {

    @NotBlank(message = "Vui lòng nhập tên tài khoản.")
    @Size(max = 100, message = "Tên không được quá 100 ký tự.")
    private String name;

    @NotNull(message = "Vui lòng chọn loại tài khoản.")
    private AccountType type;

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
}