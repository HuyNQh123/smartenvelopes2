# TÀI LIỆU QUYẾT ĐỊNH KIẾN TRÚC CƠ SỞ DỮ LIỆU (DATABASE ARCHITECTURAL DECISIONS)

Dự án: **SmartEnvelope – Quản lý ngân sách & chi tiêu thông minh**  
Kiến trúc: Java 17+, Spring Boot 3.3.4, Spring Data JPA, Hibernate, Flyway, MySQL 8+.

---

## Quyết định 1: Sử dụng kiểu dữ liệu `DECIMAL(19, 0)` cho toàn bộ dữ liệu tiền tệ
- **Bối cảnh**: Hệ thống phục vụ người dùng Việt Nam với đơn vị tiền tệ chính là VNĐ. Các kiểu số thực dấu phẩy động (`FLOAT`, `DOUBLE`) trong lập trình máy tính gặp vấn đề làm tròn nhị phân (IEEE 754), gây sai lệch số dư tài chính sau nhiều phép tính cộng/trừ.
- **Quyết định**: 
  - Trong MySQL: Lưu trữ bằng `DECIMAL(19, 0)`. Kiểu `DECIMAL` lưu trữ chính xác từng chữ số thập phân dưới dạng chuỗi byte cố định, không bị sai số làm tròn. Độ dài 19 chữ số đáp ứng quy mô tiền tệ lên đến hàng trăm nghìn tỷ VNĐ mà không bị tràn số.
  - Trong Java Spring Boot: Ánh xạ 100% thành `java.math.BigDecimal` với scale 0. Mọi phép toán nhân chia tỷ lệ phần trăm phân bổ đều được xử lý thông qua `BigDecimal.setScale(0, RoundingMode.HALF_UP)`.
- **Hệ quả**: Đảm bảo sự cân bằng tuyệt đối giữa số dư tài khoản thực tế và tổng tiền trong các phong bì/giao dịch.

---

## Quyết định 2: Hợp nhất thực thể kỳ ngân sách vào `BudgetPeriod`
- **Bối cảnh**: Khung skeleton ban đầu có sự xuất hiện của cả hai thực thể `Budget` và `BudgetPeriod`, dẫn đến sự mơ hồ về trách nhiệm giữa "ngân sách tổng" và "chu kỳ ngân sách theo tháng".
- **Quyết định**:
  - Hợp nhất và sử dụng duy nhất thực thể `BudgetPeriod` (bảng `budget_periods`).
  - Mỗi bản ghi `BudgetPeriod` đại diện cho một kỳ ngân sách hoàn chỉnh gắn với một `Workspace` trong một tháng/năm xác định (`month`, `year`).
  - Các phong bì (`Envelope`) thuộc trực tiếp về một `BudgetPeriod`.
  - Cấu hình quy tắc chung (50/30/20, ngày nhận lương) được tách sang thực thể chuyên trách `UserFinanceSettings`.
- **Hệ quả**: Loại bỏ hoàn toàn sự trùng lặp, schema tinh giản, quan hệ giữa kỳ ngân sách và phong bì trở nên trực quan, tự nhiên và dễ truy vấn.

---

## Quyết định 3: Mô hình Multi-tenancy dựa trên `Workspace` và `Membership`
- **Bối cảnh**: Ứng dụng cần phục vụ cả hai đối tượng: người dùng cá nhân (quản lý tiền của riêng mình) và nhóm người dùng (gia đình, vợ chồng, nhóm bạn cùng quản lý một quỹ chi tiêu chung).
- **Quyết định**:
  - Mỗi người dùng khi đăng ký luôn được tự động cấp một Workspace cá nhân (`type = 'PERSONAL'`).
  - Người dùng có thể tạo thêm Workspace chia sẻ (`type = 'SHARED'`) và mời người khác tham gia thông qua bảng `memberships`.
  - Toàn bộ dữ liệu tài chính (tài khoản, kỳ ngân sách, phong bì, giao dịch, heo đất, mục tiêu tiết kiệm) đều được neo theo `workspace_id`.
- **Hệ quả**: Kiến trúc linh hoạt, mở rộng đa người dùng (Shared Household) dễ dàng mà không phải thay đổi cấu trúc bảng nghiệp vụ cốt lõi.

---

## Quyết định 4: Hiện thực hóa triết lý Zero-based Budgeting (ZBB)
- **Bối cảnh**: Phương pháp Zero-based Budgeting yêu cầu: Thu nhập - Tổng phân bổ = 0. Mỗi đồng tiền kiếm được đều phải được gán một "nhiệm vụ" cụ thể vào các phong bì trước khi kỳ chi tiêu bắt đầu.
- **Quyết định**:
  - Bảng `budget_periods` lưu trữ hai chỉ số then chốt: `total_income` và `total_allocated`.
  - Kỳ ngân sách có 3 trạng thái: `DRAFT` (đang phân bổ), `CONFIRMED` (đã phân bổ 100%, khóa kế hoạch và chính thức áp dụng), `CLOSED` (đã chốt sổ cuối tháng).
  - Nghiệp vụ kiểm tra tính hợp lệ: `CONFIRMED` chỉ được kích hoạt khi `total_allocated == total_income`.
- **Hệ quả**: Ngăn chặn người dùng phân bổ tiền "ảo" hoặc chi tiêu tự do vượt quá thu nhập thực tế.

---

## Quyết định 5: Nghiệp vụ bù đắp ngân sách thông qua `EnvelopeTransfer`
- **Bối cảnh**: Khi người dùng phát sinh một khoản chi tiêu đột xuất vượt quá số dư của một phong bì nhất định, trong mô hình phong bì thực tế, người dùng phải rút tiền từ phong bì khác để bù đắp vào.
- **Quyết định**:
  - Không cho phép phong bì âm tiền không kiểm soát. Khi muốn chi vượt hạn mức, hệ thống yêu cầu hoặc đề xuất thực hiện một lượt điều chuyển ngân sách (`EnvelopeTransfer`) từ một phong bì có dư (`from_envelope_id`) sang phong bì đang thiếu (`to_envelope_id`).
  - Ghi nhận đầy đủ số tiền chuyển, lý do (`COVER_EXPENSE` hoặc `REPLAN`) và người thực hiện.
- **Hệ quả**: Duy trì tính toàn vẹn của tổng hạn mức phân bổ trong kỳ mà vẫn linh hoạt hỗ trợ người dùng ứng phó các tình huống chi tiêu thực tế.

---

## Quyết định 6: Cơ chế Heo đất tích lũy (`PiggyBank`) và Quét dư cuối tháng (`MonthClose`)
- **Bối cảnh**: Cuối tháng, các phong bì chi tiêu thường còn dư một khoản tiền nhất định. Nếu không có cơ chế thu hồi, tiền dư sẽ bị tồn đọng lãng phí hoặc gây nhầm lẫn khi sang tháng mới.
- **Quyết định**:
  - Áp dụng chính sách dọn dẹp số dư (`RolloverPolicy`): Mặc định là `SWEEP_TO_PIGGY` (quét toàn bộ số dư còn lại của tất cả phong bì vào Heo đất tích lũy `PiggyBank`).
  - Nghiệp vụ chốt tháng (`MonthClose`) tính toán tổng chi tiêu thực tế, tổng tiền tiết kiệm được, tạo các bản ghi `PiggyBankEntry` dạng `SWEEP` và chuyển trạng thái `BudgetPeriod` sang `CLOSED`.
  - Tiền trong Heo đất có thể tiếp tục được nạp vào các mục tiêu tiết kiệm dài hạn (`SavingsGoal`).
- **Hệ quả**: Tạo động lực tiết kiệm rõ ràng cho người dùng: "Tiêu càng ít trong các phong bì thì heo đất cuối tháng càng đầy".

---

## Quyết định 7: Nhật ký kiểm toán bất biến (`TransactionAudit`)
- **Bối cảnh**: Trong các ứng dụng tài chính, việc sửa hoặc xóa giao dịch có thể gây sai lệch số dư tài khoản và hạn mức phong bì, đồng thời dễ phát sinh tranh cãi trong các quỹ chung gia đình nếu không biết ai đã thay đổi số liệu.
- **Quyết định**:
  - Thiết kế bảng `transaction_audits` ghi lại mọi hành động tác động vào giao dịch: `CREATE`, `UPDATE`, `CANCEL`.
  - Lưu trữ ảnh chụp dữ liệu cũ (`old_values`) và dữ liệu mới (`new_values`) dưới dạng JSON Text.
  - Khóa ngoại liên kết chặt chẽ tới `changed_by` (người dùng) và thời điểm `changed_at`.
- **Hệ quả**: Minh bạch hóa toàn bộ dữ liệu tài chính, tạo nền tảng vững chắc cho việc đối soát và khôi phục khi phát sinh lỗi nhập liệu.

---

## Quyết định 8: Khóa lạc quan (Optimistic Locking) với `@Version`
- **Bối cảnh**: Trong môi trường quỹ chung gia đình, hai thành viên có thể cùng mở ứng dụng và ghi nhận chi tiêu hoặc sửa phân bổ phong bì tại cùng một thời điểm. Nếu không kiểm soát, sự kiện ghi đè sau sẽ làm mất bản cập nhật trước (Lost Update Problem).
- **Quyết định**:
  - Thêm cột `version INT NOT NULL DEFAULT 0` trên 4 bảng chịu áp lực ghi đồng thời cao nhất: `accounts`, `budget_periods`, `envelopes`, `transactions`.
  - Ánh xạ `@Version` trong các entity JPA tương ứng. Khi có tranh chấp đồng thời, Hibernate sẽ phát hiện version không khớp và ném ra `OptimisticLockException`, giúp Service có cơ chế thông báo cho người dùng tải lại số liệu mới nhất.
- **Hệ quả**: Bảo vệ tính nhất quán dữ liệu mà không cần áp dụng khóa bi quan (Pessimistic Locking) gây nghẽn hiệu năng hệ thống.

---

## Quyết định 9: Danh mục phân loại hỗn hợp (`is_system` và `user_id`)
- **Bối cảnh**: Người dùng mới cần có sẵn một bộ danh mục chuẩn (Ăn uống, Nhà ở, Đi lại, Mua sắm...) để dùng ngay sau khi tạo tài khoản, nhưng đồng thời cũng muốn tự tạo thêm các danh mục mang tính cá nhân hóa cao.
- **Quyết định**:
  - Bảng `categories` có cột `is_system BOOLEAN` và `user_id BIGINT NULL`.
  - Danh mục hệ thống có `is_system = TRUE`, `user_id = NULL` (được chèn tự động qua Flyway migration `V5`).
  - Danh mục người dùng tạo có `is_system = FALSE`, `user_id = <ID người dùng>`.
  - Query lấy danh mục cho một người dùng: `WHERE is_system = TRUE OR user_id = :userId`.
- **Hệ quả**: Tiết kiệm không gian lưu trữ (không cần sao chép bộ danh mục mặc định cho từng người dùng mới), vừa đảm bảo tính mở và linh hoạt tối đa.

