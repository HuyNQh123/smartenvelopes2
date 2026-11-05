package com.smartenvelope.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartenvelope.dto.budget.BudgetPeriodRequest;
import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.repository.BudgetPeriodRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class BudgetService {

    private final BudgetPeriodRepository budgetPeriodRepository;
    private final WorkspaceContextService workspaceContextService;
    private final Validator validator;

    public BudgetService(
            BudgetPeriodRepository budgetPeriodRepository,
            WorkspaceContextService workspaceContextService,
            Validator validator) {

        this.budgetPeriodRepository = budgetPeriodRepository;
        this.workspaceContextService = workspaceContextService;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<BudgetPeriod> getPeriodsForCurrentWorkspace() {
        Long workspaceId =
                workspaceContextService.getCurrentWorkspaceId();

        return budgetPeriodRepository.findByWorkspaceId(workspaceId)
                .stream()
                .sorted(
                        Comparator.comparing(BudgetPeriod::getYear)
                                .thenComparing(BudgetPeriod::getMonth)
                                .reversed()
                )
                .toList();
    }

    @Transactional
    public void createPeriod(BudgetPeriodRequest request) {
        Workspace workspace =
                workspaceContextService.getCurrentWorkspace();

        MemberRole role = workspaceContextService
                .getCurrentUserRoleInWorkspace(workspace.getId())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Bạn không có quyền truy cập quỹ."));

        if (role != MemberRole.OWNER && role != MemberRole.MANAGER) {
            throw new AccessDeniedException(
                    "Chỉ chủ quỹ hoặc người quản lý được tạo kỳ ngân sách.");
        }

        Set<ConstraintViolation<BudgetPeriodRequest>> errors =
                validator.validate(request);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    errors.iterator().next().getMessage());
        }

        boolean exists = budgetPeriodRepository
                .findByWorkspaceIdAndMonthAndYear(
                        workspace.getId(),
                        request.getMonth(),
                        request.getYear()
                )
                .isPresent();

        if (exists) {
            throw new IllegalArgumentException(
                    "Quỹ này đã có kỳ ngân sách cho tháng và năm đã chọn.");
        }

        YearMonth selectedMonth = YearMonth.of(
                request.getYear(),
                request.getMonth()
        );

        BudgetPeriod period = new BudgetPeriod();
        period.setWorkspace(workspace);
        period.setName(
                "Ngân sách tháng "
                        + request.getMonth()
                        + "/"
                        + request.getYear()
        );
        period.setMonth(request.getMonth());
        period.setYear(request.getYear());
        period.setStartDate(selectedMonth.atDay(1));
        period.setEndDate(selectedMonth.atEndOfMonth());
        period.setStatus(BudgetStatus.DRAFT);
        period.setTotalIncome(BigDecimal.ZERO);
        period.setTotalAllocated(BigDecimal.ZERO);

        budgetPeriodRepository.saveAndFlush(period);
    }
}