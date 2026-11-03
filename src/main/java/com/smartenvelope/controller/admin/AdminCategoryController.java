package com.smartenvelope.controller.admin;

import com.smartenvelope.dto.admin.AdminCategoryRequest;
import com.smartenvelope.entity.Category;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller xử lý các chức năng Quản lý danh mục thu/chi mặc định của hệ thống dành riêng cho Admin.
 * Đường dẫn gốc: /admin/categories
 * Bảo mật: Chỉ tài khoản có vai trò ROLE_ADMIN mới được truy cập.
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Hiển thị danh sách tất cả các danh mục hệ thống có bộ lọc theo loại, trạng thái và từ khóa tìm kiếm.
     */
    @GetMapping
    public String listCategories(@RequestParam(value = "type", required = false) CategoryType type,
                                 @RequestParam(value = "isActive", required = false) Boolean isActive,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 Model model) {
        List<Category> categories = categoryService.getSystemCategories(type, isActive, keyword);

        model.addAttribute("categories", categories);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", isActive);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryTypes", CategoryType.values());

        return "admin/categories";
    }

    /**
     * Hiển thị giao diện form tạo mới danh mục mặc định của hệ thống.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("categoryRequest")) {
            model.addAttribute("categoryRequest", new AdminCategoryRequest());
        }
        model.addAttribute("categoryTypes", CategoryType.values());
        return "admin/category-create";
    }

    /**
     * Xử lý lưu danh mục hệ thống mới.
     */
    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute("categoryRequest") AdminCategoryRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryTypes", CategoryType.values());
            return "admin/category-create";
        }

        try {
            categoryService.createSystemCategory(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo mới danh mục hệ thống '" + request.getName() + "' thành công.");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categoryTypes", CategoryType.values());
            return "admin/category-create";
        }
    }

    /**
     * Hiển thị giao diện form chỉnh sửa danh mục hệ thống.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id);

        if (!model.containsAttribute("categoryRequest")) {
            AdminCategoryRequest request = new AdminCategoryRequest(
                    category.getName(),
                    category.getType(),
                    category.getIcon(),
                    category.getColor(),
                    category.getIsActive()
            );
            model.addAttribute("categoryRequest", request);
        }

        model.addAttribute("categoryId", id);
        model.addAttribute("category", category);
        model.addAttribute("categoryTypes", CategoryType.values());
        return "admin/category-edit";
    }

    /**
     * Xử lý cập nhật danh mục hệ thống.
     */
    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("categoryRequest") AdminCategoryRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("categoryTypes", CategoryType.values());
            return "admin/category-edit";
        }

        try {
            categoryService.updateSystemCategory(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật danh mục hệ thống thành công.");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categoryId", id);
            model.addAttribute("categoryTypes", CategoryType.values());
            return "admin/category-edit";
        }
    }

    /**
     * Vô hiệu hóa danh mục hệ thống (không chọn được cho giao dịch mới, giữ nguyên lịch sử).
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateCategory(@PathVariable("id") Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            categoryService.deactivateCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã vô hiệu hóa danh mục hệ thống thành công.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể vô hiệu hóa danh mục: " + ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Kích hoạt lại danh mục hệ thống đã bị vô hiệu hóa.
     */
    @PostMapping("/{id}/activate")
    public String activateCategory(@PathVariable("id") Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            categoryService.activateCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã kích hoạt lại danh mục hệ thống thành công.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể kích hoạt lại danh mục: " + ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Xóa vĩnh viễn danh mục hệ thống (chỉ cho phép nếu danh mục chưa từng phát sinh giao dịch).
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteSystemCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa vĩnh viễn danh mục hệ thống.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục: " + ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}

