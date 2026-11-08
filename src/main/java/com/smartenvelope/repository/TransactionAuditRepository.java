package com.smartenvelope.repository;

import com.smartenvelope.entity.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng transaction_audits. Phụ trách bởi: Thành
 * viên 3 (Transaction & Ledger Processing).
 */
@Repository
public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {

    List<TransactionAudit> findByTransactionIdOrderByChangedAtDesc(Long transactionId);

    List<TransactionAudit> findByChangedById(Long changedById);
}
