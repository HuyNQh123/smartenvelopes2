package com.smartenvelope.controller.budget;

import com.smartenvelope.dto.budget.BudgetRequest;
import com.smartenvelope.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;

/**
 * Controller xử lý các chức năng thiết lập kỳ ngân sách và phân bổ ngân sách.
 * Phụ trách bởi: Thành viên 4.
 */
@Controller
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public String list(Model model) {

        model.addAttribute("budgets", budgetService.getCurrentWorkspaceBudgets());
        return "budget/list";
    }

    @GetMapping("/new")
    public String form(Model model) {

        BudgetRequest request = new BudgetRequest();

        YearMonth currentMonth = YearMonth.now();

        request.setMonth(currentMonth.getMonthValue());
        request.setYear(currentMonth.getYear());
        request.setName("Ngân sách tháng " + currentMonth.getMonthValue());

        model.addAttribute("budgetRequest", request);

        return "budget/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute BudgetRequest budgetRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            return "budget/form";
        }

        try {

            budgetService.createBudget(budgetRequest);

            redirectAttributes.addFlashAttribute("successMessage", "Kỳ ngân sách đã được tạo.");

            return "redirect:/budgets";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("errorMessage", ex.getMessage());
            return "budget/form";
        }
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            budgetService.confirmBudget(id);

            redirectAttributes.addFlashAttribute("successMessage", "Kỳ ngân sách đã được xác nhận.");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/budgets";
    }
}
