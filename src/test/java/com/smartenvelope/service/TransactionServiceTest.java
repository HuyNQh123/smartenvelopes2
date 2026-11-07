package com.smartenvelope.service;

import com.smartenvelope.dto.transaction.TransactionRequest;
import com.smartenvelope.entity.Account;
import com.smartenvelope.entity.Envelope;
import com.smartenvelope.entity.EnvelopeTransfer;
import com.smartenvelope.entity.Transaction;
import com.smartenvelope.entity.User;
import com.smartenvelope.entity.Workspace;
import com.smartenvelope.entity.WorkspaceMember;
import com.smartenvelope.enums.AccountType;
import com.smartenvelope.enums.TransactionType;
import com.smartenvelope.enums.TransferReason;
import com.smartenvelope.enums.WorkspaceRole;
import com.smartenvelope.exception.BusinessException;
import com.smartenvelope.exception.ResourceAccessDeniedException;
import com.smartenvelope.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Bộ kiểm thử các ca nghiệm thu dành riêng cho Thành viên 3 (Quang).
 * Kiểm thử Lõi bù chi vượt Cover Engine, Điều chuyển phong bì và Phân quyền Quỹ chung.
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EnvelopeRepository envelopeRepository;

    @Mock
    private EnvelopeTransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Envelope targetEnvelope;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "Quang", "quang@example.com");
        testAccount = new Account("Tài khoản VCB", AccountType.BANK, new BigDecimal("1000000"), testUser);
        testAccount.setId(10L);

        // Phong bì đích: "Đi chơi" có 100.000 VNĐ
        targetEnvelope = new Envelope("Đi chơi", new BigDecimal("100000"));
        targetEnvelope.setId(100L);
    }

    /**
     * Ca kiểm thử M3-02 trong tài liệu hướng dẫn:
     * Chi vượt nhưng không chọn nguồn bù -> Bị từ chối (ném BusinessException), số dư không đổi.
     */
    @Test
    @DisplayName("M3-02: Chi vượt nhưng không chọn nguồn bù -> Bị từ chối, số dư không đổi")
    void testM3_02_ExpenseOverBalanceWithoutCover_ShouldThrowExceptionAndKeepBalanceUnchanged() {
        // Given (Dữ liệu đầu vào):
        // Phong bì "Đi chơi" đang có 100.000 VNĐ. Người dùng muốn chi 250.000 VNĐ (vượt 150.000 VNĐ)
        // nhưng KHÔNG chọn phong bì nguồn bù (coverEnvelopeId = null).
        BigDecimal expenseAmount = new BigDecimal("250000");
        Long coverEnvelopeId = null;

        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(envelopeRepository.findById(100L)).thenReturn(Optional.of(targetEnvelope));

        BigDecimal initialEnvelopeBalance = targetEnvelope.getBalance();
        BigDecimal initialAccountBalance = testAccount.getBalance();

        // When & Then (Thực hiện & Kỳ vọng):
        // Hệ thống phải ném ra BusinessException cảnh báo thiếu tiền và bắt buộc chọn nguồn bù
        assertThatThrownBy(() -> transactionService.recordExpenseWithCover(
                10L, 100L, coverEnvelopeId, null, expenseAmount, "Tiệc sinh nhật", testUser
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Bắt buộc chọn nguồn bù");

        // Xác minh: Số dư phong bì "Đi chơi" và tài khoản thanh toán không bị thay đổi
        assertThat(targetEnvelope.getBalance()).isEqualByComparingTo(initialEnvelopeBalance);
        assertThat(testAccount.getBalance()).isEqualByComparingTo(initialAccountBalance);

        // Xác minh: Không có bản ghi giao dịch hay điều chuyển bù tiền nào được lưu vào CSDL
        verify(transactionRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    /**
     * Ca kiểm thử M3-03 trong tài liệu hướng dẫn:
     * Chi vượt và bù từ nguồn hợp lệ (Đi chơi có 100k, chi 250k, bù 150k từ Ăn uống)
     * -> Bù và chi cùng thành công; Đi chơi về 0đ, Ăn uống giảm 150k, Account giảm 250k.
     */
    @Test
    @DisplayName("M3-03: Chi vượt và bù từ nguồn hợp lệ -> Đi chơi về 0đ, Ăn uống giảm 150k, Account giảm 250k")
    void testM3_03_ExpenseOverBalanceWithValidCover_ShouldSucceedAndDeductCorrectly() {
        // Given (Dữ liệu đầu vào):
        // 1. Tài khoản ngân hàng có 1.000.000 VNĐ
        // 2. Phong bì đích "Đi chơi" có 100.000 VNĐ
        // 3. Phong bì nguồn bù "Ăn uống" có 1.000.000 VNĐ (không bị khóa)
        Envelope coverEnvelope = new Envelope("Ăn uống", new BigDecimal("1000000"));
        coverEnvelope.setId(200L);

        // Khoản chi 250.000 VNĐ (thiếu 150.000 VNĐ so với phong bì Đi chơi)
        BigDecimal expenseAmount = new BigDecimal("250000");
        Long coverEnvelopeId = 200L;

        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(envelopeRepository.findById(100L)).thenReturn(Optional.of(targetEnvelope));
        when(envelopeRepository.findById(200L)).thenReturn(Optional.of(coverEnvelope));
        when(transferRepository.save(any(EnvelopeTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When (Thực hiện hành động chi tiêu kèm bù tiền):
        Transaction result = transactionService.recordExpenseWithCover(
                10L, 100L, coverEnvelopeId, null, expenseAmount, "Tiệc sinh nhật", testUser
        );

        // Then (Kỳ vọng kết quả):
        // 1. Giao dịch Transaction được tạo thành công
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(expenseAmount);
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);

        // 2. Phong bì đích "Đi chơi" về đúng 0đ (100k vốn có + 150k bù - 250k chi = 0đ)
        assertThat(targetEnvelope.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        // 3. Phong bì nguồn "Ăn uống" giảm đúng 150.000 VNĐ (1.000.000đ - 150.000đ = 850.000 VNĐ)
        assertThat(coverEnvelope.getBalance()).isEqualByComparingTo(new BigDecimal("850000"));

        // 4. Tài khoản thanh toán (Account) giảm đúng khoản chi thực tế 250.000 VNĐ (1.000.000đ - 250.000đ = 750.000 VNĐ)
        assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("750000"));

        // 5. Xác minh có lưu EnvelopeTransfer ghi nhận bù chi vượt (COVER_EXPENSE) 150.000 VNĐ
        verify(transferRepository, times(1)).save(argThat(transfer ->
                transfer.getReason() == TransferReason.COVER_EXPENSE
                        && transfer.getAmount().compareTo(new BigDecimal("150000")) == 0
                        && transfer.getSourceEnvelope().getId().equals(200L)
                        && transfer.getTargetEnvelope().getId().equals(100L)
        ));

        // 6. Xác minh có lưu giao dịch chi tiêu Transaction
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    /**
     * Ca kiểm thử M3-04 trong tài liệu hướng dẫn:
     * Nguồn bù bị khóa bảo vệ -> Bị từ chối ngay lập tức, rollback toàn bộ.
     */
    @Test
    @DisplayName("M3-04: Nguồn bù bị khóa bảo vệ -> Bị từ chối ngay lập tức, rollback toàn bộ")
    void testM3_04_CoverEnvelopeLocked_ShouldThrowExceptionAndRollback() {
        // Given (Dữ liệu đầu vào):
        // Phong bì nguồn bù "Tiền trọ" có 3.000.000 VNĐ nhưng bị khóa bảo vệ (locked = true)
        Envelope lockedCoverEnvelope = new Envelope("Tiền trọ", new BigDecimal("3000000"), true);
        lockedCoverEnvelope.setId(300L);

        // Khoản chi 250.000 VNĐ (vượt quá 100.000 VNĐ của phong bì đích Đi chơi)
        BigDecimal expenseAmount = new BigDecimal("250000");
        Long coverEnvelopeId = 300L;

        when(accountRepository.findById(10L)).thenReturn(Optional.of(testAccount));
        when(envelopeRepository.findById(100L)).thenReturn(Optional.of(targetEnvelope));
        when(envelopeRepository.findById(300L)).thenReturn(Optional.of(lockedCoverEnvelope));

        BigDecimal initialTargetBalance = targetEnvelope.getBalance();
        BigDecimal initialCoverBalance = lockedCoverEnvelope.getBalance();
        BigDecimal initialAccountBalance = testAccount.getBalance();

        // When & Then (Thực hiện & Kỳ vọng):
        // Hệ thống phát hiện phong bì bù bị khóa -> Ném ngay BusinessException từ chối
        assertThatThrownBy(() -> transactionService.recordExpenseWithCover(
                10L, 100L, coverEnvelopeId, null, expenseAmount, "Tiệc sinh nhật", testUser
        ))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Phong bì nguồn đã bị khóa bảo vệ, không thể rút bù!");

        // Xác minh toàn bộ số dư được bảo toàn nguyên vẹn (rollback an toàn)
        assertThat(targetEnvelope.getBalance()).isEqualByComparingTo(initialTargetBalance);
        assertThat(lockedCoverEnvelope.getBalance()).isEqualByComparingTo(initialCoverBalance);
        assertThat(testAccount.getBalance()).isEqualByComparingTo(initialAccountBalance);

        // Xác minh: Tuyệt đối không có bản ghi nào được lưu vào CSDL
        verify(transferRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    /**
     * Ca kiểm thử M3-07 trong tài liệu hướng dẫn:
     * Hủy khoản chi có bù liên quan -> Hoàn trả lại 150k cho phong bì nguồn Ăn uống.
     */
    @Test
    @DisplayName("M3-07: Hủy khoản chi có bù liên quan -> Hoàn trả lại 150k cho phong bì nguồn Ăn uống")
    void testM3_07_CancelExpenseWithRelatedCover_ShouldRefundCoverEnvelopeAndAccount() {
        // Given (Dữ liệu đầu vào):
        // Giả lập trạng thái sau khi đã chi tiêu 250k (được bù 150k từ Ăn uống sang Đi chơi):
        // - Tài khoản VCB còn: 750.000 VNĐ
        // - Phong bì đích "Đi chơi" còn: 0 VNĐ
        // - Phong bì nguồn bù "Ăn uống" còn: 850.000 VNĐ
        testAccount.setBalance(new BigDecimal("750000"));
        targetEnvelope.setBalance(BigDecimal.ZERO);

        Envelope coverEnvelope = new Envelope("Ăn uống", new BigDecimal("850000"));
        coverEnvelope.setId(200L);

        // Bản ghi điều chuyển bù tiền 150.000 VNĐ liên quan
        EnvelopeTransfer relatedTransfer = new EnvelopeTransfer();
        relatedTransfer.setId(88L);
        relatedTransfer.setSourceEnvelope(coverEnvelope);
        relatedTransfer.setTargetEnvelope(targetEnvelope);
        relatedTransfer.setAmount(new BigDecimal("150000"));
        relatedTransfer.setReason(TransferReason.COVER_EXPENSE);
        relatedTransfer.setUser(testUser);

        // Giao dịch chi tiêu 250.000 VNĐ liên kết với bản ghi bù tiền trên
        Transaction transaction = new Transaction();
        transaction.setId(500L);
        transaction.setUser(testUser);
        transaction.setAccount(testAccount);
        transaction.setEnvelope(targetEnvelope);
        transaction.setAmount(new BigDecimal("250000"));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setRelatedTransfer(relatedTransfer);

        when(transactionRepository.findById(500L)).thenReturn(Optional.of(transaction));

        // When (Thực hiện hủy khoản chi tiêu):
        transactionService.cancelExpenseTransaction(500L, testUser);

        // Then (Kỳ vọng):
        // 1. Hoàn trả lại đúng 150.000 VNĐ cho phong bì nguồn "Ăn uống" (850k + 150k = 1.000.000 VNĐ)
        assertThat(coverEnvelope.getBalance()).isEqualByComparingTo(new BigDecimal("1000000"));

        // 2. Phong bì đích "Đi chơi" trở về đúng số dư ban đầu 100.000 VNĐ (0đ + 250k - 150k = 100k)
        assertThat(targetEnvelope.getBalance()).isEqualByComparingTo(new BigDecimal("100000"));

        // 3. Tài khoản thanh toán được hoàn lại 250.000 VNĐ (750k + 250k = 1.000.000 VNĐ)
        assertThat(testAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1000000"));

        // 4. Bản ghi bù tiền liên quan và giao dịch chi tiêu được xóa bỏ / hoàn tác
        verify(transferRepository, times(1)).delete(relatedTransfer);
        verify(transactionRepository, times(1)).delete(transaction);
    }

    /**
     * Ca kiểm thử M3-10 trong tài liệu hướng dẫn:
     * Kiểm tra quyền ghi Quỹ chung -> User quyền VIEWER bị chặn (HTTP 403).
     */
    @Test
    @DisplayName("M3-10: Kiểm tra quyền ghi Quỹ chung -> User quyền VIEWER bị chặn (HTTP 403)")
    void testM3_10_WorkspaceViewerRole_ShouldThrowResourceAccessDeniedException403() {
        // Given (Dữ liệu đầu vào):
        // 1. Quỹ chung (Workspace) có ID 99L
        Workspace sharedWorkspace = new Workspace("Quỹ chung Gia Đình", testUser);
        sharedWorkspace.setId(99L);

        // 2. Người dùng mang vai trò VIEWER (chỉ xem, không được phép ghi)
        User viewerUser = new User(2L, "Thành viên Xem", "viewer@example.com");
        WorkspaceMember viewerMember = new WorkspaceMember(sharedWorkspace, viewerUser, WorkspaceRole.VIEWER);

        // 3. Yêu cầu tạo giao dịch chi tiêu trong Quỹ chung này
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(10L);
        request.setEnvelopeId(100L);
        request.setWorkspaceId(99L);
        request.setAmount(new BigDecimal("50000"));
        request.setNote("Chi mua sắm quỹ chung");

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(99L, 2L))
                .thenReturn(Optional.of(viewerMember));

        // When & Then (Thực hiện & Kỳ vọng):
        // Hệ thống phát hiện role = VIEWER -> Ném ResourceAccessDeniedException
        assertThatThrownBy(() -> transactionService.recordExpenseWithCover(request, viewerUser))
                .isInstanceOf(ResourceAccessDeniedException.class)
                .hasMessageContaining("Thành viên quyền VIEWER chỉ có quyền xem");

        // Xác minh ngoại lệ được cấu hình trả về mã trạng thái HTTP 403 Forbidden
        ResponseStatus responseStatus = ResourceAccessDeniedException.class.getAnnotation(ResponseStatus.class);
        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.FORBIDDEN);

        // Xác minh: Không có giao dịch hay điều chuyển nào được lưu vào CSDL
        verify(transactionRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    /**
     * Ca kiểm thử Mục 5.1: Chống click đúp (Idempotency) qua requestId.
     * Nếu requestId đã tồn tại trong CSDL -> Trả về giao dịch cũ, không trừ tiền lần 2.
     */
    @Test
    @DisplayName("5.1: Chống click đúp -> Trả về giao dịch cũ nếu trùng requestId")
    void testRecordStandardExpense_DuplicateRequestId_ShouldReturnExistingTransaction() {
        // Given: Giao dịch cũ đã tồn tại với requestId = "REQ-UUID-123"
        Transaction existingTx = new Transaction();
        existingTx.setId(999L);
        existingTx.setRequestId("REQ-UUID-123");
        existingTx.setAmount(new BigDecimal("50000"));

        when(transactionRepository.findByRequestId("REQ-UUID-123")).thenReturn(Optional.of(existingTx));

        TransactionRequest duplicateReq = new TransactionRequest();
        duplicateReq.setAccountId(10L);
        duplicateReq.setEnvelopeId(100L);
        duplicateReq.setAmount(new BigDecimal("50000"));
        duplicateReq.setRequestId("REQ-UUID-123");

        // When: Người dùng click đúp gửi lại cùng một requestId
        Transaction result = transactionService.recordStandardExpense(duplicateReq, testUser);

        // Then:
        // 1. Trả về đúng bản ghi giao dịch cũ
        assertThat(result).isSameAs(existingTx);
        assertThat(result.getId()).isEqualTo(999L);

        // 2. Tuyệt đối không trừ tiền tài khoản hay phong bì lần 2
        verify(accountRepository, never()).findById(any());
        verify(envelopeRepository, never()).findById(any());
        verify(transactionRepository, never()).save(any());
    }
}
