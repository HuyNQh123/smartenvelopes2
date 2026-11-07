package com.smartenvelope.repository;

import com.smartenvelope.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu với bảng Transaction.
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Tìm giao dịch theo mã requestId (dùng để chống click đúp / idempotency).
     *
     * @param requestId Mã định danh request
     * @return Giao dịch nếu đã tồn tại
     */
    Optional<Transaction> findByRequestId(String requestId);

    /**
     * Kiểm tra sự tồn tại của giao dịch theo requestId.
     */
    boolean existsByRequestId(String requestId);

    /**
     * Tìm danh sách giao dịch của một người dùng.
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Phân trang danh sách giao dịch của một người dùng.
     */
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    /**
     * Tìm danh sách giao dịch phát sinh từ một tài khoản thanh toán.
     */
    List<Transaction> findByAccountId(Long accountId);

    /**
     * Tìm danh sách giao dịch phát sinh từ một phong bì ngân sách.
     */
    List<Transaction> findByEnvelopeId(Long envelopeId);
}
