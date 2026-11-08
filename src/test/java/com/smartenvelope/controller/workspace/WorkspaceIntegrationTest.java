package com.smartenvelope.controller.workspace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.annotation.Transactional;
import com.smartenvelope.entity.User;
import com.smartenvelope.enums.Role;
import com.smartenvelope.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkspaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void createTestUser() {
        userRepository.saveAndFlush(new User("workspace-test", "unused", "Người kiểm thử", "workspace-test@example.test", Role.USER));
    }

    @Test
    @DisplayName("STT 1: Khi chưa đăng nhập truy cập /workspaces phải redirect về /login")
    void testWorkspaces_anonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/workspaces"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("STT 2: Khi đã đăng nhập truy cập /workspaces trả về 200 OK và render giao diện")
    @WithMockUser(username = "workspace-test@example.test", roles = "USER")
    void testWorkspaces_authenticated_realUser() throws Exception {
        mockMvc.perform(get("/workspaces"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/index"))
                .andExpect(model().attributeExists("workspaces"))
                .andExpect(model().attributeExists("pendingInvitations"));
    }

    @Test
    @DisplayName("Khi đã đăng nhập truy cập /workspaces/create trả về 200 OK")
    @WithMockUser(username = "workspace-test@example.test", roles = "USER")
    void testWorkspacesCreate_authenticated() throws Exception {
        mockMvc.perform(get("/workspaces/create"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/create"))
                .andExpect(model().attributeExists("workspaceForm"));
    }

    @Test
    @DisplayName("Khi đã đăng nhập truy cập /invitations trả về 200 OK")
    @WithMockUser(username = "workspace-test@example.test", roles = "USER")
    void testInvitations_authenticated() throws Exception {
        mockMvc.perform(get("/invitations"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("workspaces/invitations"))
                .andExpect(model().attributeExists("invitations"));
    }
}

