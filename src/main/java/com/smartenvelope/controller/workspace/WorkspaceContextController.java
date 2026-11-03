package com.smartenvelope.controller.workspace;

import com.smartenvelope.service.WorkspaceContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý việc chuyển đổi Không gian tài chính (Workspace Context) trong Session.
 * Đảm bảo tính bảo mật: Chỉ cho phép người dùng chuyển sang Workspace mà họ có quyền truy cập hợp lệ.
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
@RequestMapping("/workspace")
public class WorkspaceContextController {

    private final WorkspaceContextService workspaceContextService;

    public WorkspaceContextController(WorkspaceContextService workspaceContextService) {
        this.workspaceContextService = workspaceContextService;
    }

    /**
     * Endpoint chuyển đổi Workspace đang hoạt động.
     * Sử dụng phương thức POST và yêu cầu xác thực CSRF mặc định của Spring Security.
     *
     * @param workspaceId ID của Workspace người dùng muốn kích hoạt.
     * @param request Yêu cầu HTTP hiện tại để lấy Referer chuyển hướng.
     * @param redirectAttributes Chứa thông báo flash sau khi chuyển hướng.
     * @return Chuỗi chuyển hướng (redirect) về trang trước đó hoặc /dashboard.
     */
    @PostMapping("/switch")
    public String switchWorkspace(@RequestParam(name = "workspaceId", required = false) Long workspaceId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        if (workspaceId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một Quỹ tài chính hợp lệ.");
            return determineRedirectUrl(request);
        }

        try {
            workspaceContextService.setCurrentWorkspaceId(workspaceId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển đổi không gian tài chính thành công!");
        } catch (AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Từ chối truy cập: Bạn không có quyền tham gia vào Quỹ này.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể chuyển đổi Quỹ tài chính: " + ex.getMessage());
        }

        return determineRedirectUrl(request);
    }

    /**
     * Xác định URL chuyển hướng an toàn dựa vào Header Referer.
     * Ngăn chặn vòng lặp chuyển hướng và open-redirect vulnerabilities.
     */
    private String determineRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank() 
                && !referer.contains("/workspace/switch") 
                && !referer.contains("/login") 
                && !referer.contains("/register")) {
            return "redirect:" + referer;
        }
        return "redirect:/dashboard";
    }
}

