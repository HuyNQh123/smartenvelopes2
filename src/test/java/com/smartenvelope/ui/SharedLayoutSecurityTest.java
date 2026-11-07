package com.smartenvelope.ui;

import com.smartenvelope.config.SecurityConfig;
import com.smartenvelope.controller.dashboard.DashboardController;
import com.smartenvelope.controller.workspace.WorkspaceContextController;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.CustomUserDetailsService;
import com.smartenvelope.service.WorkspaceContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kiểm tra layout với security filter và dialect thật, không truy cập database. */
@WebMvcTest(controllers = {DashboardController.class, WorkspaceContextController.class})
@Import(SecurityConfig.class)
class SharedLayoutSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean CurrentUserService currentUserService;
    @MockitoBean WorkspaceContextService workspaceContextService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void prepareModel() {
        User user = new User("kiemthu", "", "Người kiểm thử", "kiemthu@example.test", Role.USER);
        user.setId(1L);
        Workspace workspace = new Workspace("Quỹ kiểm thử", WorkspaceType.PERSONAL, "", user);
        workspace.setId(101L);
        when(currentUserService.isAuthenticated()).thenReturn(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(workspaceContextService.getCurrentWorkspace()).thenReturn(workspace);
        when(workspaceContextService.getCurrentWorkspaceId()).thenReturn(101L);
        when(workspaceContextService.getAvailableWorkspaces()).thenReturn(List.of(workspace));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER không thấy menu quản trị; form chuyển quỹ và đăng xuất giữ CSRF")
    void userNavigationAndCsrf() throws Exception {
        String html = mockMvc.perform(get("/dashboard")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).doesNotContain("href=\"/admin/users\"", "href=\"/admin/categories\"");
        assertThat(html).contains("action=\"/workspace/switch\"", "action=\"/logout\"", "aria-current=\"page\"");
        var forms = java.util.regex.Pattern.compile("<form[^>]*action=\"/(workspace/switch|logout)\"[^>]*>(.*?)</form>", java.util.regex.Pattern.DOTALL).matcher(html);
        int count = 0;
        while (forms.find()) {
            assertThat(forms.group()).contains("method=\"post\"", "name=\"_csrf\"");
            count++;
        }
        assertThat(count).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN thấy đúng các liên kết quản trị đang tồn tại")
    void adminNavigation() throws Exception {
        String html = mockMvc.perform(get("/dashboard")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("href=\"/admin/users\"", "href=\"/admin/categories\"");
    }

    @Test
    @WithMockUser
    @DisplayName("Chuyển quỹ có CSRF vẫn gửi ID tới cơ chế server hiện có")
    void switchWorkspaceUsesExistingHandler() throws Exception {
        mockMvc.perform(post("/workspace/switch").param("workspaceId", "101").with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/dashboard"));
        verify(workspaceContextService).setCurrentWorkspaceId(101L);
    }

    @Test
    @WithMockUser
    @DisplayName("Chuyển quỹ thiếu CSRF bị chặn trước khi gọi service")
    void switchWorkspaceWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/workspace/switch").param("workspaceId", "101"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(workspaceContextService);
    }

    @Test
    @WithMockUser
    @DisplayName("Đăng xuất POST có CSRF giữ chuyển hướng an toàn về trang đăng nhập")
    void logoutUsesExistingSecurityHandler() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @WithMockUser
    @DisplayName("Thiếu quỹ hoặc danh sách quỹ rỗng vẫn render lựa chọn và liên kết quản lý")
    void emptyWorkspaceState() throws Exception {
        when(workspaceContextService.getCurrentWorkspace()).thenReturn(null);
        when(workspaceContextService.getAvailableWorkspaces()).thenReturn(List.of());
        String html = mockMvc.perform(get("/dashboard")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(html).contains("Chọn quỹ làm việc", "Chưa có quỹ khả dụng.");
    }
}
