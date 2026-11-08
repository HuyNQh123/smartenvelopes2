package com.smartenvelope.service;

import com.smartenvelope.dto.envelope.EnvelopeAllocationRequest;
import com.smartenvelope.entity.BudgetPeriod;
import com.smartenvelope.entity.Envelope;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.enums.MemberRole;
import com.smartenvelope.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnvelopeActualIncomeTest {
    @Test void expectedIncomeCannotFundAllocation() {
        var periods = mock(BudgetPeriodRepository.class);
        var envelopes = mock(EnvelopeRepository.class);
        var context = mock(WorkspaceContextService.class);
        var workspace = mock(Workspace.class);
        when(workspace.getId()).thenReturn(1L);
        when(context.getCurrentWorkspaceId()).thenReturn(1L);
        when(context.getCurrentUserRoleInWorkspace(1L)).thenReturn(Optional.of(MemberRole.OWNER));
        var period = new BudgetPeriod(); period.setId(2L); period.setWorkspace(workspace);
        period.setExpectedIncome(new BigDecimal("1000000"));
        var envelope = new Envelope(); envelope.setId(3L);
        when(periods.findById(2L)).thenReturn(Optional.of(period));
        when(envelopes.findByBudgetPeriodId(2L)).thenReturn(List.of(envelope));
        var request = new EnvelopeAllocationRequest(); request.setAmount(BigDecimal.ONE); request.setPeriodVersion(period.getVersion());
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var service = new EnvelopeService(envelopes, periods, mock(CategoryRepository.class), context,
                    mock(CurrentUserService.class), factory.getValidator(), mock(EntityManager.class));
            assertThrows(IllegalArgumentException.class, () -> service.allocateEnvelope(2L, 3L, request));
            assertEquals(0, period.getTotalAllocated().compareTo(BigDecimal.ZERO));
            assertEquals(0, envelope.getAllocatedAmount().compareTo(BigDecimal.ZERO));
        }
    }
}
