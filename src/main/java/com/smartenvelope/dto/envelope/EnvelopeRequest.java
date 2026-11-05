package com.smartenvelope.dto.envelope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EnvelopeRequest {

    @NotNull(message = "Vui lòng chọn danh mục.")
    private Long categoryId;

    @NotBlank(message = "Vui lòng nhập tên phong bì.")
    @Size(max = 100, message = "Tên phong bì tối đa 100 ký tự.")
    private String name;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}