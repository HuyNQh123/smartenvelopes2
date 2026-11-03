package com.smartenvelope.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller điều hướng khu vực Quản trị hệ thống (Admin).
 * Phụ trách bởi: Thành viên 1.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping
    public String index() {
        return "redirect:/admin/users";
    }
}
