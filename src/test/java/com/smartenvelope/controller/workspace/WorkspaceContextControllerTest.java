package com.smartenvelope.controller.workspace;

import com.smartenvelope.service.WorkspaceContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceContextControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkspaceContextService workspaceContextService;

    @InjectMocks
    private WorkspaceContextController workspaceContextController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workspaceContextController).build();
    }

    @Test
    @DisplayName("STT 5: Chuyển đổi Workspace thành công -> Redirect về trang Referer với thông báo thành công")
    void testSwitchWorkspace_success_withReferer() throws Exception {
        doNothing().when(workspaceContextService).setCurrentWorkspaceId(202L);

        mockMvc.perform(post("/workspace/switch")
                        .param("workspaceId", "202")
                        .header("Referer", "http://localhost:8080/envelopes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/envelopes"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(workspaceContextService).setCurrentWorkspaceId(202L);
    }

    @Test
    @DisplayName("Chuyển đổi Workspace thành công không có Referer -> Redirect về /dashboard")
    void testSwitchWorkspace_success_withoutReferer() throws Exception {
        doNothing().when(workspaceContextService).setCurrentWorkspaceId(101L);

        mockMvc.perform(post("/workspace/switch")
                        .param("workspaceId", "101"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(workspaceContextService).setCurrentWorkspaceId(101L);
    }

    @Test
    @DisplayName("STT 6 & 7: Chuyển đổi sang Workspace không có quyền -> Bị từ chối và hiển thị thông báo lỗi")
    void testSwitchWorkspace_accessDenied() throws Exception {
        doThrow(new AccessDeniedException("Bạn không có quyền truy cập vào Workspace này"))
                .when(workspaceContextService).setCurrentWorkspaceId(999L);

        mockMvc.perform(post("/workspace/switch")
                        .param("workspaceId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(workspaceContextService).setCurrentWorkspaceId(999L);
    }

    @Test
    @DisplayName("Gửi request không có workspaceId -> Hiển thị thông báo lỗi và redirect")
    void testSwitchWorkspace_nullId() throws Exception {
        mockMvc.perform(post("/workspace/switch"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(workspaceContextService, never()).setCurrentWorkspaceId(anyLong());
    }

    @Test
    @DisplayName("Không cho phép phương thức GET truy cập /workspace/switch (tránh chuyển đổi URL GET tuỳ tiện)")
    void testSwitchWorkspace_methodGetNotAllowed() throws Exception {
        mockMvc.perform(get("/workspace/switch"))
                .andExpect(status().isMethodNotAllowed());
    }
}

