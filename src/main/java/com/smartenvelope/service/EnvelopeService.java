package com.smartenvelope.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartenvelope.dto.envelope.EnvelopeAllocationRequest;
import com.smartenvelope.dto.envelope.EnvelopeRequest;
import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.entity.Category;
import com.smartenvelope.entity.Envelope;
import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.CategoryType;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.repository.BudgetPeriodRepository;
import com.smartenvelope.repository.CategoryRepository;
import com.smartenvelope.repository.EnvelopeRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.Validator;

@Service
public class EnvelopeService {

    private final EnvelopeRepository envelopeRepository;
    private final BudgetPeriodRepository budgetPeriodRepository;
    private final CategoryRepository categoryRepository;
    private final WorkspaceContextService workspaceContextService;
    private final CurrentUserService currentUserService;
    private final Validator validator;
    private final EntityManager entityManager;

    public EnvelopeService(
            EnvelopeRepository envelopeRepository,
            BudgetPeriodRepository budgetPeriodRepository,
            CategoryRepository categoryRepository,
            WorkspaceContextService workspaceContextService,
            CurrentUserService currentUserService,
            Validator validator,
            EntityManager entityManager) {

        this.envelopeRepository = envelopeRepository;
        this.budgetPeriodRepository = budgetPeriodRepository;
        this.categoryRepository = categoryRepository;
        this.workspaceContextService = workspaceContextService;
        this.currentUserService = currentUserService;
        this.validator = validator;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public BudgetPeriod getPeriod(Long periodId) {
        Long workspaceId
                = workspaceContextService.getCurrentWorkspaceId();

        BudgetPeriod period = budgetPeriodRepository.findById(periodId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Kỳ không tồn tại hoặc bạn không có quyền truy cập."));

        if (!workspaceId.equals(period.getWorkspace().getId())) {
            throw new AccessDeniedException(
                    "Kỳ không tồn tại hoặc bạn không có quyền truy cập.");
        }

        return period;
    }

    @Transactional(readOnly = true)
    public List<Envelope> getEnvelopes(Long periodId) {
        getPeriod(periodId);

        return envelopeRepository.findByBudgetPeriodId(periodId);
    }

    @Transactional(readOnly = true)
    public List<Category> getAvailableCategories() {
        Long userId = currentUserService.getCurrentUserId();

        return categoryRepository
                .findAllAvailableForUserAndType(
                        userId, CategoryType.EXPENSE)
                .stream()
                .filter(category
                        -> Boolean.TRUE.equals(category.getIsActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean canManageCurrentWorkspace() {
        Long workspaceId
                = workspaceContextService.getCurrentWorkspaceId();

        return workspaceContextService
                .getCurrentUserRoleInWorkspace(workspaceId)
                .map(role
                        -> role == MemberRole.OWNER
                        || role == MemberRole.MANAGER)
                .orElse(false);
    }

    @Transactional
    public void createEnvelope(
            Long periodId,
            EnvelopeRequest request) {

        BudgetPeriod period = getPeriod(periodId);

        if (!canManageCurrentWorkspace()) {
            throw new AccessDeniedException(
                    "Chỉ chủ quỹ hoặc người quản lý được thêm phong bì.");
        }

        // Tuần tự hóa thao tác thêm phong bì vào cùng một kỳ.
        entityManager.refresh(
                period, LockModeType.PESSIMISTIC_WRITE);

        if (period.getStatus() != BudgetStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "Chỉ được thêm phong bì vào kỳ đang nháp.");
        }

        var errors = validator.validate(request);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    errors.iterator().next().getMessage());
        }

        // Chỉ nhận danh mục chi tiêu đang hoạt động,
        // thuộc hệ thống hoặc thuộc người dùng hiện tại.
        Category category = getAvailableCategories()
                .stream()
                .filter(item
                        -> item.getId().equals(request.getCategoryId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Danh mục không hợp lệ hoặc đã ngừng sử dụng."));

        if (envelopeRepository
                .findByBudgetPeriodIdAndCategoryId(
                        periodId, category.getId())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Kỳ này đã có phong bì cho danh mục đã chọn.");
        }

        Envelope envelope = new Envelope();
        envelope.setBudgetPeriod(period);
        envelope.setCategory(category);
        envelope.setName(request.getName().strip());
        envelope.setAllocatedAmount(BigDecimal.ZERO);
        envelope.setSpentAmount(BigDecimal.ZERO);
        envelope.setRemainingAmount(BigDecimal.ZERO);
        envelope.setWarningThreshold(80);

        envelopeRepository.saveAndFlush(envelope);
    }

    @Transactional
    public void allocateEnvelope(
            Long periodId,
            Long envelopeId,
            EnvelopeAllocationRequest request) {

        BudgetPeriod period = getPeriod(periodId);

        if (!canManageCurrentWorkspace()) {
            throw new AccessDeniedException(
                    "Chỉ chủ quỹ hoặc người quản lý được phân bổ tiền.");
        }

        entityManager.refresh(
                period, LockModeType.PESSIMISTIC_WRITE);

        if (period.getStatus() != BudgetStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "Chỉ được phân bổ ở kỳ đang nháp.");
        }

        var errors = validator.validate(request);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    errors.iterator().next().getMessage());
        }

        if (!period.getVersion().equals(request.getPeriodVersion())) {
            throw new IllegalArgumentException(
                    "Kế hoạch đã thay đổi. Hãy tải lại trang rồi nhập lại.");
        }

        List<Envelope> envelopes
                = envelopeRepository.findByBudgetPeriodId(periodId);

        Envelope selected = envelopes.stream()
                .filter(item -> item.getId().equals(envelopeId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException(
                        "Phong bì không thuộc kỳ đang chọn."));

        // Giai đoạn này chỉ hỗ trợ lập kế hoạch trước khi có chi tiêu.
        boolean hasSpending = envelopes.stream()
                .anyMatch(item
                        -> item.getSpentAmount().signum() != 0);

        if (hasSpending) {
            throw new IllegalArgumentException(
                    "Kỳ đã có chi tiêu. Chức năng này chỉ dùng cho nháp ban đầu.");
        }

        BigDecimal amount = request.getAmount().setScale(0);

        BigDecimal otherAllocated = envelopes.stream()
                .filter(item -> !item.getId().equals(envelopeId))
                .map(Envelope::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = otherAllocated.add(amount);

        // Hiện chỉ sử dụng thu nhập đã ghi nhận.
        // Tiền chuyển tiếp cần tích hợp với module đóng kỳ.
        BigDecimal available = period.getTotalIncome();

        if (newTotal.compareTo(available) > 0) {
            BigDecimal maximumForSelected
                    = available.subtract(otherAllocated);

            throw new IllegalArgumentException(
                    "Vượt tiền thu nhập đã ghi nhận của kỳ. "
                    + "Mức tối đa cho phong bì này là "
                    + maximumForSelected.toPlainString()
                    + " VND.");
        }

        selected.setAllocatedAmount(amount);
        selected.setRemainingAmount(amount);

        period.setTotalAllocated(newTotal);

        envelopeRepository.saveAndFlush(selected);
        budgetPeriodRepository.saveAndFlush(period);
    }
}
