package com.smartenvelope.controller.admin;

import com.smartenvelope.dto.admin.AdminUserResponse;
import com.smartenvelope.entity.User;
import com.smartenvelope.enums.Role;
import com.smartenvelope.enums.UserStatus;
import com.smartenvelope.service.CurrentUserService;
import com.smartenvelope.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các chức năng Quản lý tài khoản người dùng dành riêng cho Quản trị viên (Admin).
 * Đường dẫn gốc: /admin/users
 * Bảo mật: Chỉ người dùng có vai trò ROLE_ADMIN mới được truy cập.
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public AdminUserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    /**
     * Hiển thị danh sách người dùng trong hệ thống có hỗ trợ tìm kiếm, lọc theo vai trò, trạng thái và phân trang.
     */
    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "role", required = false) Role role,
                            @RequestParam(value = "status", required = false) UserStatus status,
                            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                            Model model) {
        Page<User> usersPage = userService.searchUsers(keyword, role, status, pageable);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("roles", Role.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("currentAdminId", currentUserService.getCurrentUserId());

        return "admin/users";
    }

    /**
     * Xem thông tin chi tiết một người dùng.
     */
    @GetMapping("/{id}")
    public String viewUserDetail(@PathVariable("id") Long id, Model model) {
        AdminUserResponse userDetail = userService.getUserDetail(id);
        model.addAttribute("userDetail", userDetail);
        model.addAttribute("currentAdminId", currentUserService.getCurrentUserId());
        return "admin/user-detail";
    }

    /**
     * Khóa tài khoản người dùng.
     */
    @PostMapping("/{id}/lock")
    public String lockUser(@PathVariable("id") Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            Long currentAdminId = currentUserService.getCurrentUserId();
            userService.lockUser(id, currentAdminId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản người dùng thành công.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể khóa tài khoản: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Mở khóa tài khoản người dùng.
     */
    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            Long currentAdminId = currentUserService.getCurrentUserId();
            userService.unlockUser(id, currentAdminId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản người dùng thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể mở khóa tài khoản: " + ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}

