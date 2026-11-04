package com.smartenvelope.controller.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smartenvelope.dto.account.AccountRequest;
import com.smartenvelope.service.AccountService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute(
                "accounts",
                accountService.getAccountsForCurrentWorkspace()
        );

        return "account/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("accountRequest", new AccountRequest());

        return "account/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("accountRequest") AccountRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "account/form";
        }

        try {
            accountService.createAccount(request);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("account.invalid", exception.getMessage());
            return "account/form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Thêm tài khoản tiền thành công!"
        );

        return "redirect:/accounts";
    }
}