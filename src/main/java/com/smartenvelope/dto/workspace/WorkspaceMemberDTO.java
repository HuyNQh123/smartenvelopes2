package com.smartenvelope.dto.workspace;

import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;

import java.time.LocalDateTime;

/**
 * DTO biểu diễn thông tin thành viên trong Quỹ để hiển thị danh sách và phân quyền.
 * Phụ trách bởi: Thành viên 1.
 */
public class WorkspaceMemberDTO {

    private Long membershipId;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String avatarUrl;
    private MemberRole role;
    private MembershipStatus status;
    private LocalDateTime joinedAt;
    private boolean isOwner;
    private boolean canBeManaged;
    private boolean canBeRemoved;

    public WorkspaceMemberDTO() {
    }

    public WorkspaceMemberDTO(Long membershipId, Long userId, String username, String fullName,
                              String email, String avatarUrl, MemberRole role,
                              MembershipStatus status, LocalDateTime joinedAt,
                              boolean isOwner, boolean canBeManaged, boolean canBeRemoved) {
        this.membershipId = membershipId;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
        this.isOwner = isOwner;
        this.canBeManaged = canBeManaged;
        this.canBeRemoved = canBeRemoved;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public boolean isCanBeManaged() {
        return canBeManaged;
    }

    public void setCanBeManaged(boolean canBeManaged) {
        this.canBeManaged = canBeManaged;
    }

    public boolean isCanBeRemoved() {
        return canBeRemoved;
    }

    public void setCanBeRemoved(boolean canBeRemoved) {
        this.canBeRemoved = canBeRemoved;
    }
}

