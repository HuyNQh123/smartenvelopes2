package com.smartenvelope.dto.transaction;

import com.smartenvelope.entity.Transaction;
import com.smartenvelope.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO chứa dữ liệu giao dịch trả về cho View và API:
 * id, amount, type, transactionDate, note, accountName, categoryName, envelopeName.
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime transactionDate;
    private String note;
    private String accountName;
    private String categoryName;
    private String envelopeName;

    // Các trường hỗ trợ thông tin bù tiền chi vượt (Cover Engine)
    private boolean coverTransferred;
    private BigDecimal coverAmount;
    private String coverEnvelopeName;

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, BigDecimal amount, TransactionType type, LocalDateTime transactionDate,
                               String note, String accountName, String categoryName, String envelopeName) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.transactionDate = transactionDate;
        this.note = note;
        this.accountName = accountName;
        this.categoryName = categoryName;
        this.envelopeName = envelopeName;
    }

    /**
     * Phương thức tiện ích ánh xạ từ Entity Transaction sang TransactionResponse DTO.
     *
     * @param tx Thực thể Transaction
     * @return Đối tượng DTO tương ứng
     */
    public static TransactionResponse fromEntity(Transaction tx) {
        if (tx == null) {
            return null;
        }
        TransactionResponse res = new TransactionResponse();
        res.setId(tx.getId());
        res.setAmount(tx.getAmount());
        res.setType(tx.getType());
        res.setTransactionDate(tx.getTransactionDate());
        res.setNote(tx.getNote());

        if (tx.getAccount() != null) {
            res.setAccountName(tx.getAccount().getName());
        }
        if (tx.getCategory() != null) {
            res.setCategoryName(tx.getCategory().getName());
        }
        if (tx.getEnvelope() != null) {
            res.setEnvelopeName(tx.getEnvelope().getName());
        }

        if (tx.getRelatedTransfer() != null) {
            res.setCoverTransferred(true);
            res.setCoverAmount(tx.getRelatedTransfer().getAmount());
            if (tx.getRelatedTransfer().getSourceEnvelope() != null) {
                res.setCoverEnvelopeName(tx.getRelatedTransfer().getSourceEnvelope().getName());
            }
        }
        return res;
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getEnvelopeName() {
        return envelopeName;
    }

    public void setEnvelopeName(String envelopeName) {
        this.envelopeName = envelopeName;
    }

    public boolean isCoverTransferred() {
        return coverTransferred;
    }

    public void setCoverTransferred(boolean coverTransferred) {
        this.coverTransferred = coverTransferred;
    }

    public BigDecimal getCoverAmount() {
        return coverAmount;
    }

    public void setCoverAmount(BigDecimal coverAmount) {
        this.coverAmount = coverAmount;
    }

    public String getCoverEnvelopeName() {
        return coverEnvelopeName;
    }

    public void setCoverEnvelopeName(String coverEnvelopeName) {
        this.coverEnvelopeName = coverEnvelopeName;
    }
}
