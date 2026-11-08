package com.smartenvelope.service;

import com.smartenvelope.dto.savings.*;
import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service xử lý nghiệp vụ mục tiêu tiết kiệm và tích lũy tiền vào heo đất điện
 * tử. Phụ trách bởi: Thành viên 4.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final PiggyBankRepository piggyBankRepository;
    private final PiggyBankEntryRepository entryRepository;
    private final WorkspaceContextService workspaceContext;

    public List<SavingsGoalResponse> getGoals() {

        return goalRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId()).stream()
                .map(SavingsGoalResponse::new).toList();
    }

    public BigDecimal getPiggyBankBalance() {

        return piggyBankRepository.findByWorkspaceId(workspaceContext.getCurrentWorkspaceId())
                .map(PiggyBank::getTotalBalance).orElse(BigDecimal.ZERO);
    }

    @Transactional
    public SavingsGoal createGoal(SavingsGoalRequest request) {

        requireEditorRole();

        BigDecimal initialAmount = request.initialAmount() == null ? BigDecimal.ZERO
                : request.initialAmount();

        if (initialAmount.compareTo(request.targetAmount()) > 0) {

            throw new IllegalArgumentException("Số tiền ban đầu không được lớn hơn mục tiêu.");
        }

        Workspace workspace = workspaceContext.getCurrentWorkspace();

        SavingsGoal goal = new SavingsGoal(workspace, request.name().trim(), request.targetAmount(),
                request.targetDate());

        goal.setCurrentAmount(initialAmount);

        if (initialAmount.compareTo(request.targetAmount()) == 0) {

            goal.setStatus(SavingsGoalStatus.COMPLETED);
        }

        goal = goalRepository.save(goal);

        if (initialAmount.signum() > 0) {

            addToPiggyBank(workspace, initialAmount, goal);
        }

        return goal;
    }

    @Transactional
    public void deposit(Long goalId, BigDecimal amount) {

        requireEditorRole();

        SavingsGoal goal = getGoalInCurrentWorkspace(goalId);

        if (amount == null || amount.signum() <= 0) {

            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0.");
        }

        if (goal.getStatus() != SavingsGoalStatus.ACTIVE) {

            throw new IllegalArgumentException("Chỉ có thể nạp vào mục tiêu đang hoạt động.");
        }

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);

        if (newAmount.compareTo(goal.getTargetAmount()) > 0) {

            throw new IllegalArgumentException(
                    "Số tiền nạp vượt quá số tiền còn thiếu của mục tiêu.");
        }

        goal.setCurrentAmount(newAmount);

        if (newAmount.compareTo(goal.getTargetAmount()) == 0) {

            goal.setStatus(SavingsGoalStatus.COMPLETED);
        }

        addToPiggyBank(goal.getWorkspace(), amount, goal);
    }

    private SavingsGoal getGoalInCurrentWorkspace(Long goalId) {

        Long workspaceId = workspaceContext.getCurrentWorkspaceId();

        return goalRepository.findById(goalId)
                .filter(goal -> goal.getWorkspace().getId().equals(workspaceId)).orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy mục tiêu tiết kiệm."));
    }

    private void requireEditorRole() {

        MemberRole role = workspaceContext
                .getCurrentUserRoleInWorkspace(workspaceContext.getCurrentWorkspaceId())
                .orElseThrow(() -> new AccessDeniedException(
                        "Bạn không có quyền sửa dữ liệu tiết kiệm."));

        if (role == MemberRole.VIEWER) {

            throw new AccessDeniedException("Quyền xem không cho phép sửa dữ liệu tiết kiệm.");
        }
    }

    private void addToPiggyBank(Workspace workspace, BigDecimal amount, SavingsGoal goal) {

        PiggyBank bank = piggyBankRepository.findByWorkspaceId(workspace.getId()).orElseGet(
                () -> piggyBankRepository.save(new PiggyBank(workspace, BigDecimal.ZERO)));

        bank.setTotalBalance(bank.getTotalBalance().add(amount));

        entryRepository.save(new PiggyBankEntry(bank, amount, PiggyBankEntryType.MANUAL_DEPOSIT,
                null, goal, "Nạp cho mục tiêu: " + goal.getName()));
    }
}
