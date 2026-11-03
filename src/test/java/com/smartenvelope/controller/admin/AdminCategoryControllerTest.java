package com.smartenvelope.controller.admin;

import com.smartenvelope.dto.admin.AdminCategoryRequest;
import com.smartenvelope.entity.Category;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class AdminCategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        AdminCategoryController controller = new AdminCategoryController(categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /admin/categories hiển thị danh sách danh mục hệ thống")
    void testListCategories() throws Exception {
        Category cat = new Category("Ăn uống", CategoryType.EXPENSE, "bi-cup", "#FF5722", null, true, true);
        cat.setId(1L);

        when(categoryService.getSystemCategories(any(), any(), any())).thenReturn(List.of(cat));

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories", "categoryTypes"));
    }

    @Test
    @DisplayName("GET /admin/categories/create hiển thị form tạo danh mục")
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/admin/categories/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category-create"))
                .andExpect(model().attributeExists("categoryRequest", "categoryTypes"));
    }

    @Test
    @DisplayName("POST /admin/categories/create tạo danh mục hợp lệ chuyển hướng thành công")
    void testCreateCategory_valid() throws Exception {
        Category created = new Category("Làm thêm", CategoryType.INCOME, "bi-cash", "#2E7D32", null, true, true);
        created.setId(10L);

        when(categoryService.createSystemCategory(any(AdminCategoryRequest.class))).thenReturn(created);

        mockMvc.perform(post("/admin/categories/create")
                        .param("name", "Làm thêm")
                        .param("type", "INCOME")
                        .param("icon", "bi-cash")
                        .param("color", "#2E7D32")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/categories/create tên rỗng báo lỗi validation")
    void testCreateCategory_invalidName() throws Exception {
        mockMvc.perform(post("/admin/categories/create")
                        .param("name", "")
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category-create"))
                .andExpect(model().hasErrors());

        verify(categoryService, never()).createSystemCategory(any());
    }

    @Test
    @DisplayName("GET /admin/categories/{id}/edit hiển thị form chỉnh sửa")
    void testShowEditForm() throws Exception {
        Category cat = new Category("Tiền nhà", CategoryType.EXPENSE, "bi-house", "#3F51B5", null, true, true);
        cat.setId(2L);

        when(categoryService.getCategoryById(2L)).thenReturn(cat);

        mockMvc.perform(get("/admin/categories/2/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category-edit"))
                .andExpect(model().attributeExists("categoryRequest", "categoryId", "categoryTypes"));
    }

    @Test
    @DisplayName("POST /admin/categories/{id}/edit cập nhật thành công")
    void testUpdateCategory_success() throws Exception {
        Category updated = new Category("Tiền nhà mới", CategoryType.EXPENSE, "bi-house", "#3F51B5", null, true, true);
        updated.setId(2L);

        when(categoryService.updateSystemCategory(eq(2L), any(AdminCategoryRequest.class))).thenReturn(updated);

        mockMvc.perform(post("/admin/categories/2/edit")
                        .param("name", "Tiền nhà mới")
                        .param("type", "EXPENSE")
                        .param("icon", "bi-house")
                        .param("color", "#3F51B5")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/categories/{id}/deactivate vô hiệu hóa thành công")
    void testDeactivateCategory() throws Exception {
        doNothing().when(categoryService).deactivateCategory(2L);

        mockMvc.perform(post("/admin/categories/2/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/categories/{id}/activate kích hoạt lại thành công")
    void testActivateCategory() throws Exception {
        doNothing().when(categoryService).activateCategory(2L);

        mockMvc.perform(post("/admin/categories/2/activate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/categories/{id}/delete xóa thành công khi chưa có giao dịch")
    void testDeleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteSystemCategory(2L);

        mockMvc.perform(post("/admin/categories/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/categories/{id}/delete báo lỗi khi đã có giao dịch")
    void testDeleteCategory_hasTransactions_error() throws Exception {
        doThrow(new IllegalStateException("Không thể xóa vĩnh viễn danh mục này vì đã có giao dịch liên kết!"))
                .when(categoryService).deleteSystemCategory(2L);

        mockMvc.perform(post("/admin/categories/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}

