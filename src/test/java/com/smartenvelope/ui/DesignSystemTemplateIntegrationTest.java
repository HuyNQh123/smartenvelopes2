package com.smartenvelope.ui;

import com.smartenvelope.dto.admin.AdminCategoryRequest;
import com.smartenvelope.dto.admin.AdminUserResponse;
import com.smartenvelope.dto.auth.RegisterRequest;
import com.smartenvelope.dto.workspace.WorkspaceCreateRequest;
import com.smartenvelope.dto.workspace.WorkspaceUpdateRequest;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.entity.WorkspaceInvitation;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.enums.WorkspaceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Render template thật bằng Spring MVC/Thymeleaf với dữ liệu chỉ nằm trong bộ nhớ.
 * Không khởi động ứng dụng, kết nối DB hoặc thêm endpoint vào mã production.
 * Chỉ xác minh hợp đồng tài nguyên/layout; quyền truy cập và nghiệp vụ thuộc suite hiện có.
 */
class DesignSystemTemplateIntegrationTest {
    private static final List<String> PAGES = List.of(
            "account/edit", "account/form", "account/list", "admin/categories", "admin/category-create",
            "admin/category-edit", "admin/user-detail", "admin/users", "alert/list",
            "auth/login", "auth/register", "budget/form", "budget/list", "dashboard/index",
            "envelope/form", "envelope/list", "error/403", "error/404", "error/500", "report/index",
            "savings/form", "savings/list", "transaction/form", "transaction/list",
            "workspaces/create", "workspaces/detail", "workspaces/index",
            "workspaces/invitations", "workspaces/join");
    private static MockMvc mockMvc;

    @BeforeAll
    static void configureRenderer() {
        var templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        var viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(engine);
        viewResolver.setCharacterEncoding("UTF-8");
        mockMvc = MockMvcBuilders.standaloneSetup(new TemplateProbeController())
                .setViewResolvers(viewResolver).build();
    }

    static Stream<Arguments> pages() {
        return IntStream.range(0, PAGES.size()).boxed()
                .flatMap(index -> Stream.of("", "/smartenvelope")
                        .flatMap(contextPath -> Stream.of(false, true).map(filled -> Arguments.of(index, PAGES.get(index), contextPath, filled))));
    }

    @ParameterizedTest(name = "{1} — contextPath={2}, có dữ liệu={3}")
    @MethodSource("pages")
    @DisplayName("Mọi trang render đúng head, một lần CSS và đúng context path")
    void eachPageLoadsSharedStylesExactlyOnce(int index, String template, String contextPath, boolean filled) throws Exception {
        var response = mockMvc.perform(get(contextPath + "/__ui-check/" + index).contextPath(contextPath).param("filled", String.valueOf(filled)))
                .andExpect(status().isOk()).andReturn().getResponse();
        String html = response.getContentAsString(StandardCharsets.UTF_8);
        boolean applicationPage = !template.startsWith("auth/") && !template.startsWith("error/");
        assertThat(count(html, "id=\"sidebarMenu\"")).isEqualTo(applicationPage ? 1 : 0);
        assertThat(count(html, "class=\"navbar-custom app-header\"")).isEqualTo(applicationPage ? 1 : 0);
        if (applicationPage) {
            assertThat(html).contains("data-bs-toggle=\"offcanvas\"", "aria-controls=\"sidebarMenu\"",
                    "aria-label=\"Đóng menu điều hướng\"", "method=\"post\"", "action=\"" + contextPath + "/logout\"");
            assertThat(count(html, "src=\"" + contextPath + "/js/layout.js\"")).isEqualTo(1);
            assertThat(html).doesNotContain("href=\"#\"", "data-bs-toggle=\"collapse\"");
        }
        // Chỉ xuất bản xem thử khi được yêu cầu; toàn bộ dữ liệu là fixture trong bộ nhớ.
        String previewDir = System.getProperty("ui.preview.dir");
        if (previewDir != null && contextPath.isEmpty()) {
            var directory = java.nio.file.Path.of(previewDir);
            java.nio.file.Files.createDirectories(directory);
            java.nio.file.Files.writeString(directory.resolve(template.replace('/', '-') + (filled ? "-filled" : "") + ".html"), html, StandardCharsets.UTF_8);
        }
        String head = html.substring(html.indexOf("<head"), html.indexOf("</head>") + 7);
        String sharedCss = "href=\"" + contextPath + "/css/style.css\"";
        assertThat(count(html, sharedCss)).as(template + ": CSS chung chỉ nạp một lần").isEqualTo(1);
        assertThat(head).contains(sharedCss, "charset=\"UTF-8\"", "name=\"viewport\"");
        String bootstrap = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css";
        assertThat(count(html, bootstrap)).isEqualTo(1);
        assertThat(head.indexOf(bootstrap)).isLessThan(head.indexOf(sharedCss));
        for (String child : List.of("tokens", "base", "components", "utilities", "layout", "member1")) {
            assertThat(html).doesNotContain("/css/" + child + ".css");
        }
        boolean legacy = template.matches("(account|transaction|envelope|budget|savings|report|alert)/.*");
        if (!legacy) assertThat(html).doesNotContain("Master Layout v1.0", "(TV2)", "(TV3)", "password123", "10.000.000", "<code");
        if (template.startsWith("error/")) assertThat(html).doesNotContain("confidential-internal-detail", "java.sql.SQLException");

        if (legacy) {
            assertThat(count(html, "bootstrap.bundle.min.js")).isEqualTo(1);
            assertThat(count(html, "src=\"" + contextPath + "/js/app.js\"")).isEqualTo(1);
            if (template.startsWith("account/") || template.startsWith("envelope/")) {
                assertThat(html).contains("id=\"main-content\"");
            } else {
                assertThat(html).contains("class=\"d-flex\"", "class=\"flex-grow-1 p-4\"");
                assertThat(html).doesNotContain("main-content");
            }
        }
    }

    @Test
    @DisplayName("Danh sách kiểm thử bao phủ đủ các trang hiện có và CSS con tồn tại")
    void inventoryAndImportedResourcesExist() throws Exception {
        var root = java.nio.file.Path.of("src/main/resources/templates");
        try (var paths = java.nio.file.Files.walk(root)) {
            var actual = paths.filter(path -> path.toString().endsWith(".html"))
                    .map(root::relativize).map(path -> path.toString().replace('\\', '/'))
                    .filter(path -> !path.startsWith("fragments/") && !path.startsWith("layouts/"))
                    .map(path -> path.substring(0, path.length() - 5)).toList();
            assertThat(PAGES).containsExactlyInAnyOrderElementsOf(actual);
        }
        String entry = new ClassPathResource("static/css/style.css").getContentAsString(StandardCharsets.UTF_8);
        for (String child : List.of("tokens", "base", "components", "utilities", "layout", "member1")) {
            assertThat(count(entry, "@import url(\"./" + child + ".css\")")).isEqualTo(1);
            assertThat(new ClassPathResource("static/css/" + child + ".css").exists()).isTrue();
        }
    }

    private static long count(String text, String literal) {
        return Pattern.compile(Pattern.quote(literal)).matcher(text).results().count();
    }

    @Controller
    @TestComponent
    static class TemplateProbeController {
        @GetMapping("/__ui-check/{index}")
        ModelAndView render(@PathVariable("index") int index, @RequestParam(defaultValue = "false", name = "filled") boolean filled) {
            User user = new User("kiemthu", "", "Người kiểm thử với họ và tên rất dài để kiểm tra hiển thị", "kiemthu@example.test", Role.ADMIN);
            user.setId(1L);
            user.setStatus(UserStatus.ACTIVE);
            Workspace workspace = new Workspace("Quỹ chi tiêu chung của gia đình với tên dài để kiểm tra hiển thị trên điện thoại", WorkspaceType.SHARED, "Dữ liệu mẫu", user);
            workspace.setId(1L);
            WorkspaceInvitation invitation = new WorkspaceInvitation(workspace, user.getEmail(), user,
                    MemberRole.VIEWER, "token-kiem-thu", LocalDateTime.of(2026, 11, 30, 12, 0));
            invitation.setId(10L);
            var period = new com.smartenvelope.entity.BudgetPeriod();
            period.setId(1L); period.setName("Kỳ kiểm thử"); period.setWorkspace(workspace);
            period.setTotalIncome(java.math.BigDecimal.ZERO); period.setTotalAllocated(java.math.BigDecimal.ZERO);
            var view = new ModelAndView(PAGES.get(index));
            view.addObject("accountRequest", new com.smartenvelope.dto.account.AccountRequest());
            view.addObject("accountUpdateRequest", new com.smartenvelope.dto.account.AccountUpdateRequest());
            view.addObject("accountId", 1L);
            view.addObject("accounts", List.of()); view.addObject("archived", false);
            view.addObject("period", period); view.addObject("envelopes", List.of());
            view.addObject("canCreate", false); view.addObject("canManageBudget", false);
            view.addObject("envelopeRequest", new com.smartenvelope.dto.envelope.EnvelopeRequest());
            view.addAllObjects(Map.of("currentUser", user, "currentWorkspace", workspace,
                    "availableWorkspaces", List.of(workspace), "currentAdminId", 1L,
                    "userDetail", new AdminUserResponse(user, 1), "workspace", workspace,
                    "currentUserRole", MemberRole.OWNER, "invitation", invitation, "isEmailMatch", true));
            view.addAllObjects(Map.of("categories", List.of(), "categoryTypes", CategoryType.values(),
                    "categoryRequest", new AdminCategoryRequest(), "categoryId", 1L,
                    "roles", Role.values(), "statuses", UserStatus.values(), "usersPage", Page.empty(),
                    "registerRequest", new RegisterRequest()));
            view.addAllObjects(Map.of("workspaces", List.of(), "pendingInvitations", List.of(),
                    "invitations", List.of(), "members", List.of(),
                    "workspaceForm", new WorkspaceCreateRequest(), "updateForm", new WorkspaceUpdateRequest()));
            view.addObject("updateForm", new WorkspaceUpdateRequest(workspace.getName(), workspace.getDescription()));
            view.addObject("budgetRequest", new com.smartenvelope.dto.budget.BudgetRequest(null, null, null, null, null));
            view.addObject("savingsGoalRequest", new com.smartenvelope.dto.savings.SavingsGoalRequest(null, null, null, null));
            if (PAGES.get(index).startsWith("error/")) view.addObject("errorMessage", "java.sql.SQLException: confidential-internal-detail");
            if (filled) {
                User target = new User("thanhvien", "", "Thành viên có họ và tên dài để kiểm tra bảng", "thanhvien@example.test", Role.USER);
                target.setId(2L); target.setStatus(UserStatus.ACTIVE); target.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
                User locked = new User("d akhoa".replace(" ", ""), "", "Người dùng đã khóa", "dakhoa@example.test", Role.USER);
                locked.setId(3L); locked.setStatus(UserStatus.LOCKED);
                view.addObject("usersPage", new org.springframework.data.domain.PageImpl<>(List.of(user, target, locked), org.springframework.data.domain.PageRequest.of(1, 3), 12));
                var category = new com.smartenvelope.entity.Category("Chi tiêu sinh hoạt", CategoryType.EXPENSE, "bi-cart", "#243d35", null, true);
                category.setId(1L);
                var inactive = new com.smartenvelope.entity.Category("Thu nhập", CategoryType.INCOME, "bi-wallet2", "#287a56", null, true, false);
                inactive.setId(2L);
                view.addObject("categories", List.of(category, inactive));
                var summary = new com.smartenvelope.dto.workspace.WorkspaceSummaryDTO(1L, workspace.getName(), WorkspaceType.SHARED, workspace.getDescription(), user.getFullName(), 1L, 2, MemberRole.OWNER, true, null);
                view.addObject("workspaces", List.of(summary));
                view.addObject("members", List.of(new com.smartenvelope.dto.workspace.WorkspaceMemberDTO(2L, 2L, target.getUsername(), target.getFullName(), target.getEmail(), null, MemberRole.WRITER, com.smartenvelope.enums.MembershipStatus.ACCEPTED, target.getCreatedAt(), false, true, true)));
                view.addObject("invitations", List.of(invitation));
                view.addObject("pendingInvitations", List.of(invitation));
            }
            return view;
        }
    }
}
