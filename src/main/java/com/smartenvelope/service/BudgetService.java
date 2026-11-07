package com.smartenvelope.service;

import com.smartenvelope.dto.budget.*;
import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Service xử lý nghiệp vụ ngân sách và quy tắc tài chính (ví dụ quy tắc
 * 50/30/20, thiết lập hạn mức tổng).
 * Phụ trách bởi: Thành viên 4.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetPeriodRepository budgetRepository;
    private final EnvelopeRepository envelopeRepository;
    private final WorkspaceContextService workspaceContext;

    public List<BudgetResponse> getCurrentWorkspaceBudgets() {

        return budgetRepository.findByWorkspaceIdOrderByYearDescMonthDesc(
                workspaceContext.getCurrentWorkspaceId()).stream()
                .map(BudgetResponse::new)
                .toList();
    }

    public BudgetPeriod getBudget(Long id) {

        Long workspaceId = workspaceContext.getCurrentWorkspaceId();

        return budgetRepository.findById(id)
                .filter(budget -> budget.getWorkspace().getId().equals(workspaceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kỳ ngân sách."));
    }

    @Transactional
    public BudgetPeriod createBudget(BudgetRequest request) {

        requireEditorRole();

        Workspace workspace = workspaceContext.getCurrentWorkspace();

        if (budgetRepository.findByWorkspaceIdAndMonthAndYear(
                workspace.getId(), request.getMonth(), request.getYear()).isPresent()) {

            throw new IllegalArgumentException("Kỳ ngân sách này đã tồn tại.");
        }

        YearMonth month = YearMonth.of(request.getYear(), request.getMonth());

        BudgetPeriod budget = new BudgetPeriod(
                workspace, request.getName().trim(), month.getMonthValue(), month.getYear(),
                month.atDay(1), month.atEndOfMonth(), request.getTotalIncome());

        if (request.getRolloverPolicy() != null) {

            budget.setRolloverPolicy(request.getRolloverPolicy());
        }

        return budgetRepository.save(budget);
    }

    @Transactional
    public void confirmBudget(Long budgetId) {

        requireEditorRole();

        BudgetPeriod budget = getBudget(budgetId);

        if (budget.getStatus() != BudgetStatus.DRAFT) {

            throw new IllegalArgumentException("Chỉ có thể xác nhận kỳ ngân sách đang ở trạng thái nháp.");
        }

        BigDecimal allocated = envelopeRepository.findByBudgetPeriodId(budgetId).stream()
                .map(Envelope::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (allocated.compareTo(budget.getTotalIncome()) != 0) {

            throw new IllegalArgumentException("Cần phân bổ đúng bằng tổng thu nhập trước khi xác nhận.");
        }

        budget.setTotalAllocated(allocated);
        budget.setStatus(BudgetStatus.CONFIRMED);
    }

    private void requireEditorRole() {

        MemberRole role = workspaceContext.getCurrentUserRoleInWorkspace(
                workspaceContext.getCurrentWorkspaceId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền sửa kỳ ngân sách này."));

        if (role == MemberRole.VIEWER) {

            throw new AccessDeniedException("Quyền xem không cho phép tạo kỳ ngân sách.");
        }
    }
}
