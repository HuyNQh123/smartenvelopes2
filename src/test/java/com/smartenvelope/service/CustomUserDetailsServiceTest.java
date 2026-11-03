package com.smartenvelope.service;

import com.smartenvelope.entity.User;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setFullName("Nguyễn Văn A");
        activeUser.setUsername("nguyenvana");
        activeUser.setEmail("vana@example.com");
        activeUser.setPassword("$2a$10$hashedPassword");
        activeUser.setRole(Role.USER);
        activeUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Đăng nhập thành công bằng email: Trả về UserDetails có ROLE_USER")
    void testLoadUserByUsernameWithEmailSuccess() {
        when(userRepository.findByEmail("vana@example.com")).thenReturn(Optional.of(activeUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("vana@example.com");

        assertNotNull(userDetails);
        assertEquals("nguyenvana", userDetails.getUsername());
        assertEquals("$2a$10$hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Đăng nhập thành công bằng username: Trả về UserDetails")
    void testLoadUserByUsernameWithUsernameSuccess() {
        when(userRepository.findByEmail("nguyenvana")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("nguyenvana")).thenReturn(Optional.of(activeUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("nguyenvana");

        assertNotNull(userDetails);
        assertEquals("nguyenvana", userDetails.getUsername());
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi tài khoản không tồn tại trong hệ thống")
    void testLoadUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknown@example.com"));
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi tài khoản bị KHÓA (LOCKED)")
    void testLoadUserLocked() {
        activeUser.setStatus(UserStatus.LOCKED);
        when(userRepository.findByEmail("vana@example.com")).thenReturn(Optional.of(activeUser));

        assertThrows(LockedException.class, () ->
                userDetailsService.loadUserByUsername("vana@example.com"));
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi tài khoản CHƯA KÍCH HOẠT (INACTIVE)")
    void testLoadUserInactive() {
        activeUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmail("vana@example.com")).thenReturn(Optional.of(activeUser));

        assertThrows(DisabledException.class, () ->
                userDetailsService.loadUserByUsername("vana@example.com"));
    }
}

