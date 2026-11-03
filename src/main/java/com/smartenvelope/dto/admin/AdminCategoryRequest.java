package com.smartenvelope.dto.admin;

import com.smartenvelope.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO tiếp nhận dữ liệu tạo mới hoặc cập nhật danh mục hệ thống từ Quản trị viên (Admin).
 * Phụ trách bởi: Thành viên 1.
 */
public class AdminCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String name;

    @NotNull(message = "Loại danh mục (Thu nhập hoặc Chi tiêu) là bắt buộc")
    private CategoryType type;

    @Size(max = 50, message = "Mã biểu tượng icon tối đa 50 ký tự")
    private String icon = "bi-tag";

    @Size(max = 20, message = "Mã màu tối đa 20 ký tự")
    private String color = "#0d6efd";

    private Boolean isActive = true;

    public AdminCategoryRequest() {
    }

    public AdminCategoryRequest(String name, CategoryType type, String icon, String color, Boolean isActive) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isActive = isActive != null ? isActive : true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

