package com.smartenvelope.controller.dashboard;

import com.smartenvelope.controller.advice.GlobalModelAttributeAdvice;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.WorkspaceType;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.WorkspaceContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private WorkspaceContextService workspaceContextService;

    @BeforeEach
    void setUp() {
        DashboardController dashboardController = new DashboardController();
        GlobalModelAttributeAdvice advice = new GlobalModelAttributeAdvice(currentUserService, workspaceContextService);

        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(advice)
                .build();
    }

    @Test
    @DisplayName("GET /dashboard khi đã đăng nhập có workspace")
    void testDashboard_authenticated_withWorkspace() throws Exception {
        User user = new User("vana", "pass", "Nguyễn Văn A", "vana@example.com", Role.USER);
        user.setId(1L);

        Workspace ws = new Workspace("Quỹ cá nhân", WorkspaceType.PERSONAL, "Ví", user);
        ws.setId(101L);

        when(currentUserService.isAuthenticated()).thenReturn(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(workspaceContextService.getCurrentWorkspace()).thenReturn(ws);
        when(workspaceContextService.getCurrentWorkspaceId()).thenReturn(101L);
        when(workspaceContextService.getAvailableWorkspaces()).thenReturn(List.of(ws));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"));
    }

    @Test
    @DisplayName("GET /dashboard khi đã đăng nhập nhưng không có workspace")
    void testDashboard_authenticated_noWorkspace() throws Exception {
        User user = new User("vana", "pass", "Nguyễn Văn A", "vana@example.com", Role.USER);
        user.setId(1L);

        when(currentUserService.isAuthenticated()).thenReturn(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(workspaceContextService.getCurrentWorkspace()).thenReturn(null);
        when(workspaceContextService.getCurrentWorkspaceId()).thenReturn(null);
        when(workspaceContextService.getAvailableWorkspaces()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"));
    }
}

