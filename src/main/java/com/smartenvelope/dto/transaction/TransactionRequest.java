package com.smartenvelope.dto.transaction;

import com.smartenvelope.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO tiếp nhận dữ liệu tạo mới giao dịch thu chi hoặc chuyển tiền.
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
public class TransactionRequest {

    private Long id;

    @NotNull(message = "Tài khoản thanh toán không được để trống")
    private Long accountId;

    private Long envelopeId;

    private Long categoryId;

    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền giao dịch phải lớn hơn 0")
    private BigDecimal amount;

    private TransactionType type = TransactionType.EXPENSE;

    private LocalDateTime transactionDate;

    private String note;

    /**
     * Mã định danh yêu cầu (Idempotency Key / Request ID từ OCR hoặc giọng nói AI của TV5).
     */
    private String requestId;

    /**
     * ID phong bì nguồn dùng để bù tiền nếu khoản chi vượt quá số dư phong bì đích.
     */
    private Long coverEnvelopeId;

    /**
     * ID Quỹ chung (Workspace) nếu giao dịch này thuộc về một quỹ chung.
     */
    private Long workspaceId;

    public TransactionRequest() {
        this.transactionDate = LocalDateTime.now();
    }

    public TransactionRequest(Long accountId, Long envelopeId, Long categoryId, BigDecimal amount, String note) {
        this.accountId = accountId;
        this.envelopeId = envelopeId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = TransactionType.EXPENSE;
        this.transactionDate = LocalDateTime.now();
        this.note = note;
    }

    public TransactionRequest(Long accountId, Long envelopeId, Long categoryId, BigDecimal amount,
                              TransactionType type, LocalDateTime transactionDate, String note, String requestId) {
        this.accountId = accountId;
        this.envelopeId = envelopeId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type != null ? type : TransactionType.EXPENSE;
        this.transactionDate = transactionDate != null ? transactionDate : LocalDateTime.now();
        this.note = note;
        this.requestId = requestId;
    }

    public TransactionRequest(Long accountId, Long envelopeId, Long categoryId, Long coverEnvelopeId, BigDecimal amount, String note) {
        this.accountId = accountId;
        this.envelopeId = envelopeId;
        this.categoryId = categoryId;
        this.coverEnvelopeId = coverEnvelopeId;
        this.amount = amount;
        this.type = TransactionType.EXPENSE;
        this.transactionDate = LocalDateTime.now();
        this.note = note;
    }

    public TransactionRequest(Long accountId, Long envelopeId, Long categoryId, Long coverEnvelopeId,
                              Long workspaceId, BigDecimal amount, String note) {
        this.accountId = accountId;
        this.envelopeId = envelopeId;
        this.categoryId = categoryId;
        this.coverEnvelopeId = coverEnvelopeId;
        this.workspaceId = workspaceId;
        this.amount = amount;
        this.type = TransactionType.EXPENSE;
        this.transactionDate = LocalDateTime.now();
        this.note = note;
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(Long envelopeId) {
        this.envelopeId = envelopeId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getCoverEnvelopeId() {
        return coverEnvelopeId;
    }

    public void setCoverEnvelopeId(Long coverEnvelopeId) {
        this.coverEnvelopeId = coverEnvelopeId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }
}
