package com.smartenvelope.controller.budget;

import java.time.YearMonth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartenvelope.dto.budget.BudgetPeriodRequest;
import com.smartenvelope.service.BudgetService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute(
                "periods",
                budgetService.getPeriodsForCurrentWorkspace()
        );

        return "budget/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        YearMonth currentMonth = YearMonth.now();

        BudgetPeriodRequest request = new BudgetPeriodRequest();
        request.setMonth(currentMonth.getMonthValue());
        request.setYear(currentMonth.getYear());

        model.addAttribute("budgetPeriodRequest", request);

        return "budget/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("budgetPeriodRequest")
            BudgetPeriodRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "budget/form";
        }

        try {
            budgetService.createPeriod(request);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "budget.invalid",
                    exception.getMessage()
            );
            return "budget/form";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.reject(
                    "budget.conflict",
                    "Không thể lưu kỳ. Hãy kiểm tra danh sách xem kỳ này "
                            + "đã được tạo chưa rồi thử lại."
            );
            return "budget/form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã tạo kỳ ngân sách ở trạng thái nháp."
        );

        return "redirect:/budgets";
    }
}