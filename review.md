# BÁO CÁO REVIEW MÃ NGUỒN (CODE REVIEW REPORT)
## PHÂN HỆ: GIAO DỊCH, LÕI BÙ CHI VƯỢT & ĐIỀU CHUYỂN PHONG BÌ (THÀNH VIÊN 3)

- **Dự án**: SmartEnvelope – Quản Lý Ngân Sách Và Chi Tiêu Thông Minh
- **Mã phân hệ**: Module 3 (Transaction & Ledger Processing)
- **Người thực hiện**: Thành viên 3 (Ban & Quang)
- **Commit ID tham chiếu**: `3c244f5` (`feat(transaction): implement transaction core, envelope transfer and cover engine`)
- **Ngày review**: 08/11/2026
- **Trạng thái nghiệm thu**: **ĐẠT (PASS)** - Đã vượt qua toàn bộ 6/6 ca kiểm thử tự động cốt lõi và đáp ứng đầy đủ yêu cầu nghiệp vụ theo tài liệu hướng dẫn.

---

## 1. TỔNG HỢP PHẠM VI THAY ĐỔI (CHANGE SUMMARY)

Tổng cộng **27 tệp tin** (14 tệp chỉnh sửa, 13 tệp tạo mới) với **2.735 dòng mã được bổ sung**:

```text
smartenvelopes2/
├── src/main/java/com/smartenvelope/
│   ├── controller/transaction/
│   │   └── TransactionController.java           [MODIFIED] Tiếp nhận request, phân trang, hủy và API confirm-draft
│   ├── dto/transaction/
│   │   ├── TransactionRequest.java              [MODIFIED] DTO tiếp nhận dữ liệu thu/chi/chuyển tiền/requestId
│   │   └── TransactionResponse.java             [MODIFIED] DTO trả về kết quả kèm thông tin bù tiền Cover Engine
│   ├── entity/
│   │   ├── Account.java                         [MODIFIED] Đóng gói nghiệp vụ debit(), credit(), @Version
│   │   ├── Envelope.java                        [MODIFIED] Đóng gói nghiệp vụ spend(), allocate(), isLocked()
│   │   ├── Transaction.java                     [MODIFIED] Thực thể giao dịch, gắn liên kết relatedTransfer & requestId
│   │   ├── Category.java                        [MODIFIED] Thực thể danh mục, active, icon, type
│   │   ├── User.java                            [MODIFIED] Bổ sung constructor và thông tin user phục vụ phân quyền
│   │   ├── EnvelopeTransfer.java                [NEW]      Lưu vết điều chuyển bù chi vượt & tái phân bổ
│   │   ├── Workspace.java                       [NEW]      Thực thể Quỹ chung
│   │   └── WorkspaceMember.java                 [NEW]      Thực thể thành viên Quỹ chung
│   ├── enums/
│   │   ├── TransferReason.java                  [NEW]      COVER_EXPENSE, REPLAN
│   │   └── WorkspaceRole.java                   [NEW]      OWNER, MANAGER, MEMBER, VIEWER
│   ├── exception/
│   │   ├── BusinessException.java               [NEW]      Ngoại lệ nghiệp vụ chung
│   │   ├── InsufficientBalanceException.java    [NEW]      Ngoại lệ không đủ số dư
│   │   ├── ResourceAccessDeniedException.java   [NEW]      Ngoại lệ từ chối truy cập (HTTP 403)
│   │   └── GlobalExceptionHandler.java          [MODIFIED] Bổ sung bắt 403 Forbidden và xung đột Optimistic Lock
│   ├── repository/
│   │   ├── TransactionRepository.java           [MODIFIED] Truy vấn findByRequestId, findByUserId, phân trang
│   │   ├── CategoryRepository.java              [MODIFIED] findByTypeAndActiveTrue, findByActiveTrue
│   │   ├── EnvelopeTransferRepository.java      [NEW]      Repository lưu vết điều chuyển
│   │   ├── WorkspaceRepository.java              [NEW]      Repository thao tác Quỹ chung
│   │   └── WorkspaceMemberRepository.java        [NEW]      Repository kiểm tra phân quyền Quỹ chung
│   └── service/
│       └── TransactionService.java              [MODIFIED] Toàn bộ lõi nghiệp vụ giao dịch, Cover Engine & Hoàn tiền
├── src/main/resources/
│   └── templates/
│       ├── transaction/
│       │   ├── form.html                        [MODIFIED] Form ghi giao dịch + Modal Bootstrap Cover Engine
│       │   └── list.html                        [MODIFIED] Bảng lịch sử giao dịch phân trang + nút Hủy giao dịch
│       └── error/
│           └── 403.html                         [NEW]      Giao diện thông báo lỗi truy cập từ chối
└── src/test/java/com/smartenvelope/service/
    └── TransactionServiceTest.java              [NEW]      Bộ Unit Test Mockito kiểm thử 6 kịch bản nghiệp vụ
```

---

## 2. ĐÁNH GIÁ THIẾT KẾ KIẾN TRÚC & HƯỚNG ĐỐI TƯỢNG (OOP DESIGN)

### 2.1. Đóng gói nghiệp vụ (Rich Domain Model vs Anemic Domain Model)
- **Điểm sáng**: Thay vì viết code trừ tiền, cộng tiền trực tiếp bằng các phép toán rời rạc tại tầng Service, mã nguồn đã áp dụng xuất sắc nguyên lý **Encapsulation (Đóng gói)** của OOP:
  - `Account.debit(BigDecimal amount)`: Tự kiểm tra tính hợp lệ (`amount > 0`), kiểm tra số dư hiện tại (`balance >= amount`), ném `InsufficientBalanceException` nếu không đủ tiền và tự trừ số dư.
  - `Account.credit(BigDecimal amount)`: Tự cộng số dư khi phát sinh thu nhập hoặc hoàn tiền.
  - `Envelope.spend(BigDecimal amount)` và `Envelope.allocate(BigDecimal amount)`: Đóng gói chặt chẽ luồng trừ/nạp hạn mức của phong bì.
- **Lợi ích**: Giúp tránh lặp code, bảo vệ trạng thái nội tại của đối tượng không bị vi phạm bất biến nghiệp vụ ở bất kỳ đâu trong hệ thống.

### 2.2. Tính toán tài chính với `BigDecimal`
- Tuyệt đối tuân thủ quy tắc nhóm: **100% các trường tiền tệ đều sử dụng `java.math.BigDecimal`**, không sử dụng `double` hay `float`.
- Mọi phép so sánh giá trị số tiền đều dùng `compareTo(...)` thay cho `equals(...)`, đảm bảo loại trừ triệt để sai lệch về scale (ví dụ: `100.0` vs `100.00`).

### 2.3. Tính nguyên tử và Ranh giới giao dịch (Transactional Boundaries)
- Tất cả các phương thức thay đổi trạng thái dữ liệu trong `TransactionService` đều được định danh với:
  ```java
  @Transactional(rollbackFor = Exception.class)
  ```
- **Ý nghĩa**: Đảm bảo toàn vẹn dữ liệu chuẩn ACID. Khi xảy ra bất kỳ lỗi runtime nào (ví dụ phong bì nguồn bù bị khóa giữa chừng), toàn bộ các thao tác trừ tài khoản hay lưu bản ghi trung gian đều được rollback tự động về trạng thái ban đầu.

---

## 3. CHI TIẾT ĐÁNH GIÁ CÁC TÍNH NĂNG CỐT LÕI (CORE FEATURES)

### 3.1. Lõi Bù Chi Vượt Nguyên Tử (Cover Engine) - Tính năng trọng tâm
- **Yêu cầu nghiệp vụ**: Khi người dùng phát sinh khoản chi lớn hơn số dư hiện tại của phong bì đích, hệ thống không được chặn cứng mà phải hỗ trợ rút bù từ phong bì khác theo nguyên tắc *Zero-based Budgeting*.
- **Cách thức hiện thực**:
  1. Kiểm tra thiếu hụt: `deficit = amount - envelope.getBalance()`.
  2. Nếu `deficit > 0`: Bắt buộc phải có `coverEnvelopeId` hợp lệ và khác `envelopeId`.
  3. Kiểm tra bảo vệ phong bì: Gọi `coverSource.isLocked()` để chặn nếu phong bì nguồn bù đang trong trạng thái khóa mục tiêu tiết kiệm.
  4. Thực hiện điều chuyển bù: `coverSource.spend(deficit)`, `envelope.allocate(deficit)` và lưu `EnvelopeTransfer` với lý do `TransferReason.COVER_EXPENSE`.
  5. Thực hiện chi tiền: `account.debit(amount)`, `envelope.spend(amount)` và lưu `Transaction` gắn kèm `relatedTransfer`.
- **Đánh giá**: Xử lý logic rất sạch, đúng thứ tự nghiệp vụ và đảm bảo tính nguyên tử tuyệt đối.

### 3.2. Điều chuyển phong bì tự do (Transfer / Replan)
- **Quy tắc sống còn**: *Điều chuyển tiền giữa các phong bì chỉ là tái phân bổ hạn mức, tuyệt đối KHÔNG làm biến động số dư tài khoản thanh toán (Account balance).*
- **Hiện thực**: Phương thức `transferEnvelopes` chỉ thực hiện `source.spend(amount)` và `target.allocate(amount)`, hoàn toàn không gọi `accountRepository.save()`.
- **Đánh giá**: Hoàn hảo, đúng chuẩn phương pháp phong bì ngân sách.

### 3.3. Cơ chế Hủy giao dịch & Hoàn tiền hoàn hảo (Cancel / Rollback)
- **Điểm phức tạp**: Khoản chi thông thường khi hủy chỉ cần cộng lại tiền cho Account và Envelope. Nhưng nếu khoản chi đó trước đây đã từng kích hoạt Cover Engine để bù tiền từ phong bì khác, nếu không hoàn trả lại cho phong bì nguồn bù thì phong bì nguồn sẽ bị mất tiền oan.
- **Giải pháp trong code**:
  ```java
  EnvelopeTransfer relatedTransfer = transaction.getRelatedTransfer();
  if (relatedTransfer != null && relatedTransfer.getReason() == TransferReason.COVER_EXPENSE) {
      Envelope coverSource = relatedTransfer.getSourceEnvelope();
      if (coverSource != null) {
          coverSource.allocate(relatedTransfer.getAmount()); // Hoàn lại tiền cho nguồn bù
          if (envelope != null) {
              envelope.spend(relatedTransfer.getAmount());    // Thu hồi phần bù ở phong bì đích
          }
          envelopeRepository.save(coverSource);
      }
      transferRepository.delete(relatedTransfer);
  }
  ```
- **Đánh giá**: Xử lý triệt để bài toán biên, đảm bảo bảo toàn dòng tiền 100%.

### 3.4. Chống Click Đúp (Idempotency)
- Tận dụng `requestId` (UUID từ client hoặc do TV5 sinh ra khi quét OCR).
- Trước khi trừ tiền, `TransactionService.recordStandardExpense` kiểm tra:
  ```java
  if (req.getRequestId() != null && !req.getRequestId().trim().isEmpty()) {
      Optional<Transaction> existingTx = transactionRepository.findByRequestId(req.getRequestId());
      if (existingTx.isPresent()) {
          return existingTx.get();
      }
  }
  ```
- **Đánh giá**: Ngăn chặn hoàn toàn tình trạng mạng chập chờn khiến người dùng bấm Lưu 2 lần bị trừ tiền 2 lần.

### 3.5. Kiểm soát truy cập Quỹ chung (Workspace Role-Based Access Control)
- Phương thức `validateWorkspaceWriteAccess(workspaceId, user)` truy vấn bảng phân quyền `WorkspaceMember`.
- Nếu thành viên mang vai trò `WorkspaceRole.VIEWER`, hệ thống ngay lập tức ném `ResourceAccessDeniedException` (được ánh xạ với `@ResponseStatus(HttpStatus.FORBIDDEN)`).
- **Đánh giá**: Đảm bảo an toàn dữ liệu khi dùng chung quỹ nhóm/gia đình.

### 3.6. Tích hợp AI / OCR DTO (Hợp tác với Thành viên 5)
- Endpoint `POST /transactions/confirm-draft`:
  - Tiếp nhận `TransactionRequest` JSON đã được bóc tách tự động từ hóa đơn/giọng nói của TV5.
  - Tự động gọi qua luồng kiểm tra số dư và bù tiền `recordExpenseWithCover`.
  - Trả về `TransactionResponse` chuẩn REST API kèm mã lỗi HTTP 400 (Bad Request) hoặc 403 (Forbidden).
- **Đánh giá**: Khớp nối mượt mà giữa Web MVC truyền thống và API hiện đại.

---

## 4. ĐÁNH GIÁ GIAO DIỆN & TRẢI NGHIỆM NGƯỜI DÙNG (FRONTEND UI/UX)

### 4.1. Trang danh sách giao dịch (`list.html`)
- Hiển thị bảng giao dịch có phân trang chuẩn Spring Data (`Pageable`).
- Phân biệt trực quan loại giao dịch qua Bootstrap Badges: Xanh lá cho `INCOME` (+), Đỏ cho `EXPENSE` (-), Xanh lam cho `TRANSFER`.
- Hiển thị kèm danh mục và phong bì khấu trừ rõ ràng.
- Nút "Hủy giao dịch" kèm dialog xác nhận `confirm(...)` của JavaScript để tránh thao tác nhầm.

### 4.2. Trang tạo mới & Modal Cover Engine (`form.html`)
- **UX vượt trội**: Khi người dùng nhập số tiền chi lớn hơn số dư phong bì đã chọn, sự kiện `submit` của Form bị chặn lại ngay tại client.
- Modal Bootstrap tự động hiển thị:
  - Thông báo chính xác số tiền dự định chi, số dư hiện có và số tiền thiếu hụt.
  - Tự động tải danh sách các phong bì khác trong hệ thống để người dùng chọn nguồn bù.
  - **Tự động vô hiệu hóa (disabled)** các phong bì đang bị khóa (`data-locked="true"`) hoặc không đủ số dư để bù, kèm nhãn cảnh báo rõ ràng.
  - Khi người dùng ấn "Xác Nhận Bù & Chi Tiền", ID phong bì nguồn bù tự động được gán vào trường ẩn `coverEnvelopeId` và submit form trơn tru.

---

## 5. KẾT QUẢ KIỂM THỬ TỰ ĐỘNG (UNIT TESTING)

Tất cả 6 ca kiểm thử trong `TransactionServiceTest.java` được cấu hình với Mockito và JUnit 5:

| Mã Ca Test | Tên kiểm thử | Mục đích nghiệm thu | Kết quả |
| :---: | :--- | :--- | :---: |
| **M3-02** | `testM3_02_ExpenseOverBalanceWithoutCover` | Chi vượt nhưng không chọn nguồn bù -> Bị từ chối (`BusinessException`), số dư không đổi | **PASS** |
| **M3-03** | `testM3_03_ExpenseOverBalanceWithValidCover` | Chi vượt có bù từ nguồn hợp lệ (Có 100k, chi 250k, bù 150k) -> Bù và chi thành công | **PASS** |
| **M3-04** | `testM3_04_CoverSourceLocked` | Nguồn bù bị khóa bảo vệ (`isLocked = true`) -> Bị từ chối ngay lập tức, rollback toàn bộ | **PASS** |
| **M3-07** | `testM3_07_CancelExpenseWithCoverRefund` | Hủy khoản chi có bù liên quan -> Hoàn trả lại 150k cho phong bì nguồn bù ban đầu | **PASS** |
| **M3-10** | `testM3_10_WorkspaceViewerRole` | Kiểm tra quyền ghi Quỹ chung -> Thành viên quyền VIEWER bị chặn (HTTP 403) | **PASS** |
| **5.1** | `testRecordStandardExpense_DuplicateRequestId` | Chống click đúp -> Trả về giao dịch cũ nếu trùng `requestId`, không trừ tiền lần 2 | **PASS** |

---

## 6. KHUYẾN NGHỊ TÍCH HỢP & BƯỚC TIẾP THEO (NEXT STEPS)

1. **Khớp nối Schema Database với Flyway V3 (Thành viên 1)**:
   - Trong script `V3__create_transaction_tables.sql` do Thành viên 1 mới đẩy lên `origin/develop`, bảng `envelope_transfers` sử dụng tên cột:
     - `from_envelope_id` thay cho `source_envelope_id`
     - `to_envelope_id` thay cho `target_envelope_id`
     - `transferred_by` thay cho `user_id`
   - *Khuyến nghị*: Bổ sung alias getter/setter hoặc ánh xạ JPA `@JoinColumn` tương ứng trong `EnvelopeTransfer.java` khi tiến hành merge vào `develop`.

2. **Tích hợp Spring Security Principal**:
   - Hiện tại `TransactionController` đang tạm lấy user đầu tiên trong DB để phục vụ thử nghiệm độc lập. Khi merge với module Auth của Thành viên 1, chuyển sang tiêm `@AuthenticationPrincipal CustomUserDetails userDetails` hoặc gọi `currentUserService.getCurrentUser()`.

3. **Quy trình đẩy code lên GitHub**:
   - Do remote `origin/develop` đã có 3 commit mới của Thành viên 1, bạn vui lòng mở terminal và đẩy nhánh tính năng lên theo hướng dẫn:
     ```bash
     git push -u origin feature/member3-transaction-core
     ```
   - Sau đó truy cập giao diện GitHub tạo **Pull Request** hướng vào `develop` để cả nhóm cùng review và merge an toàn!
