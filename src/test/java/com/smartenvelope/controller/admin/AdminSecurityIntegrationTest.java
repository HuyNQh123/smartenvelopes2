package com.smartenvelope.controller.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Người dùng chưa đăng nhập truy cập /admin/users phải redirect về /login")
    void testAnonymous_adminUsers_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Người dùng chưa đăng nhập truy cập /admin/categories phải redirect về /login")
    void testAnonymous_adminCategories_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Người dùng USER truy cập /admin/users bị từ chối 403 Forbidden")
    @WithMockUser(username = "normal_user@example.com", roles = "USER")
    void testUser_adminUsers_forbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Người dùng USER truy cập /admin/categories bị từ chối 403 Forbidden")
    @WithMockUser(username = "normal_user@example.com", roles = "USER")
    void testUser_adminCategories_forbidden() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Người dùng USER gọi API khóa tài khoản bị từ chối 403 Forbidden")
    @WithMockUser(username = "normal_user@example.com", roles = "USER")
    void testUser_lockUserApi_forbidden() throws Exception {
        mockMvc.perform(post("/admin/users/1/lock").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN truy cập /admin/users được phép (200 OK)")
    @WithMockUser(username = "deptraihuy33@gmail.com", roles = "ADMIN")
    void testAdmin_adminUsers_allowed() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN truy cập /admin/categories được phép (200 OK)")
    @WithMockUser(username = "deptraihuy33@gmail.com", roles = "ADMIN")
    void testAdmin_adminCategories_allowed() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk());
    }
}

