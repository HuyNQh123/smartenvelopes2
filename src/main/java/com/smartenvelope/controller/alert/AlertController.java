package com.smartenvelope.controller.alert;

import com.smartenvelope.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các chức năng xem và quản lý thông báo, cảnh báo chi tiêu.
 * Hỗ trợ cả hai đường dẫn /alerts và /notifications để đồng bộ giữa các thành
 * viên.
 * Phụ trách bởi: Thành viên 4.
 */
@Controller
@RequestMapping({ "/alerts", "/notifications" })
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notifications", alertService.getNotifications());
        return "alert/list";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        alertService.markAsRead(id);
        redirectAttributes.addFlashAttribute("successMessage", "Thông báo đã được đánh dấu là đã đọc.");
        return "redirect:/alerts";
    }
}
