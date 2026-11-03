package com.smartenvelope.repository;

import com.smartenvelope.entity.EnvelopeTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng envelope_transfers.
 * Phụ trách bởi: Thành viên 3 (Transaction & Ledger Processing).
 */
@Repository
public interface EnvelopeTransferRepository extends JpaRepository<EnvelopeTransfer, Long> {

    List<EnvelopeTransfer> findByBudgetPeriodId(Long budgetPeriodId);

    List<EnvelopeTransfer> findByFromEnvelopeId(Long fromEnvelopeId);

    List<EnvelopeTransfer> findByToEnvelopeId(Long toEnvelopeId);
}

