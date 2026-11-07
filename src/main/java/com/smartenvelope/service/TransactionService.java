package com.smartenvelope.service;

import com.smartenvelope.dto.transaction.TransactionRequest;
import com.smartenvelope.entity.Account;
import com.smartenvelope.entity.Category;
import com.smartenvelope.entity.Envelope;
import com.smartenvelope.entity.EnvelopeTransfer;
import com.smartenvelope.entity.Transaction;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.WorkspaceMember;
import com.smartenvelope.enums.TransactionType;
import com.smartenvelope.enums.TransferReason;
import com.smartenvelope.enums.WorkspaceRole;
import com.smartenvelope.exception.BusinessException;
import com.smartenvelope.exception.InsufficientBalanceException;
import com.smartenvelope.exception.ResourceAccessDeniedException;
import com.smartenvelope.repository.AccountRepository;
import com.smartenvelope.repository.CategoryRepository;
import com.smartenvelope.repository.EnvelopeRepository;
import com.smartenvelope.repository.EnvelopeTransferRepository;
import com.smartenvelope.repository.TransactionRepository;
import com.smartenvelope.repository.WorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service xử lý nghiệp vụ ghi chép giao dịch tài chính (thêm thu/chi, bù chi vượt Cover Engine,
 * điều chuyển phong bì, kiểm soát quyền Quỹ chung và quản lý giao dịch).
 * Phụ trách bởi: Thành viên 3 (Quang).
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final EnvelopeRepository envelopeRepository;
    private final EnvelopeTransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              EnvelopeRepository envelopeRepository,
                              EnvelopeTransferRepository transferRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository) {
        this(transactionRepository, envelopeRepository, transferRepository, accountRepository, categoryRepository, null);
    }

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              EnvelopeRepository envelopeRepository,
                              EnvelopeTransferRepository transferRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              WorkspaceMemberRepository workspaceMemberRepository) {
        this.transactionRepository = transactionRepository;
        this.envelopeRepository = envelopeRepository;
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    /**
     * Điều chuyển tiền tự do giữa hai phong bì ngân sách (TransactionType.TRANSFER).
     * Lưu ý sống còn: Điều chuyển phong bì KHÔNG ĐƯỢC làm thay đổi số dư Account!
     *
     * @param sourceId ID phong bì nguồn
     * @param targetId ID phong bì đích
     * @param amount   Số tiền cần chuyển
     * @param user     Người thực hiện giao dịch
     * @return Bản ghi lịch sử điều chuyển phong bì (EnvelopeTransfer)
     */
    @Transactional(rollbackFor = Exception.class)
    public EnvelopeTransfer transferEnvelopes(Long sourceId, Long targetId, BigDecimal amount, User user) {
        if (sourceId == null || targetId == null) {
            throw new BusinessException("ID phong bì nguồn và đích không được để trống");
        }

        if (sourceId.equals(targetId)) {
            throw new BusinessException("Phong bì nguồn và đích không được trùng nhau");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền chuyển phải lớn hơn 0");
        }

        Envelope source = envelopeRepository.findById(sourceId)
                .orElseThrow(() -> new BusinessException("Phong bì nguồn không tồn tại"));
        Envelope target = envelopeRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException("Phong bì đích không tồn tại"));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Phong bì nguồn không đủ tiền để chuyển");
        }

        source.spend(amount);
        target.allocate(amount);
        envelopeRepository.save(source);
        envelopeRepository.save(target);

        EnvelopeTransfer transfer = new EnvelopeTransfer();
        transfer.setSourceEnvelope(source);
        transfer.setTargetEnvelope(target);
        transfer.setAmount(amount);
        transfer.setReason(TransferReason.REPLAN);
        transfer.setUser(user);
        transfer.setTransferredAt(LocalDateTime.now());
        return transferRepository.save(transfer);
    }

    /**
     * Alias method cho transferEnvelopes.
     */
    @Transactional(rollbackFor = Exception.class)
    public EnvelopeTransfer transferBetweenEnvelopes(Long sourceId, Long targetId, BigDecimal amount, User user) {
        return transferEnvelopes(sourceId, targetId, amount, user);
    }

    /**
     * Ghi nhận giao dịch thu nhập (INCOME) - Mục 3.3.
     * Cộng tiền vào tài khoản thanh toán và lưu thông tin giao dịch.
     *
     * @param req         DTO yêu cầu tạo giao dịch
     * @param currentUser Người dùng thực hiện
     * @return Bản ghi giao dịch Transaction đã được lưu
     */
    @Transactional(rollbackFor = Exception.class)
    public Transaction recordIncome(TransactionRequest req, User currentUser) {
        if (req == null) {
            throw new BusinessException("Yêu cầu giao dịch không hợp lệ");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thu nhập phải lớn hơn 0");
        }
        Account account = accountRepository.findById(req.getAccountId())
            .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại"));
        account.credit(req.getAmount());
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setUser(currentUser);
        tx.setAccount(account);
        tx.setAmount(req.getAmount());
        tx.setType(TransactionType.INCOME);
        tx.setTransactionDate(req.getTransactionDate() != null ? req.getTransactionDate() : LocalDateTime.now());
        tx.setNote(req.getNote());
        if (req.getCategoryId() != null) {
            tx.setCategory(categoryRepository.findById(req.getCategoryId()).orElse(null));
        }
        if (req.getRequestId() != null) {
            tx.setRequestId(req.getRequestId());
        }
        return transactionRepository.save(tx);
    }

    /**
     * Overload hàm recordIncome(TransactionRequest req) không cần truyền user.
     */
    @Transactional(rollbackFor = Exception.class)
    public Transaction recordIncome(TransactionRequest req) {
        return recordIncome(req, null);
    }

    /**
     * Ghi nhận giao dịch chi tiêu tiêu chuẩn (EXPENSE) - Mục 4.1.
     * Quy trình:
     * 1. Kiểm tra Account đủ tiền
     * 2. Kiểm tra Envelope đủ tiền
     * 3. Trừ Account và Envelope
     * 4. Lưu Transaction với type = EXPENSE
     *
     * @param req         DTO yêu cầu giao dịch
     * @param currentUser Người dùng thực hiện
     * @return Bản ghi giao dịch Transaction đã được lưu
     */
    @Transactional(rollbackFor = Exception.class)
    public Transaction recordStandardExpense(TransactionRequest req, User currentUser) {
        if (req == null) {
            throw new BusinessException("Yêu cầu giao dịch không hợp lệ");
        }
        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền chi tiêu phải lớn hơn 0");
        }

        // 5.1. Chống click đúp (Idempotency): Nếu requestId != null và đã tồn tại trong DB thì trả về giao dịch cũ
        if (req.getRequestId() != null && !req.getRequestId().trim().isEmpty()) {
            Optional<Transaction> existingTx = transactionRepository.findByRequestId(req.getRequestId());
            if (existingTx.isPresent()) {
                return existingTx.get();
            }
        }

        // 1. Kiểm tra Account đủ tiền
        Account account = accountRepository.findById(req.getAccountId())
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại"));

        if (account.getBalance().compareTo(req.getAmount()) < 0) {
            throw new InsufficientBalanceException("Số dư tài khoản không đủ để thanh toán");
        }

        // 2. Kiểm tra Envelope đủ tiền
        Envelope envelope = envelopeRepository.findById(req.getEnvelopeId())
                .orElseThrow(() -> new BusinessException("Phong bì không tồn tại"));

        if (envelope.getBalance().compareTo(req.getAmount()) < 0) {
            throw new InsufficientBalanceException("Số dư phong bì không đủ để chi tiêu");
        }

        // 3. Trừ Account và Envelope
        account.debit(req.getAmount());
        envelope.spend(req.getAmount());

        accountRepository.save(account);
        envelopeRepository.save(envelope);

        // 4. Lưu Transaction với type = EXPENSE
        Transaction tx = new Transaction();
        tx.setUser(currentUser);
        tx.setAccount(account);
        tx.setEnvelope(envelope);
        if (req.getCategoryId() != null) {
            tx.setCategory(categoryRepository.findById(req.getCategoryId()).orElse(null));
        }
        tx.setAmount(req.getAmount());
        tx.setType(TransactionType.EXPENSE);
        tx.setTransactionDate(req.getTransactionDate() != null ? req.getTransactionDate() : LocalDateTime.now());
        tx.setNote(req.getNote());
        if (req.getRequestId() != null) {
            tx.setRequestId(req.getRequestId());
        }

        return transactionRepository.save(tx);
    }

    /**
     * Overload hàm recordStandardExpense(TransactionRequest req) không cần truyền user.
     */
    @Transactional(rollbackFor = Exception.class)
    public Transaction recordStandardExpense(TransactionRequest req) {
        return recordStandardExpense(req, null);
    }

    /**
     * Kiểm tra quyền thành viên trong Quỹ chung (Workspace) - Mục 5.1 & Test M3-10.
     * Nếu role = VIEWER -> chặn và ném ResourceAccessDeniedException (HTTP 403).
     *
     * @param workspaceId ID quỹ chung
     * @param user        Người dùng cần kiểm tra
     */
    public void validateWorkspaceWriteAccess(Long workspaceId, User user) {
        if (workspaceId == null || user == null) {
            return;
        }

        if (workspaceMemberRepository != null) {
            WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                    .orElseThrow(() -> new ResourceAccessDeniedException("Bạn không phải là thành viên của Quỹ chung này"));

            if (member.getRole() == WorkspaceRole.VIEWER) {
                throw new ResourceAccessDeniedException("Thành viên quyền VIEWER chỉ có quyền xem, không được phép thực hiện giao dịch trong Quỹ chung!");
            }
        }
    }

    /**
     * Lõi Bù chi vượt nguyên tử - Cover Engine (Tính năng cốt lõi).
     * Khi số tiền chi > số dư phong bì đích, tự động kiểm tra phong bì nguồn bù,
     * thực hiện điều chuyển bù thiếu (COVER_EXPENSE) và thực hiện trừ tiền tài khoản thanh toán + phong bì đích.
     *
     * @param accountId       ID tài khoản thanh toán thực tế
     * @param envelopeId      ID phong bì đích thực hiện chi tiêu
     * @param coverEnvelopeId ID phong bì nguồn bù tiền (nếu thiếu hụt)
     * @param categoryId      ID danh mục chi tiêu
     * @param amount          Số tiền chi tiêu thực tế
     * @param note            Ghi chú chi tiêu
     * @param user            Người dùng thực hiện
     * @return Giao dịch chi tiêu Transaction đã được lưu
     */
    @Transactional(rollbackFor = Exception.class)
    public Transaction recordExpenseWithCover(Long accountId, Long envelopeId, Long coverEnvelopeId,
                                              Long categoryId, BigDecimal amount, String note, User user) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền chi tiêu phải lớn hơn 0");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Tài khoản thanh toán không tồn tại"));

        Envelope envelope = envelopeRepository.findById(envelopeId)
                .orElseThrow(() -> new BusinessException("Phong bì đích không tồn tại"));

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        EnvelopeTransfer savedTransfer = null;
        BigDecimal deficit = amount.subtract(envelope.getBalance());

        if (deficit.compareTo(BigDecimal.ZERO) > 0) {
            if (coverEnvelopeId == null) {
                throw new BusinessException("Phong bì thiếu " + deficit + " VNĐ. Bắt buộc chọn nguồn bù!");
            }
            if (coverEnvelopeId.equals(envelopeId)) {
                throw new BusinessException("Phong bì nguồn bù không được trùng với phong bì đích!");
            }
            Envelope coverSource = envelopeRepository.findById(coverEnvelopeId)
                    .orElseThrow(() -> new BusinessException("Phong bì nguồn bù không tồn tại"));

            // Gọi kiểm tra khóa phong bì bảo vệ (TV4)
            if (coverSource.isLocked()) {
                throw new BusinessException("Phong bì nguồn đã bị khóa bảo vệ, không thể rút bù!");
            }
            if (coverSource.getBalance().compareTo(deficit) < 0) {
                throw new InsufficientBalanceException("Phong bì nguồn không đủ tiền bù!");
            }

            // 1. Chuyển tiền bù nguyên tử
            coverSource.spend(deficit);
            envelope.allocate(deficit);
            envelopeRepository.save(coverSource);

            EnvelopeTransfer transfer = new EnvelopeTransfer();
            transfer.setSourceEnvelope(coverSource);
            transfer.setTargetEnvelope(envelope);
            transfer.setAmount(deficit);
            transfer.setReason(TransferReason.COVER_EXPENSE);
            transfer.setUser(user);
            transfer.setTransferredAt(LocalDateTime.now());
            savedTransfer = transferRepository.save(transfer);
        }

        // 2. Chi tiền thực tế (Account giảm amount, Envelope giảm amount)
        account.debit(amount);
        envelope.spend(amount);

        accountRepository.save(account);
        envelopeRepository.save(envelope);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setEnvelope(envelope);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNote(note);
        transaction.setRelatedTransfer(savedTransfer);

        return transactionRepository.save(transaction);
    }

    /**
     * Overload hàm recordExpenseWithCover nhận TransactionRequest DTO.
     * Tự động kiểm tra quyền Quỹ chung nếu request có chứa workspaceId.
     */
    @Transactional(rollbackFor = Exception.class)
    public Transaction recordExpenseWithCover(TransactionRequest request, User user) {
        if (request == null) {
            throw new BusinessException("Yêu cầu giao dịch không hợp lệ");
        }

        // 5.1. Kiểm tra quyền Quỹ chung (Workspace): Nếu role = VIEWER -> ném ResourceAccessDeniedException
        if (request.getWorkspaceId() != null) {
            validateWorkspaceWriteAccess(request.getWorkspaceId(), user);
        }

        return recordExpenseWithCover(
                request.getAccountId(),
                request.getEnvelopeId(),
                request.getCoverEnvelopeId(),
                request.getCategoryId(),
                request.getAmount(),
                request.getNote(),
                user
        );
    }

    /**
     * Hủy giao dịch và hoàn tiền cho Account và Envelope - Mục 5.3 & Test M3-07.
     * Tự động nhận diện và hoàn tiền theo loại giao dịch:
     * - EXPENSE: Hoàn tiền về Account, hoàn tiền về Envelope.
     *   ĐẶC BIỆT: Nếu khoản chi có gắn vết bù tiền (relatedTransfer / COVER_EXPENSE),
     *   tự động hoàn trả lại số tiền đã bù về cho phong bì nguồn bù (phối hợp Cover Engine).
     * - INCOME: Thu hồi lại tiền từ Account.
     *
     * @param txId ID giao dịch cần hủy
     * @param user Người thực hiện
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransaction(Long txId, User user) {
        if (txId == null) {
            throw new BusinessException("ID giao dịch không được để trống");
        }

        Transaction transaction = transactionRepository.findById(txId)
                .orElseThrow(() -> new BusinessException("Giao dịch không tồn tại"));

        Account account = transaction.getAccount();
        Envelope envelope = transaction.getEnvelope();
        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == TransactionType.EXPENSE) {
            // 1. Hoàn lại tiền cho tài khoản thanh toán
            if (account != null) {
                account.credit(amount);
                accountRepository.save(account);
            }

            // 2. Hoàn lại tiền chi cho phong bì đích
            if (envelope != null) {
                envelope.allocate(amount);
            }

            // 3. Phối hợp với Quang: Nếu khoản chi có gắn vết bù tiền (COVER_EXPENSE)
            EnvelopeTransfer relatedTransfer = transaction.getRelatedTransfer();
            if (relatedTransfer != null && relatedTransfer.getReason() == TransferReason.COVER_EXPENSE) {
                Envelope coverSource = relatedTransfer.getSourceEnvelope();
                if (coverSource != null) {
                    // Hoàn trả lại tiền về phong bì nguồn bù
                    coverSource.allocate(relatedTransfer.getAmount());
                    // Phong bì đích trừ đi phần tiền bù vừa được hoàn trả
                    if (envelope != null) {
                        envelope.spend(relatedTransfer.getAmount());
                    }
                    envelopeRepository.save(coverSource);
                }
                transferRepository.delete(relatedTransfer);
            }

            if (envelope != null) {
                envelopeRepository.save(envelope);
            }
        } else if (transaction.getType() == TransactionType.INCOME) {
            // Hủy thu nhập: Thu hồi lại tiền từ tài khoản
            if (account != null) {
                account.debit(amount);
                accountRepository.save(account);
            }
        }

        transactionRepository.delete(transaction);
    }

    /**
     * Overload hàm cancelTransaction(Long txId) không cần truyền user.
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransaction(Long txId) {
        cancelTransaction(txId, null);
    }

    /**
     * Alias method cho cancelTransaction tương thích ngược.
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpenseTransaction(Long transactionId, User user) {
        cancelTransaction(transactionId, user);
    }
}
