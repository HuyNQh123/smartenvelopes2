package com.smartenvelope.service;

import com.smartenvelope.entity.*;
import com.smartenvelope.enums.*;
import com.smartenvelope.exception.ResourceNotFoundException;
import com.smartenvelope.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý kiểm tra và phát sinh cảnh báo (cảnh báo khi phong bì đạt 80%,
 * vượt hạn mức, nhắc nhở định kỳ).
 * Phụ trách bởi: Thành viên 4.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private static final int DEFAULT_WARNING_THRESHOLD = 80;

    private final CurrentUserService currentUserService;
    private final WorkspaceContextService workspaceContext;
    private final BudgetPeriodRepository budgetRepository;
    private final EnvelopeRepository envelopeRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public List<Notification> getNotifications() {

        refreshBudgetAlerts();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(
                currentUserService.getCurrentUserId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {

        Long userId = currentUserService.getCurrentUserId();

        Notification notification = notificationRepository.findById(notificationId)
                .filter(item -> item.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo."));

        notification.setStatus(NotificationStatus.READ);
    }

    /** Tạo tối đa một cảnh báo cho mỗi phong bì, loại và ngày. */
    @Transactional
    public void refreshBudgetAlerts() {

        User user = currentUserService.getCurrentUser();

        Long workspaceId = workspaceContext.getCurrentWorkspaceId();

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();

        List<Notification> existing = new ArrayList<>(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));

        List<BudgetPeriod> budgets = budgetRepository.findByWorkspaceIdOrderByYearDescMonthDesc(workspaceId);

        for (BudgetPeriod budget : budgets) {

            if (budget.getStatus() != BudgetStatus.CONFIRMED
                    || budget.getMonth() != today.getMonthValue() || budget.getYear() != today.getYear()) {

                continue;
            }

            for (Envelope envelope : envelopeRepository.findByBudgetPeriodId(budget.getId())) {

                NotificationType type = getAlertType(envelope);

                if (type == null) {

                    continue;
                }

                String title = type == NotificationType.OVERSPENT ? "Đã chạm hạn mức" : "Sắp chạm hạn mức";

                String message = String.format("Phong bì %s đã chi %s trên %s.", envelope.getName(),
                        envelope.getSpentAmount().toPlainString(), envelope.getAllocatedAmount().toPlainString());

                boolean alreadyCreated = existing.stream()
                        .anyMatch(item -> item.getType() == type && item.getTitle().equals(title)
                                && item.getMessage().equals(message) && item.getCreatedAt() != null
                                && !item.getCreatedAt().isBefore(startOfDay));

                if (!alreadyCreated) {

                    Notification notification = new Notification(user, type, title, message, "/budgets");

                    notification.setStatus(NotificationStatus.SENT);

                    existing.add(notificationRepository.save(notification));
                }
            }
        }
    }

    private NotificationType getAlertType(Envelope envelope) {

        BigDecimal allocated = envelope.getAllocatedAmount();

        BigDecimal spent = envelope.getSpentAmount();

        if (allocated == null || spent == null || spent.signum() <= 0) {

            return null;
        }
        if (allocated.signum() <= 0 || spent.compareTo(allocated) >= 0) {

            return NotificationType.OVERSPENT;
        }

        BigDecimal percentUsed = spent.multiply(BigDecimal.valueOf(100))
                .divide(allocated, 2, java.math.RoundingMode.DOWN);

        int threshold = envelope.getWarningThreshold() == null
                ? DEFAULT_WARNING_THRESHOLD
                : envelope.getWarningThreshold();

        return percentUsed.compareTo(BigDecimal.valueOf(threshold)) >= 0
                ? NotificationType.NEAR_LIMIT
                : null;
    }
}
