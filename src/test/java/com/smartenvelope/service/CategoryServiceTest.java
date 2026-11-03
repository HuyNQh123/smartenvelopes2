package com.smartenvelope.service;

import com.smartenvelope.dto.admin.AdminCategoryRequest;
import com.smartenvelope.entity.Category;
import com.smartenvelope.entity.User;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.CategoryRepository;
import com.smartenvelope.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, transactionRepository);
    }

    @Test
    @DisplayName("Admin tạo mới danh mục hệ thống thành công")
    void testCreateSystemCategory_success() {
        AdminCategoryRequest request = new AdminCategoryRequest("Du lịch", CategoryType.EXPENSE, "bi-airplane", "#2196F3", true);

        when(categoryRepository.existsByNameIgnoreCaseAndTypeAndIsSystemTrue("Du lịch", CategoryType.EXPENSE))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(101L);
            return cat;
        });

        Category created = categoryService.createSystemCategory(request);

        assertNotNull(created);
        assertEquals(101L, created.getId());
        assertEquals("Du lịch", created.getName());
        assertEquals(CategoryType.EXPENSE, created.getType());
        assertTrue(created.getIsSystem());
        assertTrue(created.getIsActive());
        assertNull(created.getUser());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Tạo danh mục hệ thống bị lỗi nếu trùng tên và cùng loại")
    void testCreateSystemCategory_duplicateNameThrows() {
        AdminCategoryRequest request = new AdminCategoryRequest("Ăn uống", CategoryType.EXPENSE, "bi-cup", "#FF5722", true);

        when(categoryRepository.existsByNameIgnoreCaseAndTypeAndIsSystemTrue("Ăn uống", CategoryType.EXPENSE))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                categoryService.createSystemCategory(request));

        assertTrue(ex.getMessage().contains("đã tồn tại"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin cập nhật danh mục hệ thống thành công")
    void testUpdateSystemCategory_success() {
        Category existing = new Category("Tiền nhà", CategoryType.EXPENSE, "bi-house", "#3F51B5", null, true, true);
        existing.setId(2L);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameIgnoreCaseAndTypeAndIsSystemTrueAndIdNot("Tiền thuê nhà", CategoryType.EXPENSE, 2L))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminCategoryRequest request = new AdminCategoryRequest("Tiền thuê nhà", CategoryType.EXPENSE, "bi-building", "#1A237E", true);
        Category updated = categoryService.updateSystemCategory(2L, request);

        assertEquals("Tiền thuê nhà", updated.getName());
        assertEquals("bi-building", updated.getIcon());
        verify(categoryRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Cập nhật danh mục: Không được sửa danh mục riêng của người dùng từ Admin")
    void testUpdateSystemCategory_cannotUpdateUserCategory() {
        User user = new User();
        user.setId(5L);
        Category userCat = new Category("Quỹ riêng", CategoryType.EXPENSE, "bi-wallet", "#000", user, false, true);
        userCat.setId(99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.of(userCat));

        AdminCategoryRequest request = new AdminCategoryRequest("Sửa danh mục", CategoryType.EXPENSE, "bi-tag", "#FFF", true);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                categoryService.updateSystemCategory(99L, request));

        assertTrue(ex.getMessage().contains("Chỉ Quản trị viên mới có quyền"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Vô hiệu hóa danh mục hệ thống thành công")
    void testDeactivateCategory_success() {
        Category category = new Category("Y tế", CategoryType.EXPENSE, "bi-heart", "#F44336", null, true, true);
        category.setId(7L);

        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));

        categoryService.deactivateCategory(7L);

        assertFalse(category.getIsActive());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Kích hoạt lại danh mục hệ thống thành công")
    void testActivateCategory_success() {
        Category category = new Category("Y tế", CategoryType.EXPENSE, "bi-heart", "#F44336", null, true, false);
        category.setId(7L);

        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));

        categoryService.activateCategory(7L);

        assertTrue(category.getIsActive());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Xóa danh mục hệ thống: Thất bại nếu đã có Transaction liên kết")
    void testDeleteSystemCategory_hasTransactions_throws() {
        Category category = new Category("Ăn uống", CategoryType.EXPENSE, "bi-cup", "#FF5722", null, true, true);
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionRepository.existsByCategoryId(1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                categoryService.deleteSystemCategory(1L));

        assertTrue(ex.getMessage().contains("đã có giao dịch tài chính liên kết"));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Xóa danh mục hệ thống: Thành công nếu chưa từng có giao dịch nào")
    void testDeleteSystemCategory_noTransactions_success() {
        Category category = new Category("Danh mục mới chưa dùng", CategoryType.EXPENSE, "bi-tag", "#000", null, true, true);
        category.setId(88L);

        when(categoryRepository.findById(88L)).thenReturn(Optional.of(category));
        when(transactionRepository.existsByCategoryId(88L)).thenReturn(false);

        categoryService.deleteSystemCategory(88L);

        verify(categoryRepository, times(1)).delete(category);
    }
}

