package com.smartenvelope.entity;

import com.smartenvelope.enums.CategoryType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho danh mục thu chi trong hệ thống.
 * Hỗ trợ danh mục mặc định toàn hệ thống (is_system = true, user = null)
 * và danh mục riêng do từng người dùng tạo (is_system = false, user != null).
 * Phụ trách bởi: Thành viên 2 (Account & Category Management).
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
    private CategoryType type;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Category() {
    }

    public Category(String name, CategoryType type, String icon, String color, User user, Boolean isSystem) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.user = user;
        this.isSystem = isSystem;
        this.isActive = true;
    }

    public Category(String name, CategoryType type, String icon, String color, User user, Boolean isSystem, Boolean isActive) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.user = user;
        this.isSystem = isSystem;
        this.isActive = isActive != null ? isActive : true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isSystem == null) {
            this.isSystem = false;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
