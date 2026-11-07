package com.smartenvelope.ui;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Xác minh Spring Boot tìm đúng template lỗi, không thêm controller hoặc sửa error handler. */
@SpringBootTest
@AutoConfigureMockMvc
class ErrorPageIntegrationTest {
    @Autowired MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(ints = {403, 404, 500})
    @DisplayName("Error dispatch chọn đúng trang lỗi và không lộ thông tin nội bộ")
    void standardErrorView(int code) throws Exception {
        String html = mockMvc.perform(get("/error").accept("text/html")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, code)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/duong-dan-kiem-thu")
                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, "confidential-internal-detail"))
                .andExpect(status().is(code)).andExpect(view().name("error/" + code))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(html).contains("/css/style.css").doesNotContain("confidential-internal-detail", "stackTrace");
    }
}
