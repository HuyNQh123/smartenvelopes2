package com.smartenvelope.ui;

import com.smartenvelope.config.SecurityConfig;
import com.smartenvelope.controller.auth.AuthController;
import com.smartenvelope.controller.admin.*;
import com.smartenvelope.controller.workspace.*;
import com.smartenvelope.dto.auth.RegisterRequest;
import com.smartenvelope.dto.workspace.*;
import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Kiểm tra POST, validation và quyền hiển thị bằng controller/security thật, service giả, không ghi DB. */
@WebMvcTest(controllers = {AuthController.class, AdminController.class, AdminUserController.class,
        AdminCategoryController.class, WorkspaceController.class, WorkspaceInvitationController.class})
@Import(SecurityConfig.class)
class Member1FormsUiTest {
    @Autowired MockMvc mockMvc;
    @Autowired PasswordEncoder passwordEncoder;
    @MockBean CurrentUserService currentUserService;
    @MockBean WorkspaceContextService workspaceContextService;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean UserService userService;
    @MockBean CategoryService categoryService;
    @MockBean WorkspaceService workspaceService;
    @MockBean WorkspaceInvitationService invitationService;
    User user;
    Workspace workspace;
    WorkspaceInvitation invitation;

    @BeforeEach
    void prepareFixtures() {
        user = new User("kiemthu", "", "Người kiểm thử", "kiemthu@example.test", Role.USER);
        user.setId(1L);
        workspace = new Workspace("Quỹ kiểm thử", WorkspaceType.SHARED, "Mô tả quỹ", user);
        workspace.setId(101L);
        invitation = new WorkspaceInvitation(workspace, user.getEmail(), user, MemberRole.VIEWER,
                "ma-loi-moi-chi-dung-trong-test", LocalDateTime.of(2026, 11, 30, 12, 0));
        invitation.setId(10L);
        when(currentUserService.isAuthenticated()).thenReturn(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(workspaceContextService.getCurrentWorkspace()).thenReturn(workspace);
        when(workspaceContextService.getAvailableWorkspaces()).thenReturn(List.of(workspace));
        when(workspaceService.getWorkspaceByIdAndCheckAccess(101L, 1L)).thenReturn(workspace);
        when(workspaceService.getUserRoleInWorkspace(101L, 1L)).thenReturn(MemberRole.OWNER);
    }

    @Test
    @DisplayName("Đăng nhập hợp lệ qua SecurityFilterChain giữ chuyển hướng tổng quan")
    void validLogin() throws Exception {
        when(customUserDetailsService.loadUserByUsername("kiemthu")).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername("kiemthu")
                        .password(passwordEncoder.encode("matkhau-kiemthu")).roles("USER").build());
        mockMvc.perform(post("/login").with(csrf()).param("email", "kiemthu").param("password", "matkhau-kiemthu"))
                .andExpect(authenticated()).andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Mật khẩu sai giữ thông báo lỗi đăng nhập và không xác thực")
    void invalidLogin() throws Exception {
        when(customUserDetailsService.loadUserByUsername("kiemthu")).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername("kiemthu")
                        .password(passwordEncoder.encode("matkhau-kiemthu")).roles("USER").build());
        mockMvc.perform(post("/login").with(csrf()).param("email", "kiemthu").param("password", "sai-mat-khau"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("Đăng ký trống render lỗi tại trường, không gọi tạo tài khoản")
    void invalidRegistration() throws Exception {
        String html = mockMvc.perform(post("/register").with(csrf()).param("fullName", "")
                        .param("email", "sai-email").param("password", "a").param("confirmPassword", "a"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registerRequest", "fullName", "email", "password"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("id=\"fullName-error\"", "id=\"email-error\"", "aria-invalid=\"true\"");
        writePreview("auth-register-invalid", html);
        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("Xác nhận mật khẩu khác nhau vẫn bị validation server từ chối")
    void passwordMismatch() throws Exception {
        mockMvc.perform(post("/register").with(csrf()).param("fullName", "Người kiểm thử")
                        .param("email", "kiemthu@example.test").param("password", "matkhau-kiemthu").param("confirmPassword", "khong-trung"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "confirmPassword"));
        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("Email trùng vẫn hiển thị lỗi server của trường email")
    void duplicateEmail() throws Exception {
        when(userService.existsByEmail("kiemthu@example.test")).thenReturn(true);
        mockMvc.perform(post("/register").with(csrf()).param("fullName", "Người kiểm thử")
                        .param("email", "kiemthu@example.test").param("password", "matkhau-kiemthu").param("confirmPassword", "matkhau-kiemthu"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));
    }

    @Test
    @DisplayName("Đăng ký hợp lệ giữ POST và chuyển hướng về đăng nhập")
    void validRegistration() throws Exception {
        mockMvc.perform(post("/register").with(csrf()).param("fullName", "Người kiểm thử")
                        .param("email", "kiemthu@example.test").param("password", "matkhau-kiemthu").param("confirmPassword", "matkhau-kiemthu"))
                .andExpect(redirectedUrl("/login?registered=true"));
        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Tên quỹ rỗng và mô tả quá dài render cả hai lỗi validation")
    void invalidWorkspace() throws Exception {
        String html = mockMvc.perform(post("/workspaces/create").with(csrf()).param("name", "").param("description", "x".repeat(256)))
                .andExpect(model().attributeHasFieldErrors("workspaceForm", "name", "description"))
                .andExpect(view().name("workspaces/create")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        writePreview("workspaces-create-invalid", html);
        verify(workspaceService, never()).createSharedWorkspace(any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("Tạo quỹ hợp lệ giữ chuyển hướng tới quỹ vừa tạo")
    void validWorkspace() throws Exception {
        when(workspaceService.createSharedWorkspace(any(), eq(user))).thenReturn(workspace);
        mockMvc.perform(post("/workspaces/create").with(csrf()).param("name", "Quỹ kiểm thử"))
                .andExpect(redirectedUrl("/workspaces/101"));
    }

    @ParameterizedTest
    @EnumSource(MemberRole.class)
    @WithMockUser
    @DisplayName("Vai trò thành viên quyết định đúng form mời, chuyển quyền và xóa quỹ")
    void membershipRoleVisibility(MemberRole role) throws Exception {
        when(workspaceService.getUserRoleInWorkspace(101L, 1L)).thenReturn(role);
        when(workspaceService.getWorkspaceMembers(101L, 1L)).thenReturn(List.of(
                new WorkspaceMemberDTO(2L, 2L, "thanhvien", "Thành viên", "thanhvien@example.test", null,
                        MemberRole.WRITER, MembershipStatus.ACCEPTED, LocalDateTime.now(), false, true, true)));
        String html = mockMvc.perform(get("/workspaces/101")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        writePreview("workspaces-detail-" + role.name(), html);
        boolean manager = role == MemberRole.OWNER || role == MemberRole.MANAGER;
        assertThat(html.contains("action=\"/workspaces/101/invitations\"")).isEqualTo(manager);
        assertThat(html.contains("action=\"/workspaces/101/delete\"")).isEqualTo(role == MemberRole.OWNER);
        assertThat(html.contains("value=\"OWNER\"")).isEqualTo(role == MemberRole.OWNER);
        assertThat(html.contains("value=\"MANAGER\"")).isEqualTo(role == MemberRole.OWNER);
    }

    @Test
    @WithMockUser
    @DisplayName("Quỹ cá nhân không có form rời hoặc xóa")
    void personalWorkspaceProtection() throws Exception {
        workspace.setType(WorkspaceType.PERSONAL);
        String html = mockMvc.perform(get("/workspaces/101")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).doesNotContain("action=\"/workspaces/101/delete\"", "action=\"/workspaces/101/leave\"");
    }

    @Test
    @WithMockUser
    @DisplayName("Email mời sai không gọi service gửi lời mời")
    void invalidInvitationEmail() throws Exception {
        mockMvc.perform(post("/workspaces/101/invitations").with(csrf()).param("email", "sai-email").param("role", "VIEWER"))
                .andExpect(redirectedUrl("/workspaces/101")).andExpect(flash().attributeExists("errorMessage"));
        verify(invitationService, never()).inviteMember(anyLong(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("Lời mời hợp lệ giữ handler, CSRF và thông báo kết quả")
    void validInvitation() throws Exception {
        when(invitationService.inviteMember(eq(101L), any(), eq(user))).thenReturn(invitation);
        mockMvc.perform(post("/workspaces/101/invitations").with(csrf()).param("email", "thanhvien@example.test").param("role", "WRITER"))
                .andExpect(redirectedUrl("/workspaces/101")).andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser
    @DisplayName("Email không khớp lời mời không render form chấp nhận token")
    void invitationEmailMismatch() throws Exception {
        invitation.setEmail("nguoi-khac@example.test");
        when(invitationService.getInvitationByToken("ma-kiemthu")).thenReturn(invitation);
        String html = mockMvc.perform(get("/invitations/join").param("token", "ma-kiemthu"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        writePreview("workspaces-join-mismatch", html);
        assertThat(html).contains("không khớp lời mời").doesNotContain("action=\"/invitations/join/accept\"", "ma-loi-moi-chi-dung-trong-test");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Điểm vào quản trị giữ chuyển hướng tới danh sách người dùng")
    void adminEntry() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Bộ lọc và phân trang người dùng vẫn gửi đúng giá trị controller")
    void userFilters() throws Exception {
        when(userService.searchUsers(any(), any(), any(), any())).thenReturn(Page.empty());
        mockMvc.perform(get("/admin/users").param("keyword", "kiemthu").param("role", "USER").param("status", "LOCKED").param("page", "1"))
                .andExpect(status().isOk());
        verify(userService).searchUsers(eq("kiemthu"), eq(Role.USER), eq(UserStatus.LOCKED), argThat(page -> page.getPageNumber() == 1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Khóa và mở khóa vẫn gọi handler hiện có với ID quản trị viên")
    void userLockAndUnlock() throws Exception {
        mockMvc.perform(post("/admin/users/2/lock").with(csrf())).andExpect(redirectedUrl("/admin/users"));
        mockMvc.perform(post("/admin/users/2/unlock").with(csrf())).andExpect(redirectedUrl("/admin/users"));
        verify(userService).lockUser(2L, 1L);
        verify(userService).unlockUser(2L, 1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Validation danh mục giữ lỗi name/type trong fragment trường dùng chung")
    void categoryValidation() throws Exception {
        String html = mockMvc.perform(post("/admin/categories/create").with(csrf()).param("name", ""))
                .andExpect(model().attributeHasFieldErrors("categoryRequest", "name", "type"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        writePreview("admin-category-create-invalid", html);
        assertThat(html).contains("id=\"name-error\"", "id=\"type-error\"");
        verify(categoryService, never()).createSystemCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Danh mục hợp lệ giữ POST tạo mới và điều hướng danh sách")
    void validCategory() throws Exception {
        mockMvc.perform(post("/admin/categories/create").with(csrf()).param("name", "Sinh hoạt").param("type", "EXPENSE")
                        .param("icon", "bi-tag").param("color", "#243d35").param("isActive", "true"))
                .andExpect(redirectedUrl("/admin/categories"));
        verify(categoryService).createSystemCategory(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"bi-cup-hot", ""})
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Biểu tượng tùy chỉnh hoặc để trống vẫn được giữ trong form chỉnh sửa")
    void customCategoryIconRemainsEditable(String icon) throws Exception {
        Category category = new Category("Danh mục", CategoryType.EXPENSE, icon, "#243d35", null, true);
        category.setId(1L);
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        String html = mockMvc.perform(get("/admin/categories/1/edit")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var field = java.util.regex.Pattern.compile("<input[^>]*name=\"icon\"[^>]*>").matcher(html);
        assertThat(field.find()).isTrue();
        assertThat(field.group()).contains("type=\"text\"", "value=\"" + icon + "\"");
    }
    private static void writePreview(String name, String html) throws Exception {
        String directory = System.getProperty("ui.preview.dir");
        if (directory == null) return;
        var path = java.nio.file.Path.of(directory);
        java.nio.file.Files.createDirectories(path);
        java.nio.file.Files.writeString(path.resolve(name + ".html"), html, StandardCharsets.UTF_8);
    }
}
