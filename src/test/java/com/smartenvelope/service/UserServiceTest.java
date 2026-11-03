package com.smartenvelope.service;

import com.smartenvelope.dto.auth.RegisterRequest;
import com.smartenvelope.entity.Membership;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.UserFinanceSettings;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.MembershipStatus;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.repository.MembershipRepository;
import com.smartenvelope.repository.UserFinanceSettingsRepository;
import com.smartenvelope.repository.UserRepository;
import com.smartenvelope.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "Nguyễn Văn A",
                "nguyenvana",
                "vana@example.com",
                "password123",
                "password123"
        );
    }

    @Test
    @DisplayName("Đăng ký thành công: Lưu User với mật khẩu BCrypt, tạo Workspace cá nhân, Membership OWNER và Cài đặt tài chính")
    void testRegisterSuccess() {
        // Given
        when(userRepository.existsByEmail("vana@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("nguyenvana")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedBCryptPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName(validRequest.getFullName());
        savedUser.setUsername(validRequest.getUsername());
        savedUser.setEmail(validRequest.getEmail());
        savedUser.setPassword("$2a$10$hashedBCryptPassword");
        savedUser.setRole(Role.USER);
        savedUser.setStatus(UserStatus.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Workspace savedWorkspace = new Workspace("Quỹ cá nhân của Nguyễn Văn A", WorkspaceType.PERSONAL, "Mô tả", savedUser);
        savedWorkspace.setId(10L);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(savedWorkspace);

        // When
        User result = userService.register(validRequest);

        // Then
        assertNotNull(result);
        assertEquals("Nguyễn Văn A", result.getFullName());
        assertEquals(Role.USER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        // Kiểm tra mật khẩu được mã hóa BCrypt
        verify(passwordEncoder).encode("password123");

        // Kiểm tra tự động tạo Workspace cá nhân
        ArgumentCaptor<Workspace> wsCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceRepository).save(wsCaptor.capture());
        assertEquals(WorkspaceType.PERSONAL, wsCaptor.getValue().getType());
        assertTrue(wsCaptor.getValue().getName().contains("Nguyễn Văn A"));

        // Kiểm tra tự động tạo Membership với vai trò OWNER
        ArgumentCaptor<Membership> memCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(memCaptor.capture());
        assertEquals(MemberRole.OWNER, memCaptor.getValue().getRole());
        assertEquals(MembershipStatus.ACCEPTED, memCaptor.getValue().getStatus());

        // Kiểm tra tự động tạo UserFinanceSettings
        ArgumentCaptor<UserFinanceSettings> settingCaptor = ArgumentCaptor.forClass(UserFinanceSettings.class);
        verify(userFinanceSettingsRepository).save(settingCaptor.capture());
        assertEquals("50/30/20", settingCaptor.getValue().getDefaultRule());
        assertEquals(5, settingCaptor.getValue().getSalaryDay());
    }

    @Test
    @DisplayName("Đăng ký thất bại khi xác nhận mật khẩu không khớp")
    void testRegisterMismatchPassword() {
        validRequest.setConfirmPassword("differentPassword");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.register(validRequest));

        assertEquals("Mật khẩu xác nhận không khớp", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký thất bại khi email đã tồn tại trong hệ thống")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("vana@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.register(validRequest));

        assertEquals("Địa chỉ email đã tồn tại trong hệ thống", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký thất bại khi tên đăng nhập đã tồn tại trong hệ thống")
    void testRegisterDuplicateUsername() {
        when(userRepository.existsByEmail("vana@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("nguyenvana")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.register(validRequest));

        assertEquals("Tên đăng nhập đã tồn tại trong hệ thống", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}

