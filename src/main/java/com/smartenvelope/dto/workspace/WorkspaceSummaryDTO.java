package com.smartenvelope.dto.workspace;

import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.WorkspaceType;

import java.time.LocalDateTime;

/**
 * DTO tóm tắt thông tin Workspace cho danh sách hiển thị trên giao diện.
 * Phụ trách bởi: Thành viên 1.
 */
public class WorkspaceSummaryDTO {

    private Long id;
    private String name;
    private WorkspaceType type;
    private String description;
    private String ownerName;
    private Long ownerId;
    private long memberCount;
    private MemberRole currentUserRole;
    private boolean isCurrent;
    private LocalDateTime createdAt;

    public WorkspaceSummaryDTO() {
    }

    public WorkspaceSummaryDTO(Long id, String name, WorkspaceType type, String description,
                               String ownerName, Long ownerId, long memberCount,
                               MemberRole currentUserRole, boolean isCurrent, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.memberCount = memberCount;
        this.currentUserRole = currentUserRole;
        this.isCurrent = isCurrent;
        this.createdAt = createdAt;
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

    public WorkspaceType getType() {
        return type;
    }

    public void setType(WorkspaceType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public MemberRole getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(MemberRole currentUserRole) {
        this.currentUserRole = currentUserRole;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

