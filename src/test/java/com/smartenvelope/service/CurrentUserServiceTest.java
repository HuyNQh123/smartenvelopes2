package com.smartenvelope.service;

import com.smartenvelope.entity.User;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserService(userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("STT 1: Người chưa đăng nhập gọi getCurrentUser() ném AccessDeniedException")
    void testGetCurrentUser_whenNotAuthenticated_shouldThrowAccessDeniedException() {
        assertThrows(AccessDeniedException.class, () -> currentUserService.getCurrentUser());
        assertFalse(currentUserService.isAuthenticated());
        assertTrue(currentUserService.findCurrentUser().isEmpty());
    }

    @Test
    @DisplayName("STT 1b: Anonymous token gọi getCurrentUser() ném AccessDeniedException")
    void testGetCurrentUser_whenAnonymous_shouldThrowAccessDeniedException() {
        AnonymousAuthenticationToken anonToken = new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonToken);

        assertThrows(AccessDeniedException.class, () -> currentUserService.getCurrentUser());
        assertFalse(currentUserService.isAuthenticated());
    }

    @Test
    @DisplayName("STT 2: Người đã đăng nhập với CustomUserDetails trả về đúng User")
    void testGetCurrentUser_whenCustomUserDetails_shouldReturnUser() {
        User user = new User("nguyenvana", "hashedPass", "Nguyễn Văn A", "vana@example.com", Role.USER);
        user.setId(10L);
        user.setStatus(UserStatus.ACTIVE);

        CustomUserDetailsService.CustomUserDetails principal = new CustomUserDetailsService.CustomUserDetails(
                user, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, "credentials", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        User result = currentUserService.getCurrentUser();
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("vana@example.com", result.getEmail());
        assertTrue(currentUserService.isAuthenticated());
    }

    @Test
    @DisplayName("STT 2b: Lấy ID và Email trực tiếp từ CustomUserDetails mà không query lặp lại")
    void testGetCurrentUserIdAndEmail_fromPrincipal() {
        User user = new User("nguyenvana", "hashedPass", "Nguyễn Văn A", "vana@example.com", Role.USER);
        user.setId(10L);

        CustomUserDetailsService.CustomUserDetails principal = new CustomUserDetailsService.CustomUserDetails(
                user, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, "credentials", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(10L, currentUserService.getCurrentUserId());
        assertEquals("vana@example.com", currentUserService.getCurrentUserEmail());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("STT 2c: Fallback tìm kiếm bằng Email khi Principal là String")
    void testGetCurrentUser_whenPrincipalIsString() {
        User user = new User("nguyenvana", "hashedPass", "Nguyễn Văn A", "vana@example.com", Role.USER);
        user.setId(20L);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "vana@example.com", "credentials", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("vana@example.com")).thenReturn(Optional.of(user));

        User result = currentUserService.getCurrentUser();
        assertNotNull(result);
        assertEquals(20L, result.getId());
    }

    @Test
    @DisplayName("Ném ResourceNotFoundException nếu không tìm thấy User trong Database")
    void testGetCurrentUser_whenUserNotFoundInDb_shouldThrowResourceNotFoundException() {
        User user = new User("nguyenvana", "hashedPass", "Nguyễn Văn A", "vana@example.com", Role.USER);
        user.setId(999L);

        CustomUserDetailsService.CustomUserDetails principal = new CustomUserDetailsService.CustomUserDetails(
                user, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, "credentials", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> currentUserService.getCurrentUser());
    }
}

