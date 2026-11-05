package com.smartenvelope.service;

import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartenvelope.dto.account.AccountRequest;
import com.smartenvelope.dto.account.AccountUpdateRequest;
import com.smartenvelope.entity.Account;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.AccountType;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.repository.AccountRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final WorkspaceContextService workspaceContextService;
    private final Validator validator;

    public AccountService(
            AccountRepository accountRepository,
            WorkspaceContextService workspaceContextService,
            Validator validator) {
        this.accountRepository = accountRepository;
        this.workspaceContextService = workspaceContextService;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsForCurrentWorkspace() {
        Long workspaceId = workspaceContextService.getCurrentWorkspaceId();

        return accountRepository.findByWorkspaceIdAndActive(
                workspaceId, true);
    }

    @Transactional
    public void createAccount(AccountRequest request) {
        Workspace workspace = workspaceContextService.getCurrentWorkspace();

        MemberRole role = workspaceContextService
                .getCurrentUserRoleInWorkspace(workspace.getId())
                .orElseThrow(()
                        -> new AccessDeniedException("Bạn không có quyền truy cập quỹ."));

        if (role != MemberRole.OWNER && role != MemberRole.MANAGER) {
            throw new AccessDeniedException(
                    "Chỉ chủ quỹ hoặc người quản lý được thêm tài khoản tiền.");
        }

        Set<ConstraintViolation<AccountRequest>> errors
                = validator.validate(request);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    errors.iterator().next().getMessage());
        }

        if (request.getType() != AccountType.CASH
                && request.getType() != AccountType.BANK) {
            throw new IllegalArgumentException(
                    "Hiện chỉ hỗ trợ tài khoản tiền mặt và ngân hàng.");
        }

        Account account = new Account();
        account.setWorkspace(workspace);
        account.setName(request.getName().strip());
        account.setType(request.getType());
        account.setBalance(
                request.getInitialBalance().setScale(0, RoundingMode.UNNECESSARY));
        account.setCurrency("VND");

        accountRepository.save(account);
    }

    private Account getAccountForEditing(Long accountId) {
        Long workspaceId = workspaceContextService.getCurrentWorkspaceId();

        MemberRole role = workspaceContextService
                .getCurrentUserRoleInWorkspace(workspaceId)
                .orElseThrow(()
                        -> new AccessDeniedException("Bạn không có quyền truy cập quỹ."));

        if (role != MemberRole.OWNER && role != MemberRole.MANAGER) {
            throw new AccessDeniedException(
                    "Chỉ chủ quỹ hoặc người quản lý được sửa tài khoản tiền.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()
                        -> new AccessDeniedException(
                                "Tài khoản không tồn tại hoặc bạn không có quyền truy cập."));

        if (!workspaceId.equals(account.getWorkspace().getId())) {
            throw new AccessDeniedException(
                    "Tài khoản không tồn tại hoặc bạn không có quyền truy cập.");
        }

        return account;
    }

    @Transactional(readOnly = true)
    public AccountUpdateRequest getAccountUpdateRequest(Long accountId) {
        Account account = getAccountForEditing(accountId);

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setName(account.getName());
        request.setType(account.getType());

        return request;
    }

    @Transactional
    public void updateAccount(Long accountId, AccountUpdateRequest request) {
        Account account = getAccountForEditing(accountId);

        Set<ConstraintViolation<AccountUpdateRequest>> errors
                = validator.validate(request);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    errors.iterator().next().getMessage());
        }

        if (request.getType() != AccountType.CASH
                && request.getType() != AccountType.BANK) {
            throw new IllegalArgumentException(
                    "Hiện chỉ hỗ trợ tài khoản tiền mặt và ngân hàng.");
        }

        account.setName(request.getName().strip());
        account.setType(request.getType());

        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> getInactiveAccountsForCurrentWorkspace() {
        Long workspaceId = workspaceContextService.getCurrentWorkspaceId();

        return accountRepository.findByWorkspaceIdAndActive(
                workspaceId, false);
    }

    @Transactional
    public void deactivateAccount(Long accountId) {
        Account account = getAccountForEditing(accountId);

        account.setActive(false);
        accountRepository.save(account);
    }

    @Transactional
    public void reactivateAccount(Long accountId) {
        Account account = getAccountForEditing(accountId);

        account.setActive(true);
        accountRepository.save(account);
    }
}
