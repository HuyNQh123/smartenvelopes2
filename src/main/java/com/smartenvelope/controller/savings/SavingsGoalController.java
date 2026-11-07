package com.smartenvelope.controller.savings;

import com.smartenvelope.dto.savings.SavingsGoalRequest;
import com.smartenvelope.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Controller xử lý các chức năng quản lý mục tiêu tiết kiệm và heo đất điện tử.
 * Phụ trách bởi: Thành viên 4.
 */
@Controller
@RequestMapping("/savings")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @GetMapping
    public String list(Model model) {

        model.addAttribute("goals", savingsGoalService.getGoals());

        model.addAttribute("piggyBankBalance", savingsGoalService.getPiggyBankBalance());

        return "savings/list";
    }

    @GetMapping("/new")
    public String form(Model model) {

        model.addAttribute("savingsGoalRequest", new SavingsGoalRequest());

        return "savings/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute SavingsGoalRequest savingsGoalRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            return "savings/form";
        }
        try {

            savingsGoalService.createGoal(savingsGoalRequest);

            redirectAttributes.addFlashAttribute("successMessage", "Mục tiêu tiết kiệm đã được tạo.");

            return "redirect:/savings";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("errorMessage", ex.getMessage());

            return "savings/form";
        }
    }

    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id, @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {

            savingsGoalService.deposit(id, amount);

            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật khoản tiết kiệm.");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/savings";
    }
}
