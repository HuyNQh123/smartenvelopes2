package com.smartenvelope.controller.admin;

import com.smartenvelope.dto.admin.AdminUserResponse;
import com.smartenvelope.entity.User;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        AdminUserController controller = new AdminUserController(userService, currentUserService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /admin/users hiển thị danh sách người dùng phân trang")
    void testListUsers() throws Exception {
        User user = new User("vana", "pass", "Nguyen Van A", "vana@example.com", Role.USER);
        user.setId(1L);
        Page<User> page = new PageImpl<>(List.of(user));

        when(userService.searchUsers(any(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(currentUserService.getCurrentUserId()).thenReturn(99L);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("usersPage", "roles", "statuses", "currentAdminId"));
    }

    @Test
    @DisplayName("GET /admin/users/{id} xem chi tiết người dùng")
    void testViewUserDetail() throws Exception {
        User user = new User("vana", "pass", "Nguyen Van A", "vana@example.com", Role.USER);
        user.setId(1L);
        AdminUserResponse response = new AdminUserResponse(user, 2);

        when(userService.getUserDetail(1L)).thenReturn(response);
        when(currentUserService.getCurrentUserId()).thenReturn(99L);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-detail"))
                .andExpect(model().attributeExists("userDetail", "currentAdminId"));
    }

    @Test
    @DisplayName("POST /admin/users/{id}/lock khóa tài khoản thành công")
    void testLockUser_success() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(99L);
        doNothing().when(userService).lockUser(2L, 99L);

        mockMvc.perform(post("/admin/users/2/lock"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).lockUser(2L, 99L);
    }

    @Test
    @DisplayName("POST /admin/users/{id}/lock thất bại khi tự khóa chính mình")
    void testLockUser_cannotLockSelf() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(99L);
        doThrow(new IllegalArgumentException("Bạn không thể tự khóa tài khoản của chính mình!"))
                .when(userService).lockUser(99L, 99L);

        mockMvc.perform(post("/admin/users/99/lock"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /admin/users/{id}/unlock mở khóa tài khoản thành công")
    void testUnlockUser_success() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(99L);
        doNothing().when(userService).unlockUser(2L, 99L);

        mockMvc.perform(post("/admin/users/2/unlock"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).unlockUser(2L, 99L);
    }
}

