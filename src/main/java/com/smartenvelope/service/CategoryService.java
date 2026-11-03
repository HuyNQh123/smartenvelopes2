package com.smartenvelope.service;

import com.smartenvelope.dto.admin.AdminCategoryRequest;
import com.smartenvelope.entity.Category;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.CategoryRepository;
import com.smartenvelope.repository.TransactionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các nghiệp vụ quản lý danh mục thu/chi (Category).
 * Hỗ trợ danh mục mặc định toàn hệ thống (Admin quản lý) và danh mục khả dụng cho người dùng.
 * Phụ trách bởi: Thành viên 1 (Admin Category) & Thành viên 2 (Account & Category).
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Lấy danh sách danh mục hệ thống có hỗ trợ lọc theo loại, trạng thái và tìm kiếm tên.
     */
    @Transactional(readOnly = true)
    public List<Category> getSystemCategories(CategoryType type, Boolean isActive, String keyword) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return categoryRepository.findSystemCategoriesFilter(type, isActive, cleanKeyword);
    }

    /**
     * Lấy thông tin chi tiết một danh mục theo ID.
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
    }

    /**
     * Tạo mới một danh mục mặc định của hệ thống (chỉ dành cho Quản trị viên).
     * Tự động gán is_system = true, user = null, is_active = true.
     * Kiểm tra chống trùng tên và loại trong phạm vi hệ thống.
     */
    @Transactional
    public Category createSystemCategory(AdminCategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu danh mục không được để trống");
        }

        String name = request.getName().trim();
        CategoryType type = request.getType();

        if (categoryRepository.existsByNameIgnoreCaseAndTypeAndIsSystemTrue(name, type)) {
            throw new IllegalArgumentException("Danh mục hệ thống với tên '" + name + "' và loại " + type + " đã tồn tại!");
        }

        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(request.getIcon() != null && !request.getIcon().trim().isEmpty() ? request.getIcon().trim() : "bi-tag");
        category.setColor(request.getColor() != null && !request.getColor().trim().isEmpty() ? request.getColor().trim() : "#0d6efd");
        category.setIsSystem(true);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        category.setUser(null);

        return categoryRepository.save(category);
    }

    /**
     * Chỉnh sửa danh mục mặc định của hệ thống.
     * Tuyệt đối không cho phép chỉnh sửa danh mục cá nhân của người dùng từ trang Admin.
     */
    @Transactional
    public Category updateSystemCategory(Long categoryId, AdminCategoryRequest request) {
        Category category = getCategoryById(categoryId);

        if (!Boolean.TRUE.equals(category.getIsSystem())) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền chỉnh sửa danh mục mặc định hệ thống!");
        }

        String name = request.getName().trim();
        CategoryType type = request.getType();

        if (categoryRepository.existsByNameIgnoreCaseAndTypeAndIsSystemTrueAndIdNot(name, type, categoryId)) {
            throw new IllegalArgumentException("Danh mục hệ thống khác với tên '" + name + "' và loại " + type + " đã tồn tại!");
        }

        category.setName(name);
        category.setType(type);
        if (request.getIcon() != null && !request.getIcon().trim().isEmpty()) {
            category.setIcon(request.getIcon().trim());
        }
        if (request.getColor() != null && !request.getColor().trim().isEmpty()) {
            category.setColor(request.getColor().trim());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        return categoryRepository.save(category);
    }

    /**
     * Vô hiệu hóa một danh mục hệ thống.
     * Danh mục bị vô hiệu hóa sẽ không hiển thị khi chọn cho giao dịch mới,
     * nhưng vẫn bảo toàn đầy đủ các giao dịch cũ trong lịch sử.
     */
    @Transactional
    public void deactivateCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        if (!Boolean.TRUE.equals(category.getIsSystem())) {
            throw new AccessDeniedException("Chỉ được thao tác trên danh mục hệ thống.");
        }
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    /**
     * Kích hoạt lại danh mục hệ thống.
     */
    @Transactional
    public void activateCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        if (!Boolean.TRUE.equals(category.getIsSystem())) {
            throw new AccessDeniedException("Chỉ được thao tác trên danh mục hệ thống.");
        }
        category.setIsActive(true);
        categoryRepository.save(category);
    }

    /**
     * Xóa danh mục hệ thống với kiểm tra an toàn:
     * Tuyệt đối không xóa vật lý danh mục đã từng phát sinh Transaction để bảo toàn báo cáo tài chính.
     */
    @Transactional
    public void deleteSystemCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        if (!Boolean.TRUE.equals(category.getIsSystem())) {
            throw new AccessDeniedException("Chỉ được thao tác trên danh mục hệ thống.");
        }

        // Kiểm tra ràng buộc với các giao dịch đã có
        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new IllegalStateException("Không thể xóa vĩnh viễn danh mục này vì đã có giao dịch tài chính liên kết. Hãy sử dụng chức năng Vô hiệu hóa để bảo toàn lịch sử sổ sách!");
        }

        categoryRepository.delete(category);
    }

    /**
     * Lấy tất cả danh mục khả dụng (danh mục hệ thống đang kích hoạt + danh mục riêng của người dùng).
     */
    @Transactional(readOnly = true)
    public List<Category> getAvailableCategoriesForUser(Long userId) {
        return categoryRepository.findAllAvailableForUser(userId);
    }

    /**
     * Lấy tất cả danh mục khả dụng theo loại thu/chi.
     */
    @Transactional(readOnly = true)
    public List<Category> getAvailableCategoriesForUserAndType(Long userId, CategoryType type) {
        return categoryRepository.findAllAvailableForUserAndType(userId, type);
    }
}

