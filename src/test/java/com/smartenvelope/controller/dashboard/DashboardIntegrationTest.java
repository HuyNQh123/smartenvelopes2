package com.smartenvelope.controller.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Khi chưa đăng nhập truy cập /dashboard phải redirect về /login, không được báo lỗi 500")
    void testGetDashboard_anonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Khi đã đăng nhập với user thực tế trong DB truy cập /dashboard trả về 200 OK")
    @WithMockUser(username = "deptraihuy33@gmail.com", roles = "USER")
    void testGetDashboard_authenticated_realUser() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
