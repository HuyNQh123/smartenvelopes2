package com.smartenvelope.controller.auth;

import com.smartenvelope.dto.auth.RegisterRequest;
import com.smartenvelope.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("STT 1: GET /login hiển thị trang đăng nhập")
    void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("STT 2: GET /register hiển thị trang đăng ký với model registerRequest")
    void testShowRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("STT 3: POST /register hợp lệ -> Chuyển hướng tới /login?registered=true")
    void testRegisterValid() throws Exception {
        when(userService.existsByEmail("vana@example.com")).thenReturn(false);
        when(userService.existsByUsername("nguyenvana")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn A")
                        .param("username", "nguyenvana")
                        .param("email", "vana@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("STT 4: POST /register với email đã tồn tại -> Báo lỗi email và giữ lại trang đăng ký")
    void testRegisterDuplicateEmail() throws Exception {
        when(userService.existsByEmail("vana@example.com")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn A")
                        .param("email", "vana@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("STT 5: POST /register xác nhận mật khẩu không khớp -> Báo lỗi confirmPassword")
    void testRegisterMismatchPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Nguyễn Văn A")
                        .param("email", "vana@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "differentPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "confirmPassword"));

        verify(userService, never()).register(any());
    }
}
