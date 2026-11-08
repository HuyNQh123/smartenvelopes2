package com.smartenvelope.service;

import com.smartenvelope.dto.budget.BudgetRequest;
import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.BudgetStatus;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.repository.BudgetPeriodRepository;
import com.smartenvelope.repository.EnvelopeRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BudgetIncomeIntegrationTest {
    @Test void expectedIncomeDoesNotCreateRealIncome() {
        var repo = mock(BudgetPeriodRepository.class);
        var context = mock(WorkspaceContextService.class);
        var workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(42L);
        when(context.getCurrentWorkspace()).thenReturn(workspace);
        when(context.getCurrentWorkspaceId()).thenReturn(42L);
        when(context.getCurrentUserRoleInWorkspace(42L)).thenReturn(Optional.of(MemberRole.OWNER));
        when(repo.findByWorkspaceIdAndMonthAndYear(42L, 1, 2027)).thenReturn(Optional.empty());
        when(repo.save(any(BudgetPeriod.class))).thenAnswer(i -> i.getArgument(0));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new BudgetService(repo, mock(EnvelopeRepository.class), context, mock(EntityManager.class), factory.getValidator());
            var period = service.createBudget(new BudgetRequest("Tháng một", 1, 2027, new BigDecimal("1500000"), null));
            assertEquals(0, period.getExpectedIncome().compareTo(new BigDecimal("1500000")));
            assertEquals(0, period.getTotalIncome().compareTo(BigDecimal.ZERO));
            assertEquals(0, period.getTotalAllocated().compareTo(BigDecimal.ZERO));
            assertEquals(BudgetStatus.DRAFT, period.getStatus());
        }
    }
    @Test void viewerCannotCreateBudget() {
        var context = mock(WorkspaceContextService.class);
        when(context.getCurrentWorkspaceId()).thenReturn(42L);
        when(context.getCurrentUserRoleInWorkspace(42L)).thenReturn(Optional.of(MemberRole.VIEWER));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new BudgetService(mock(BudgetPeriodRepository.class), mock(EnvelopeRepository.class), context, mock(EntityManager.class), factory.getValidator());
            assertThrows(AccessDeniedException.class, () -> service.createBudget(new BudgetRequest("Tháng một", 1, 2027, BigDecimal.ZERO, null)));
        }
    }
    @Test void negativeExpectedIncomeIsInvalid() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(new BudgetRequest("Tháng một", 1, 2027, BigDecimal.valueOf(-1), null));
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("expectedIncome")));
        }
    }
    @Test void confirmRequiresAllRealIncomeAllocated() {
        var repo = mock(BudgetPeriodRepository.class);
        var envelopes = mock(EnvelopeRepository.class);
        var context = mock(WorkspaceContextService.class);
        var workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(42L);
        when(context.getCurrentWorkspaceId()).thenReturn(42L);
        when(context.getCurrentUserRoleInWorkspace(42L)).thenReturn(Optional.of(MemberRole.OWNER));
        var period = new BudgetPeriod(); period.setId(7L); period.setWorkspace(workspace);
        period.setTotalIncome(BigDecimal.TEN);
        when(repo.findById(7L)).thenReturn(Optional.of(period));
        when(envelopes.findByBudgetPeriodId(7L)).thenReturn(java.util.List.of());
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new BudgetService(repo, envelopes, context, mock(EntityManager.class), factory.getValidator());
            assertThrows(IllegalArgumentException.class, () -> service.confirmBudget(7L));
            assertEquals(BudgetStatus.DRAFT, period.getStatus());
        }
    }
}
