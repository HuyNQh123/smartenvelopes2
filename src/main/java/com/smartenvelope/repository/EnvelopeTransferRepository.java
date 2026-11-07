package com.smartenvelope.repository;

import com.smartenvelope.entity.EnvelopeTransfer;
import com.smartenvelope.enums.TransferReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu với bảng EnvelopeTransfer (lịch sử điều chuyển phong bì).
 * Phụ trách bởi: Thành viên 3.
 */
@Repository
public interface EnvelopeTransferRepository extends JpaRepository<EnvelopeTransfer, Long> {

    /**
     * Tìm tất cả các giao dịch điều chuyển phong bì của một người dùng.
     */
    List<EnvelopeTransfer> findByUserId(Long userId);

    /**
     * Tìm các giao dịch điều chuyển theo lý do (COVER_EXPENSE hoặc REPLAN).
     */
    List<EnvelopeTransfer> findByReason(TransferReason reason);

    /**
     * Tìm các giao dịch điều chuyển liên quan đến một phong bì (nguồn hoặc đích).
     */
    List<EnvelopeTransfer> findBySourceEnvelopeIdOrTargetEnvelopeId(Long sourceEnvelopeId, Long targetEnvelopeId);
}
