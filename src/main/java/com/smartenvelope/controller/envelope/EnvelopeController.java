package com.smartenvelope.controller.envelope;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartenvelope.dto.envelope.EnvelopeRequest;
import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.service.EnvelopeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/envelopes")
public class EnvelopeController {

    private final EnvelopeService envelopeService;

    public EnvelopeController(EnvelopeService envelopeService) {
        this.envelopeService = envelopeService;
    }

    @GetMapping
    public String list(
            @RequestParam(name = "periodId", required = false)
            Long periodId,
            Model model) {

        if (periodId == null) {
            return "redirect:/budgets";
        }

        var period = envelopeService.getPeriod(periodId);

        model.addAttribute("period", period);
        model.addAttribute(
                "envelopes",
                envelopeService.getEnvelopes(periodId));
        model.addAttribute(
                "canCreate",
                period.getStatus() == BudgetStatus.DRAFT
                        && envelopeService.canManageCurrentWorkspace());

        return "envelope/list";
    }

    @GetMapping("/new")
    public String form(
            @RequestParam("periodId") Long periodId,
            Model model,
            RedirectAttributes redirectAttributes) {

        var period = envelopeService.getPeriod(periodId);

        if (!envelopeService.canManageCurrentWorkspace()) {
            throw new AccessDeniedException(
                    "Bạn không có quyền thêm phong bì.");
        }

        if (period.getStatus() != BudgetStatus.DRAFT) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Chỉ được thêm phong bì vào kỳ đang nháp.");

            return "redirect:/envelopes?periodId=" + periodId;
        }

        model.addAttribute("envelopeRequest", new EnvelopeRequest());
        prepareForm(periodId, model);

        return "envelope/form";
    }

    @PostMapping
    public String create(
            @RequestParam("periodId") Long periodId,
            @Valid @ModelAttribute("envelopeRequest")
            EnvelopeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra quyền kể cả khi dữ liệu form có lỗi.
        envelopeService.getPeriod(periodId);

        if (!envelopeService.canManageCurrentWorkspace()) {
            throw new AccessDeniedException(
                    "Bạn không có quyền thêm phong bì.");
        }

        if (bindingResult.hasErrors()) {
            prepareForm(periodId, model);
            return "envelope/form";
        }

        try {
            envelopeService.createEnvelope(periodId, request);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "envelope.invalid", exception.getMessage());
            prepareForm(periodId, model);
            return "envelope/form";
        } catch (DataIntegrityViolationException exception) {
            bindingResult.reject(
                    "envelope.conflict",
                    "Không thể lưu phong bì. Hãy tải lại danh sách rồi thử lại.");
            prepareForm(periodId, model);
            return "envelope/form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã thêm phong bì. Bạn có thể phân bổ tiền ở bước tiếp theo.");

        return "redirect:/envelopes?periodId=" + periodId;
    }

    private void prepareForm(Long periodId, Model model) {
        model.addAttribute(
                "period", envelopeService.getPeriod(periodId));
        model.addAttribute(
                "categories",
                envelopeService.getAvailableCategories());
    }
}