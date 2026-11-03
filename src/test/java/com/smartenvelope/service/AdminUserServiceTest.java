package com.smartenvelope.service;

import com.smartenvelope.dto.admin.AdminUserResponse;
import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.UserFinanceSettingsRepository;
import com.smartenvelope.repository.UserRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserFinanceSettingsRepository userFinanceSettingsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, workspaceRepository, membershipRepository,
                userFinanceSettingsRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Admin tìm kiếm người dùng phân trang thành công")
    void testSearchUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User("user1", "pass", "Nguyen Van A", "a@example.com", Role.USER);
        user.setId(1L);
        Page<User> mockPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findBySearchFilter(eq("Nguyen"), eq(Role.USER), eq(UserStatus.ACTIVE), eq(pageable)))
                .thenReturn(mockPage);

        Page<User> result = userService.searchUsers("Nguyen", Role.USER, UserStatus.ACTIVE, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Nguyen Van A", result.getContent().get(0).getFullName());
    }

    @Test
    @DisplayName("Lấy chi tiết người dùng an toàn")
    void testGetUserDetail_success() {
        User user = new User("user1", "pass", "Nguyen Van A", "a@example.com", Role.USER);
        user.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(10L, MembershipStatus.ACCEPTED))
                .thenReturn(List.of(mock(Membership.class)));

        AdminUserResponse response = userService.getUserDetail(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Nguyen Van A", response.getFullName());
        assertEquals(1, response.getWorkspaceCount());
    }

    @Test
    @DisplayName("Khóa tài khoản người dùng thành công")
    void testLockUser_success() {
        User targetUser = new User("user1", "pass", "Nguyen Van A", "a@example.com", Role.USER);
        targetUser.setId(2L);
        targetUser.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        userService.lockUser(2L, 1L);

        assertEquals(UserStatus.LOCKED, targetUser.getStatus());
        verify(userRepository, times(1)).save(targetUser);
    }

    @Test
    @DisplayName("Khóa tài khoản: Admin không thể tự khóa chính mình")
    void testLockUser_cannotLockSelf() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.lockUser(1L, 1L));

        assertTrue(ex.getMessage().contains("không thể tự khóa tài khoản của chính mình"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Khóa tài khoản: Không được khóa Quản trị viên (ADMIN) cuối cùng còn hoạt động")
    void testLockUser_cannotLockLastActiveAdmin() {
        User adminUser = new User("admin1", "pass", "Super Admin", "admin@example.com", Role.ADMIN);
        adminUser.setId(2L);
        adminUser.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndStatus(Role.ADMIN, UserStatus.ACTIVE)).thenReturn(1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                userService.lockUser(2L, 1L));

        assertTrue(ex.getMessage().contains("cuối cùng đang hoạt động"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Mở khóa tài khoản người dùng thành công")
    void testUnlockUser_success() {
        User targetUser = new User("user1", "pass", "Nguyen Van A", "a@example.com", Role.USER);
        targetUser.setId(2L);
        targetUser.setStatus(UserStatus.LOCKED);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        userService.unlockUser(2L, 1L);

        assertEquals(UserStatus.ACTIVE, targetUser.getStatus());
        verify(userRepository, times(1)).save(targetUser);
    }
}

