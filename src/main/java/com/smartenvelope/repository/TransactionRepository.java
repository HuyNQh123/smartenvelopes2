package com.smartenvelope.repository;

import com.smartenvelope.entity.Transaction;
import com.smartenvelope.enums.TransactionStatus;
import com.smartenvelope.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng transactions.
 * Phụ trách bởi: Thành viên 3 (Transaction & Ledger Processing).
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWorkspaceId(Long workspaceId);

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByEnvelopeId(Long envelopeId);

    List<Transaction> findByWorkspaceIdAndTransactionDateBetween(Long workspaceId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByWorkspaceIdAndType(Long workspaceId, TransactionType type);

    List<Transaction> findByWorkspaceIdAndStatus(Long workspaceId, TransactionStatus status);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT t FROM Transaction t WHERE t.workspace.id = :workspaceId ORDER BY t.transactionDate DESC, t.id DESC")
    List<Transaction> findRecentTransactions(@Param("workspaceId") Long workspaceId);
}
