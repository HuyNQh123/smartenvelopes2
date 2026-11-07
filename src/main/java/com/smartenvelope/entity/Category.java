package com.smartenvelope.entity;

import com.smartenvelope.enums.CategoryType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho danh mục thu chi (Ăn uống, Nhà ở, Mua sắm, v.v.).
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CategoryType type = CategoryType.EXPENSE;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "is_default")
    private boolean isDefault = true;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Category() {
    }

    public Category(String name, CategoryType type) {
        this.name = name;
        this.type = type;
        this.isDefault = true;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Category(String name, CategoryType type, String icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.isDefault = true;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Category(Long id, String name, CategoryType type, String icon, boolean isDefault, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.isDefault = isDefault;
        this.active = active;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
