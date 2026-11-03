# Kế hoạch phân công công việc dự án SmartEnvelope

**Đề tài:** Website quản lý chi tiêu theo cơ chế phong bì thông minh  
**Quy mô:** 5 thành viên  
**Thời gian:** 14 ngày kể từ ngày nhóm bắt đầu  
**Công nghệ:** Java 17+, Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, MySQL, Spring Security, Maven, GitHub.

Tài liệu này phân chia công việc theo ý tưởng SmartEnvelope, giữ cơ chế phân bổ đủ thu nhập, bắt buộc bù trước khi chi vượt và tự động gom tiền dư cuối tháng. Mỗi thành viên chịu trách nhiệm nghiệp vụ, giao diện, kiểm thử và tài liệu của phần được giao. Trưởng nhóm điều phối và tích hợp, không làm thay các module.

Đây là **kế hoạch phát triển đích**, không phải xác nhận rằng mã nguồn hiện có đã hoàn thành tất cả chức năng. Nhóm phải kiểm kê phần đã có, phần cần sửa và phần chưa có trước khi bắt đầu.

## 0. Cách dùng kế hoạch và bảng giao việc tổng quát

### 0.1. Giả định để triển khai trong 14 ngày

- Nhóm có 5 người, mỗi người dành khoảng 3–4 giờ/ngày; đây là giả định lập kế hoạch, cần xác nhận trong buổi đầu.
- Một ứng dụng Spring Boot, một MySQL và Thymeleaf; không tách thêm React, microservice hoặc mobile app trong đợt này.
- Ngày 1–2 ưu tiên nền tảng và hợp đồng dữ liệu; từ ngày 3 mới phát triển song song trên hợp đồng đó.
- P0 phải chạy xuyên suốt trước khi đầu tư hiệu ứng giao diện. P1 được chia nhỏ, thử sớm và đánh giá khả thi theo mốc.
- Chưa có tên thành viên/ngày bắt đầu thực tế: dùng TV1–TV5 và Ngày 1–14.

### 0.2. Bảng trách nhiệm

| Thành viên | Vai trò | Phần tự làm đến cuối | Người review/kiểm thử chéo |
|---|---|---|---|
| TV1 — Trưởng nhóm | Nền tảng và quyền | Auth/Admin, User, Workspace/Membership, layout, môi trường, tích hợp | TV2 kiểm tra quyền; TV3 review thay đổi ảnh hưởng giao dịch |
| TV2 | Tài khoản và lập kế hoạch | Account, kỳ, Envelope, phân bổ 100%, các form | TV3 đối chiếu số dư; TV4 kiểm tra khóa và chuyển kỳ |
| TV3 | Giao dịch và luân chuyển | Thu/chi, bù, điều chuyển, sửa/hủy, giao dịch quỹ chung | TV4 kiểm thử quy tắc; TV2 đối chiếu Account/Envelope |
| TV4 | Kỷ luật chi tiêu và thời gian | Cảnh báo, pacing, effort, lịch lương/nhắc, tiết kiệm, đóng kỳ, gợi ý phân bổ, huy hiệu | TV5 đối chiếu báo cáo; TV3 kiểm tra số dư/chuyển kỳ |
| TV5 | Tổng quan và nhập liệu | Dashboard, báo cáo, bản đồ luân chuyển, nhập câu, OCR và form nháp | TV1 kiểm tra quyền/tệp; TV3 kiểm tra đường lưu chi tiêu |

Mỗi thành viên tự làm giao diện và test của module mình. TV5 không làm toàn bộ HTML cho nhóm; TV1 không sửa thay tất cả lỗi tích hợp.

### 0.3. Việc làm ngay trong buổi đầu

1. Điền tên thật và xác nhận thời gian tham gia của từng người.
2. Đọc quy tắc nghiệp vụ, module được giao, bảng phối hợp và tiêu chí nghiệm thu.
3. Đi qua một ví dụ từ nhận thu nhập đến bù; thống nhất Account khác Envelope thế nào.
4. Kiểm kê mã hiện có, tạo issue theo mục 10.1; mỗi Entity có một người chủ trì.
5. Chốt ERD và hợp đồng ở mục 4.4–4.6 trước khi chia nhánh code.
6. Tạo DB phát triển/thử nghiệm riêng và dữ liệu demo giả; thống nhất cách khởi động.
7. Mỗi người nhận issue đầu tiên có đầu vào, đầu ra và người review cụ thể.

### 0.4. Kiểm kê mã hiện có

Đây là ghi nhận từ cấu trúc tệp đã kiểm tra khi cập nhật kế hoạch, chưa phải kết quả chạy lại toàn hệ thống.

| Hiện trạng | Có thể tận dụng | Việc cần làm tiếp |
|---|---|---|
| Java trong `backend/`, giao diện trong `dist/` | Maven, kết nối MySQL, bố cục UI | Giữ thư mục ngoài, thêm Thymeleaf trong resources; không di chuyển project chỉ để giống sơ đồ |
| Có Budget, Envelope, Expense, BudgetTransfer, UserSettings, SharedFund, MonthSummary | Chi/bù, snapshot, dữ liệu và test mẫu | Chuyển hồ sơ ngân sách đơn sang User/Workspace; không chỉ thêm login lên dữ liệu dùng chung |
| Có BudgetService, BudgetController, BudgetRepository | Use case hiện có | Tách dần theo hợp đồng, tránh cả nhóm cùng sửa một Service lớn |
| Tiền đang dùng `long` và lớp Money | Test và phép tính nguyên VNĐ | Chuyển BigDecimal thống nhất, giữ kết quả nghiệp vụ tương đương |
| Có parser từ khóa | Fallback khi API không dùng được | Chưa được coi là LLM hoặc OCR đã hoàn thành |
| Chưa thấy Security/Thymeleaf trong pom đã kiểm tra | MVC và JPA | TV1 thêm nền; mỗi người bổ sung template và kiểm tra quyền cho module |
| Có cấu hình sandbox Docker | Môi trường riêng có thể tiếp tục hoàn thiện | Kiểm tra lại lỗi build context đường dẫn có dấu đã gặp trước khi đánh dấu chạy ổn |
| Có test Java/API | Test hồi quy | Chạy lại và bổ sung Account, multi-user, Thymeleaf, quỹ chung |

Không dùng kết quả test của phiên bản cũ để xác nhận phiên bản mới. Không dùng dữ liệu cá nhân thật làm dữ liệu demo.

## 1. Mục tiêu và người dùng

- **Mục tiêu:** giúp người dùng lập kế hoạch ngay khi nhận thu nhập, nhìn rõ sự đánh đổi khi chi tiêu và tránh sử dụng nhầm tiền dành cho nhu cầu thiết yếu.
- **Người dùng chính:** sinh viên và người mới đi làm, có thu nhập cố định hoặc bán thời gian.
- **Người dùng phụ:** bạn cùng phòng, cặp đôi hoặc nhóm du lịch cần quản lý quỹ chung minh bạch.
- **Luồng chính:** đăng nhập → ghi nhận thu nhập → phân bổ đủ tiền → ghi chi tiêu → bù ngân sách nếu thiếu → theo dõi cảnh báo → tổng kết và tiết kiệm.

## 2. Phạm vi và mức ưu tiên

| Mức | Nội dung | Điều kiện hoàn thành |
|---|---|---|
| P0 — Lõi bắt buộc | Đăng nhập/phân quyền; nguồn tiền; kỳ ngân sách; phân bổ đủ 100%; phong bì; thu/chi; bắt buộc bù; khóa nguồn bù; lưu MySQL; dashboard cơ bản | Luồng chính chạy xuyên suốt, tiền và quyền truy cập đúng |
| P1 — Hoàn thiện ý tưởng | Hạn mức ngày; giờ làm tương đương; tự động gom tiền dư; lịch nhận lương; nhắc lập ngân sách; nhắc kết thúc chi tiêu; quỹ chung; bản đồ điều chuyển; huy hiệu; gợi ý phân bổ từ lịch sử; nhập câu và bóc tách hóa đơn | Mỗi chức năng có người phụ trách, demo và test |
| P2 — Mở rộng | Xuất CSV/JSON nâng cao; lời khuyên AI; phát hiện bất thường; tùy biến giao diện và báo cáo nâng cao | Chỉ triển khai sau khi P0/P1 ổn định |

P1 vẫn thuộc ý tưởng cần đáp ứng, không tự động trở thành phần có thể bỏ. Nếu không kịp hoặc thiếu dịch vụ AI/OCR, trưởng nhóm và cả nhóm phải ghi rõ tính năng hoãn, lý do và ảnh hưởng; không báo cáo là đã hoàn thành bằng một màn hình giả lập.

## 3. Quy tắc nghiệp vụ phải thống nhất trước khi code

### 3.1. Phân bổ đủ tiền

- Người dùng có thể lưu **bản nháp phân bổ** khi chưa chia hết tiền.
- Chỉ được xác nhận kế hoạch khi **tổng phân bổ bằng đúng tổng tiền khả dụng của kỳ**.
- Chưa xác nhận kế hoạch thì không được ghi giao dịch chi của kỳ đó.
- Tổng phân bổ không được vượt tiền khả dụng ở bất kỳ thời điểm nào.
- Số dư sau chi tiêu giảm theo giao dịch; yêu cầu 100% áp dụng cho số tiền phân bổ, không phải tổng số dư sau khi đã chi.
- Chuyển giữa hai phong bì không làm thay đổi tổng tiền được phân bổ.
- Tiền khả dụng gồm thu nhập thực nhận được đưa vào kỳ và số dư được phép chuyển tiếp. Lương dự kiến chỉ dùng để nhắc và lập nháp, không tự tạo tiền thật.
- Nếu phát sinh thu nhập mới trong kỳ, phải bổ sung kế hoạch và phân bổ đủ phần tăng thêm trước khi tiếp tục ghi chi.

### 3.2. Phân biệt tài khoản và phong bì

- `Account` là nơi giữ tiền thực tế: tiền mặt, ngân hàng hoặc loại tài khoản nhóm thống nhất.
- `Envelope` là mục đích sử dụng tiền: Ăn uống, Tiền trọ, Học tập, Đi chơi, Quỹ khẩn cấp.
- Phân bổ hoặc điều chuyển phong bì chỉ thay đổi mục đích của tiền, không tự tăng/giảm số dư Account.
- Giao dịch thu/chi cập nhật Account và phần ngân sách liên quan theo một quy tắc thống nhất, không cập nhật hai lần ở hai module.
- Điều chuyển phong bì không được tính thành thu nhập hoặc chi phí mới trong báo cáo.

### 3.3. Chi vượt và bù ngân sách

1. Kiểm tra người dùng có quyền với Account, kỳ ngân sách và phong bì.
2. Kiểm tra Account có đủ tiền thực tế; bù phong bì không giải quyết được việc Account thiếu tiền.
3. Nếu phong bì thiếu tiền, tính `phần thiếu = khoản chi - số dư phong bì`.
4. Chưa chọn nguồn bù hợp lệ thì không lưu chi tiêu và không thay đổi số dư.
5. Nguồn bù phải khác đích, cùng không gian/quỹ và kỳ ngân sách, chưa khóa và đủ tiền.
6. Bù đúng phần thiếu, lưu lịch sử điều chuyển rồi ghi chi tiêu trong **cùng transaction DB**.
7. Một bước thất bại phải rollback toàn bộ.

Ví dụ: Đi chơi còn 100.000đ, khoản chi 250.000đ → phải lấy 150.000đ từ phong bì hợp lệ trước khi ghi chi.

### 3.4. Phong bì cố định

- Phong bì đã khóa vẫn được chi cho đúng mục đích của nó.
- Không được dùng làm nguồn điều chuyển hoặc giảm phân bổ để lách cơ chế bảo vệ.
- Mở khóa cần thao tác xác nhận rõ ràng và có lịch sử người thực hiện/thời điểm.

### 3.5. Hạn mức ngày và giờ làm tương đương

- Hạn mức ngày = số dư phong bì / số ngày còn lại của tháng, **bao gồm hôm nay**.
- Tính lại từ số dư hiện tại; hôm nay tiết kiệm thì hạn mức ngày sau tăng, tiêu nhiều thì giảm.
- Áp dụng cho phong bì có bật định mức ngày, không bắt buộc với tiền trọ hoặc khoản cố định.
- Quy ước triển khai: đây là hạn mức khuyến nghị có cảnh báo khi vượt; ràng buộc cứng là số dư phong bì và quy trình bù. Không cấm ghi lại một khoản thực tế chỉ vì vượt khuyến nghị ngày.
- Giờ lao động = khoản chi không thiết yếu / thu nhập theo giờ; làm tròn 1 chữ số thập phân để hiển thị.
- Không tính khi chưa có mức thu nhập theo giờ hợp lệ.

### 3.6. Cảnh báo và huy hiệu

- Ngưỡng triển khai thống nhất: sử dụng dưới 80% ngân sách hiện tại là xanh; từ 80% đến dưới 100% là vàng; từ 100% là đỏ.
- Tỷ lệ sử dụng = số tiền đã chi / ngân sách sau điều chuyển. Phong bì chưa phân bổ phải có trạng thái riêng, không chia cho 0.
- Ý tưởng không quy định sẵn các ngưỡng số cụ thể; nhóm dùng một bộ ngưỡng duy nhất cho backend, giao diện và test.
- Huy hiệu tháng bản đầu: 0 lần bù → **Chiến thần kỷ luật**; 1–3 lần → **Người giữ cân bằng**; từ 4 lần → **Chuyên gia lạm lẹm**.
- Chỉ tính các lần điều chuyển phục vụ bù khoản chi vào chỉ số huy hiệu; chuyển để lập lại kế hoạch phải phân biệt bằng loại/lý do.
- Không gán huy hiệu ghi nợ nếu hệ thống không có nghiệp vụ nợ thực tế.

### 3.7. Tiền dư cuối tháng

- Phong bì sinh hoạt được đánh dấu `SWEEP_TO_PIGGY`; tiền còn dư **tự động** chuyển vào lợn đất khi kết thúc kỳ.
- Phong bì cần giữ tiền, ví dụ Quỹ khẩn cấp hoặc khoản thiết yếu chưa thanh toán, dùng `CARRY_FORWARD`; không tự quét tiền đã bảo vệ.
- Việc chuyển tiền dư không yêu cầu xác nhận từng tháng. Người dùng cấu hình chính sách phong bì trước đó; hệ thống thông báo kết quả sau khi chạy.
- Quét tiền dư và đóng kỳ phải nguyên tử, có lịch sử và không chạy trùng. Không làm phát sinh thu nhập giả.
- Scheduler chạy theo múi giờ `Asia/Ho_Chi_Minh`. Nếu máy chủ dừng vào cuối tháng, xử lý bù các kỳ còn thiếu khi hoạt động lại.
- Không tự cộng lương dự kiến cho tháng mới. Kế hoạch kỳ mới chỉ được xác nhận sau khi có dữ liệu tiền thực tế.

### 3.8. Lịch lương và nhắc nhở

- Cài ngày nhận lương, mức lương dự kiến, giờ nhắc lập ngân sách và giờ khép lại chi tiêu.
- Nếu ngày lương lớn hơn số ngày trong tháng, dùng ngày cuối tháng đó.
- Từ ngày nhận lương, đến giờ nhắc, nếu kế hoạch chưa được xác nhận thì tạo thông báo; mặc định nhắc lại mỗi **15 phút**, dừng khi phân bổ đủ và xác nhận.
- Mốc khép lại chi tiêu mặc định **22:00**, có thể đổi; thông báo một lần mỗi ngày, không khóa chức năng ghi giao dịch đã xảy ra.
- Phân biệt rõ: thông báo trong ứng dụng được đọc khi người dùng mở web; muốn nhận khi web đóng phải triển khai Web Push và kiểm thử cấp quyền. Không cam kết thông báo nền nếu mới chỉ có polling trong trang.

### 3.9. AI và quỹ chung

- AI chỉ tạo gợi ý hoặc bản nháp; mọi khoản chi phải qua xác nhận và Service kiểm tra nghiệp vụ như nhập tay.
- OCR phải bóc tách tối thiểu: tên quán, ngày, tổng tiền và phong bì gợi ý. Thiếu hoặc nhận sai thì người dùng sửa trước khi lưu.
- Gợi ý tháng mới dùng tối đa 3 tháng đã tổng kết; chưa đủ lịch sử phải thông báo, không bịa số liệu.
- Quỹ chung có không gian dữ liệu và vai trò riêng. Chỉ thành viên được phép mới được xem hoặc ghi giao dịch; không tự lấy tiền cá nhân bù sang quỹ chung.

## 4. Thiết kế và quy ước kỹ thuật

### 4.1. Phân tầng

| Tầng | Trách nhiệm |
|---|---|
| Controller | Nhận form, Validation, gọi Service, chọn trang Thymeleaf; không tự tính lại số dư |
| Service | Nghiệp vụ, kiểm tra quyền sở hữu, transaction, khóa dữ liệu và phối hợp Repository |
| Repository | Đọc/ghi MySQL, truy vấn theo người dùng/không gian, hỗ trợ khóa khi cần |
| Entity/Domain | Dữ liệu, quan hệ và hành vi bảo vệ trạng thái hợp lệ |
| DTO | Dữ liệu form, bản nháp AI, kết quả dashboard và báo cáo |
| Thymeleaf | Hiển thị dữ liệu, form và thông báo lỗi; không quyết định giao dịch có hợp lệ hay không |

Giữ base package đang có là `vn.smartenvelope`. Ngày đầu nhóm chốt ánh xạ giữa các package hiện tại như `api`, `domain`, `service`, `repository` với cấu trúc mở rộng; không tự ý đổi package hoặc tạo một bộ Entity trùng chức năng.

Giao diện đích dùng Thymeleaf. Giao diện JavaScript hiện có có thể dùng làm mẫu hoặc giữ tạm trong quá trình chuyển đổi, nhưng mỗi chức năng chỉ có một Service nghiệp vụ dùng chung.

### 4.2. Dữ liệu và bảo mật

- Tiền dùng `BigDecimal`; thống nhất VNĐ nguyên với scale 0 và quy tắc làm tròn. Không dùng `float`/`double` để lưu hay tính tiền.
- Khoản thu/chi và điều chuyển phải lớn hơn 0. Số tiền phân bổ được nhập phải lớn hơn 0; phong bì mới/chưa phân bổ có thể có số dư 0.
- Nếu mã hiện tại dùng `long` VNĐ, chuyển sang `BigDecimal` theo một thay đổi được review cùng migration/test; không trộn kiểu tiền giữa module.
- Dùng `@Transactional` cho cập nhật nhiều số dư; có chiến lược khóa/revision để tránh hai yêu cầu cùng dùng một số dư cũ.
- Chống ghi trùng khi người dùng bấm hai lần hoặc gửi lại yêu cầu do mạng lỗi.
- Sửa/xóa giao dịch phải hoàn tác tác động cũ và kiểm tra trạng thái mới trong một transaction. Lưu dấu vết điều chỉnh/hủy; không xóa mất lịch sử kiểm toán.
- Mọi truy vấn phải kiểm tra quyền theo người dùng và không gian cá nhân/quỹ chung; không chỉ ẩn nút trên UI.
- Mật khẩu mã hóa bằng `PasswordEncoder`/BCrypt; không commit `.env`, API key, mật khẩu DB hoặc ảnh hóa đơn cá nhân.

### 4.3. Thực thể và chủ sở hữu thiết kế

| Thực thể/nhóm dữ liệu | Vai trò | Người phụ trách |
|---|---|---|
| User, Role, Category mặc định | Đăng nhập, phân quyền và danh mục dùng chung | TV1 |
| Workspace, Membership | Không gian cá nhân/quỹ chung; chủ quỹ, người ghi và người xem | TV1, thống nhất với TV2/TV3 |
| Account, AccountType | Nguồn tiền thanh toán thực tế | TV2 |
| BudgetPeriod/Budget, Envelope | Kỳ ngân sách, bản nháp/xác nhận, phân bổ và khóa | TV2; quy tắc do TV4 cung cấp |
| Transaction, Income/Expense, EnvelopeTransfer | Thu/chi, bù, điều chuyển và lịch sử điều chỉnh | TV3 |
| UserFinanceSettings | Ngày lương, lương dự kiến, thu nhập theo giờ, giờ nhắc | TV4; tham chiếu User của TV1 |
| SavingsGoal, PiggyBank, MonthClose | Tiết kiệm và lịch sử tổng kết tự động | TV4 |
| Notification, lịch gửi/đọc | Thông báo và chống nhắc trùng | TV4; xác thực/kênh nhận phối hợp TV1 |
| Report DTO, AI draft/OCR DTO | Tổng hợp và dữ liệu nháp, chưa cần bảng riêng nếu không có nhu cầu lưu | TV5 |

Ngày 1 phải chốt ERD, khóa ngoại, trạng thái kỳ, quy tắc Account + Envelope và hợp đồng Service. Chọn một cách đặt tên `BudgetPeriod/Budget` và dùng thống nhất, không để mỗi người tạo một lớp đại diện cùng khái niệm.

### 4.4. Cây thư mục áp dụng cho nhóm

```text
smart-envelope/
├── pom.xml
├── README.md
├── .gitignore
├── .env.example
├── Dockerfile
├── compose.yaml
│
├── docs/
│   ├── Ke_hoach_phan_cong_SmartEnvelope.md
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── SANDBOX.md
│   ├── diagrams/
│   │   ├── erd.md
│   │   └── class-diagram.md
│   └── test-cases/
│       ├── authentication.md
│       ├── budget.md
│       ├── transaction.md
│       └── ai-ocr.md
│
├── database/
│   ├── schema.sql
│   └── demo-data.sql
│
└── src/
    ├── main/
    │   ├── java/vn/smartenvelope/
    │   │   ├── SmartEnvelopeApplication.java
    │   │   │
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── SchedulingConfig.java
    │   │   │   └── AiConfig.java
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── AdminController.java
    │   │   │   ├── WorkspaceController.java
    │   │   │   ├── AccountController.java
    │   │   │   ├── BudgetPeriodController.java
    │   │   │   ├── EnvelopeController.java
    │   │   │   ├── TransactionController.java
    │   │   │   ├── TransferController.java
    │   │   │   ├── SettingsController.java
    │   │   │   ├── SavingsController.java
    │   │   │   ├── NotificationController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── ReportController.java
    │   │   │   └── AssistantController.java
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── auth/
    │   │   │   ├── workspace/
    │   │   │   ├── account/
    │   │   │   ├── budget/
    │   │   │   ├── transaction/
    │   │   │   ├── savings/
    │   │   │   ├── notification/
    │   │   │   ├── report/
    │   │   │   └── assistant/
    │   │   │       ├── AITransactionDraftDTO.java
    │   │   │       └── OcrReceiptDraftDTO.java
    │   │   │
    │   │   ├── entity/
    │   │   │   ├── User.java
    │   │   │   ├── Workspace.java
    │   │   │   ├── Membership.java
    │   │   │   ├── Account.java
    │   │   │   ├── Category.java
    │   │   │   ├── BudgetPeriod.java
    │   │   │   ├── Envelope.java
    │   │   │   ├── Transaction.java
    │   │   │   ├── IncomeTransaction.java
    │   │   │   ├── ExpenseTransaction.java
    │   │   │   ├── EnvelopeTransfer.java
    │   │   │   ├── UserFinanceSettings.java
    │   │   │   ├── SavingsGoal.java
    │   │   │   ├── PiggyBank.java
    │   │   │   ├── MonthClose.java
    │   │   │   └── Notification.java
    │   │   │
    │   │   ├── enums/
    │   │   │   ├── Role.java
    │   │   │   ├── WorkspaceType.java
    │   │   │   ├── MemberRole.java
    │   │   │   ├── AccountType.java
    │   │   │   ├── BudgetStatus.java
    │   │   │   ├── TransactionType.java
    │   │   │   ├── TransferReason.java
    │   │   │   ├── RolloverPolicy.java
    │   │   │   └── AlertLevel.java
    │   │   │
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── WorkspaceRepository.java
    │   │   │   ├── MembershipRepository.java
    │   │   │   ├── AccountRepository.java
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── BudgetPeriodRepository.java
    │   │   │   ├── EnvelopeRepository.java
    │   │   │   ├── TransactionRepository.java
    │   │   │   ├── EnvelopeTransferRepository.java
    │   │   │   ├── UserFinanceSettingsRepository.java
    │   │   │   ├── SavingsGoalRepository.java
    │   │   │   ├── PiggyBankRepository.java
    │   │   │   ├── MonthCloseRepository.java
    │   │   │   └── NotificationRepository.java
    │   │   │
    │   │   ├── service/
    │   │   │   ├── auth/
    │   │   │   │   ├── UserService.java
    │   │   │   │   ├── CustomUserDetailsService.java
    │   │   │   │   └── CurrentUserService.java
    │   │   │   ├── workspace/
    │   │   │   │   ├── WorkspaceService.java
    │   │   │   │   └── MembershipService.java
    │   │   │   ├── account/
    │   │   │   │   └── AccountService.java
    │   │   │   ├── budget/
    │   │   │   │   ├── BudgetPeriodService.java
    │   │   │   │   ├── AllocationService.java
    │   │   │   │   ├── EnvelopeService.java
    │   │   │   │   └── AllocationSuggestionService.java
    │   │   │   ├── transaction/
    │   │   │   │   ├── TransactionService.java
    │   │   │   │   └── TransferService.java
    │   │   │   ├── rules/
    │   │   │   │   ├── EnvelopeRuleService.java
    │   │   │   │   ├── AlertService.java
    │   │   │   │   ├── DailyPacingService.java
    │   │   │   │   ├── EffortCalculatorService.java
    │   │   │   │   └── DisciplineBadgeService.java
    │   │   │   ├── savings/
    │   │   │   │   ├── SavingsGoalService.java
    │   │   │   │   └── MonthCloseService.java
    │   │   │   ├── notification/
    │   │   │   │   ├── NotificationService.java
    │   │   │   │   └── ReminderService.java
    │   │   │   ├── report/
    │   │   │   │   ├── DashboardService.java
    │   │   │   │   └── ReportService.java
    │   │   │   └── assistant/
    │   │   │       ├── AIService.java
    │   │   │       ├── ReceiptOcrService.java
    │   │   │       └── provider/
    │   │   │           ├── TransactionParser.java
    │   │   │           ├── LlmTransactionParser.java
    │   │   │           └── RuleBasedTransactionParser.java
    │   │   │
    │   │   ├── scheduler/
    │   │   │   ├── MonthCloseScheduler.java
    │   │   │   └── ReminderScheduler.java
    │   │   │
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java
    │   │       ├── BusinessException.java
    │   │       ├── InsufficientBalanceException.java
    │   │       ├── BudgetNotAllocatedException.java
    │   │       └── ResourceAccessDeniedException.java
    │   │
    │   └── resources/
    │       ├── application.properties
    │       ├── application-dev.properties
    │       ├── application-docker.properties
    │       │
    │       ├── templates/
    │       │   ├── fragments/
    │       │   │   ├── header.html
    │       │   │   ├── sidebar.html
    │       │   │   └── footer.html
    │       │   ├── auth/
    │       │   ├── admin/
    │       │   ├── workspaces/
    │       │   ├── accounts/
    │       │   ├── budget/
    │       │   ├── envelopes/
    │       │   ├── transactions/
    │       │   ├── transfers/
    │       │   ├── settings/
    │       │   ├── savings/
    │       │   ├── notifications/
    │       │   ├── dashboard/
    │       │   ├── reports/
    │       │   ├── assistant/
    │       │   └── error/
    │       │
    │       └── static/
    │           ├── css/
    │           ├── js/
    │           └── images/
    │
    └── test/
        ├── java/vn/smartenvelope/
        │   ├── service/
        │   ├── repository/
        │   ├── controller/
        │   └── integration/
        │       ├── BudgetFlowIntegrationTest.java
        │       ├── TransferIntegrationTest.java
        │       ├── MonthCloseIntegrationTest.java
        │       └── WorkspacePermissionIntegrationTest.java
        └── resources/
            └── application-test.properties
```

Đây là cách mở rộng cấu trúc hiện có, không cần tạo sẵn mọi lớp trống. Với lớp đang tồn tại trong `api/domain`, chỉ tách hoặc chuyển khi người sở hữu module đã thống nhất và có test hồi quy. Không tạo đồng thời `domain.Envelope` và `entity.Envelope` cùng đại diện một bảng.

Ảnh hóa đơn là dữ liệu người dùng: lưu ngoài thư mục tài nguyên tĩnh, kiểm tra quyền truy cập; không đặt trong Git hoặc `static/images`.

### 4.5. Hợp đồng dữ liệu và trạng thái

| Nội dung | Quy ước phải chốt |
|---|---|
| Workspace | Có kiểu PERSONAL/SHARED; truy vấn Account, kỳ, Envelope và giao dịch phải nằm trong workspace hợp lệ |
| Vai trò | Quản lý quỹ quản lý thành viên/kế hoạch; người ghi được ghi giao dịch; người xem chỉ đọc. Chi tiết quyền sửa/hủy phải ghi thành bảng quyền |
| Người thực hiện | `createdBy/updatedBy` lấy từ phiên đăng nhập; không tin userId gửi trong form |
| Kỳ ngân sách | DRAFT → CONFIRMED → CLOSED; mới nhận thêm tiền thì yêu cầu cập nhật phân bổ trước khi ghi chi tiếp |
| Kỳ duy nhất | Một workspace không có hai kỳ cùng tháng/năm; có ràng buộc DB tương ứng |
| Mã định danh | Dùng nhất quán một kiểu ID theo ERD. Ví dụ dưới đây là hợp đồng logic, không bắt buộc đổi mọi ID hiện có sang UUID |
| Tiền | BigDecimal scale 0, đối chiếu bằng `compareTo`; nhận tiền thập phân sai quy ước phải báo lỗi, không tự làm tròn mất tiền |
| Thời gian | Lưu thời điểm sự kiện nhất quán; tính ngày/kỳ theo `Asia/Ho_Chi_Minh`, không theo giờ ngẫu nhiên của máy thành viên |
| Phiên bản | Mỗi lệnh sửa số dư mang revision/version của trạng thái người dùng đã đọc; backend báo xung đột khi dữ liệu đã đổi |
| Yêu cầu lặp | Mỗi khoản chi có requestId; cùng ID/cùng nội dung không tính tiền lần hai; cùng ID/khác nội dung bị từ chối |
| Điều chuyển | Có lý do COVER_EXPENSE hoặc REPLAN; chỉ COVER_EXPENSE được tính vào số lần bù dùng cho huy hiệu |
| Đóng kỳ | Lưu kết quả duy nhất cho workspace + kỳ; nhiều lần chạy scheduler không tạo nhiều bản tổng kết |
| AI/OCR | Trả bản nháp có các trường có thể thiếu, không trả đối tượng Entity rồi lưu thẳng |
| Liên kết lịch sử phong bì | Có khóa mục đích ổn định như purposeKey/template để đối chiếu các kỳ; không mặc định ID phong bì tháng trước trùng tháng mới |

Các đẳng thức cần dùng khi thiết kế/test:

```text
Số dư Account = số dư mở đầu + thu hợp lệ - chi hợp lệ
                (cộng/trừ chuyển Account nếu nhóm bổ sung nghiệp vụ này)

Ngân sách phong bì hiện tại = phân bổ gốc + chuyển vào - chuyển ra
Số dư phong bì = ngân sách hiện tại - chi hợp lệ của phong bì

Kế hoạch được xác nhận khi:
  tổng ngân sách phong bì hiện tại = tiền khả dụng của kỳ

Sau bù giữa phong bì:
  tổng ngân sách không đổi; số dư Account chưa đổi vì chỉ mới đổi mục đích tiền

Sau chi:
  Account và Envelope liên quan cùng giảm đúng số tiền chi

Gom lợn đất:
  tiền đổi mục đích từ phong bì sang tiết kiệm, không tự giảm Account lần nữa
  và không được cộng thành thu nhập mới
```

Không lấy tổng số dư của mọi Account rồi tự coi toàn bộ là thu nhập mới của tháng. Tiền đã nằm trong lợn đất, tiền quỹ khác và số dư chuyển tiếp phải được phân biệt để không phân bổ trùng.

### 4.6. Hợp đồng gọi giữa các module

Nhóm chốt tên method/DTO thực tế trong ngày 1–2. Mỗi hợp đồng cần đầu vào, đầu ra và lỗi; không yêu cầu module khác gọi trực tiếp Repository để sửa số dư.

| Hợp đồng logic | Chủ sở hữu | Đầu vào chính | Kết quả và điều kiện |
|---|---|---|---|
| Lấy người dùng/kiểm tra quyền | TV1 | Phiên đăng nhập, workspaceId, hành động | Actor/quyền hợp lệ hoặc từ chối; không lấy danh tính tùy ý từ form |
| Lấy tiền khả dụng của kỳ | TV2 | Actor, workspaceId, periodId | Số đã nhận, chuyển tiếp, đã phân bổ, chưa phân bổ; không tự nhận lương dự kiến |
| Xác nhận phân bổ | TV2 | Actor, periodId, version, danh sách phong bì/số tiền | Kế hoạch CONFIRMED khi tổng chính xác; nếu chưa đủ thì báo rõ phần thiếu |
| Tạo thu/chi | TV3 | Actor, workspaceId, accountId, envelopeId, amount, date, requestId, version | Giao dịch và số dư sau lưu, hoặc lỗi có thể hiển thị |
| Bù và ghi chi | TV3 | Dữ liệu khoản chi và sourceEnvelopeId | Một transaction chứa chuyển đúng phần thiếu và chi; không lưu nửa chừng |
| Kiểm tra khóa/quy tắc | TV4 | Source/target envelope, mục đích thao tác | Cho phép hoặc lỗi nghiệp vụ; TV3 gọi trong luồng chi/bù |
| Tính pacing/effort/cảnh báo | TV4 | Số dư, kỳ, cấu hình và Clock | DTO hiển thị; không tự tạo giao dịch |
| Tổng kết kỳ | TV4 | Workspace/kỳ, trigger thủ công hoặc scheduler | Snapshot, tiền quét, tiền chuyển tiếp, kỳ mới; không chạy trùng |
| Gợi ý phân bổ | TV4 | Thu nhập mới, lịch sử tối đa 3 kỳ, tiền bảo vệ | Danh sách đề xuất, số tháng dùng và cách tính; chưa tự xác nhận |
| Lấy báo cáo | TV5 | Actor, workspaceId, kỳ/bộ lọc | DTO từ dữ liệu thật, đã lọc quyền và không cộng điều chuyển vào chi |
| Tách câu/ảnh | TV5 | Nội dung/ảnh hợp lệ và danh sách phong bì được phép | Draft có thể sửa; không cập nhật Account/Envelope |

Quy ước lỗi tối thiểu: dữ liệu form không hợp lệ, chưa phân bổ đủ, không có quyền, thiếu tiền Account, thiếu nguồn bù, nguồn khóa, kỳ đóng, dữ liệu đã đổi, dịch vụ AI/OCR không khả dụng. Controller ánh xạ lỗi thành thông báo rõ ràng thay vì chỉ hiện stack trace.

Không giữ khóa/transaction số dư trong khi chờ LLM/OCR. Phân tích bản nháp xong, người dùng xác nhận, khi đó mới mở transaction ghi tiền.

## 5. Thành viên 1 — Nền tảng, tài khoản và phân quyền

**Mục tiêu:** cả nhóm chạy được dự án; dữ liệu cá nhân và quỹ chung được bảo vệ đúng quyền.

### Đầu việc

- Dựng/củng cố Spring Boot, Maven, MySQL, Thymeleaf, Security và Validation; quản lý cấu hình môi trường.
- Tạo GitHub, nhánh `main`/`develop`, quy tắc PR, xử lý lỗi chung và README.
- Đăng ký, đăng nhập, đăng xuất; kiểm tra trùng email, mã hóa mật khẩu và khóa tài khoản.
- Admin tối thiểu: xem danh sách, khóa/mở người dùng, quản lý danh mục mặc định. Không mặc định cho Admin xem chi tiêu riêng của người khác.
- Cung cấp cách lấy User hiện tại và kiểm tra quyền dùng chung.
- Làm quản lý thành viên quỹ chung: chủ quỹ mời tài khoản đã tồn tại, người nhận chấp nhận trong ứng dụng; vai trò quản lý/ghi/xem.
- Cung cấp giao diện layout/navbar dùng chung và cấu hình bảo vệ form.
- Hỗ trợ TV4 phần xác thực của kênh thông báo; không tự gộp toàn bộ nghiệp vụ nhắc vào module đăng nhập.
- Duy trì cấu hình sandbox Docker cho app + MySQL, DB thử riêng và hướng dẫn chạy; không chạy migration/test phá dữ liệu trên DB thật.
- Tích hợp PR hằng ngày, điều phối xung đột, tạo bản demo và release.

### Sản phẩm bàn giao

`SecurityConfig`, User/Role, Workspace/Membership, Auth/Admin/Membership Controller–Service–Repository, trang auth/admin/thành viên, layout chung, README và cấu hình môi trường chạy.

### Kiểm thử tối thiểu

- Đăng ký/đăng nhập/đăng xuất; email trùng, mật khẩu sai, tài khoản khóa.
- USER không vào trang Admin; không truy cập dữ liệu cá nhân của USER khác bằng cách đổi ID.
- Người ngoài không vào quỹ chung; người xem không được ghi; lời mời chưa chấp nhận không tạo quyền truy cập.
- Thành viên khác clone và chạy được bằng hướng dẫn, không cần mật khẩu cá nhân của trưởng nhóm.

## 6. Thành viên 2 — Account, kỳ ngân sách và phân bổ phong bì

**Mục tiêu:** quản lý nguồn tiền và bảo đảm mỗi đồng tiền khả dụng được phân bổ khi xác nhận kế hoạch.

### Đầu việc

- Account tiền mặt/ngân hàng; thêm, sửa, xem, ẩn tài khoản; không xóa cứng tài khoản có lịch sử giao dịch.
- Thống nhất với TV3 cách ghi số dư mở đầu và thu nhập, tránh ghi nhận tiền hai lần.
- Quản lý kỳ ngân sách tháng với trạng thái nháp, đã xác nhận, đã đóng.
- CRUD phong bì, Category, cờ không thiết yếu, bật pacing, trạng thái khóa và chính sách tiền dư.
- Trang nhập thu nhập và phân bổ: hiển thị tổng khả dụng, đã phân bổ, chưa phân bổ; cho lưu nháp nhưng chỉ xác nhận khi đủ 100%.
- Khi có thu nhập thực nhận mới, yêu cầu phân bổ phần tăng thêm và xác nhận lại theo luồng thống nhất.
- Tích hợp khóa/mở khóa từ TV4; không cho giảm phân bổ để lách phong bì khóa.
- Tích hợp nút gợi ý phân bổ từ TV4, vẫn cho người dùng kiểm tra/sửa trước khi xác nhận.
- Cho cùng các chức năng hoạt động trong workspace cá nhân hoặc quỹ chung được cấp quyền.

### Sản phẩm bàn giao

Account, BudgetPeriod/Budget, Envelope và Controller–Service–Repository tương ứng; các trang tài khoản, kỳ, phong bì, phân bổ; dữ liệu demo.

### Kiểm thử tối thiểu

- Khả dụng 10 triệu: chia 6 triệu được lưu nháp nhưng chưa xác nhận/ghi chi; chia đủ 10 triệu được xác nhận; chia 11 triệu bị từ chối.
- Phân bổ âm hoặc khoản nhập bằng 0 bị từ chối; phong bì chưa được phân bổ có thể ở trạng thái 0.
- Phân bổ không làm thay đổi số dư Account.
- Không sửa ngân sách kỳ đã đóng hoặc dữ liệu của người/quỹ không có quyền.
- Tiền chuyển tiếp và tiền từ lương thực nhận được tính một lần; không tính lương dự kiến như tiền sẵn có.

## 7. Thành viên 3 — Giao dịch, bù ngân sách và sổ quỹ

**Mục tiêu:** mọi thao tác tiền đúng số dư, có lịch sử và rollback khi lỗi.

### Đầu việc

- Thiết kế thu/chi và điều chuyển phong bì; có thể dùng lớp Transaction trừu tượng với IncomeTransaction/ExpenseTransaction nếu nhóm chọn để thể hiện đa hình.
- Form giao dịch: Account, phong bì, Category, số tiền, ngày và ghi chú; Validation ở cả form và backend.
- Danh sách, tìm kiếm, lọc theo kỳ, ngày, loại, danh mục và phong bì.
- Thu nhập cập nhật Account một lần và thông báo phần tiền cần phân bổ cho TV2.
- Chi tiêu kiểm tra kế hoạch đã xác nhận, quyền sở hữu và đủ tiền thực tế.
- Thiếu phong bì thì mở form chọn nguồn, chỉ commit khi bù đúng phần thiếu và nguồn hợp lệ.
- Lưu `EnvelopeTransfer` với nguồn, đích, số tiền, thời gian, người thực hiện, loại/lý do; phân biệt bù chi với tái phân bổ.
- Sửa/hủy giao dịch theo quy tắc hoàn tác; nếu có bù đi kèm, phải xử lý cả giao dịch và phần bù liên quan, không làm nguồn/đích âm.
- Chặn yêu cầu trùng và xử lý xung đột số dư từ hai tab/yêu cầu đồng thời.
- Làm giao dịch quỹ chung: đóng góp từ bên ngoài, thu/chi của quỹ, người thực hiện; không tự động trừ tiền cá nhân nếu chưa có luồng chuyển rõ ràng.
- Nhận DTO đã được người dùng xác nhận từ AI/OCR; không có đường lưu bỏ qua kiểm tra nghiệp vụ.

### Sản phẩm bàn giao

Transaction/Income/Expense, EnvelopeTransfer, các Service/Controller/Repository, trang giao dịch/bù/điều chuyển/sổ quỹ và test nghiệp vụ.

### Kiểm thử tối thiểu

| Mã | Tình huống | Kết quả |
|---|---|---|
| M3-01 | Phong bì còn 200k, chi 150k | Còn 50k; Account giảm 150k |
| M3-02 | Còn 100k, chi 250k, chưa chọn nguồn | Không ghi chi, không đổi số dư |
| M3-03 | Bù 150k từ nguồn hợp lệ | Bù và chi cùng thành công; tổng phân bổ không đổi |
| M3-04 | Nguồn khóa, thiếu tiền, khác quỹ/kỳ hoặc trùng đích | Từ chối toàn bộ |
| M3-05 | Account thiếu tiền nhưng phong bì đủ | Từ chối, không tạo tiền bằng cách bù |
| M3-06 | Sửa khoản chi 100k thành 160k | Tác động ròng tăng đúng 60k; kiểm tra bù nếu cần |
| M3-07 | Hủy khoản chi có bù liên quan | Hoàn tác theo chính sách đã chốt, không âm và có dấu vết |
| M3-08 | Bấm lưu hai lần/gửi lại cùng mã yêu cầu | Chỉ tính tiền một lần |
| M3-09 | Hai yêu cầu cùng dùng số dư cũ | Không chi âm; yêu cầu xung đột được báo rõ |
| M3-10 | Thành viên chỉ có quyền xem quỹ gửi request ghi | Backend từ chối |

## 8. Thành viên 4 — Kiểm soát ngân sách, tiết kiệm và nhắc nhở

**Mục tiêu:** hoàn thiện các cơ chế đặc trưng và xử lý theo thời gian của SmartEnvelope.

### Đầu việc

- Service cảnh báo theo ngưỡng thống nhất và DTO màu/trạng thái.
- Quy tắc khóa/mở khóa, kiểm tra nguồn bù ở backend và lịch sử thao tác; TV2 tích hợp UI phong bì, TV3 gọi quy tắc khi điều chuyển.
- Daily Pacing, tính số ngày còn lại gồm hôm nay và phân biệt phong bì cố định.
- Effort Calculator cho khoản không thiết yếu; cấu hình thu nhập theo giờ.
- SavingsGoal/PiggyBank và chính sách `SWEEP_TO_PIGGY`/`CARRY_FORWARD`.
- Scheduler đóng kỳ, tự động gom đúng tiền dư, chuyển tiếp tiền bảo vệ, lưu lịch sử và chống xử lý trùng.
- Xử lý bù kỳ khi máy chủ hoạt động lại; không tự nhận lương dự kiến.
- Trang cài ngày lương, lương dự kiến, giờ nhắc lập ngân sách và giờ khép lại chi tiêu.
- Thông báo trong ứng dụng, lịch nhắc lặp 15 phút khi chưa xác nhận kế hoạch và dừng khi đã hoàn tất.
- Phối hợp TV1 nếu triển khai Web Push; mô tả rõ hành vi khi trình duyệt đóng hoặc từ chối quyền.
- Gợi ý phân bổ theo tỷ lệ chi của 1–3 tháng đã tổng kết; giữ tối thiểu tiền cần bảo vệ, xử lý làm tròn để tổng đúng thu nhập. TV2 làm màn hình nhận/sửa gợi ý.
- Tính chỉ số/huy hiệu kỷ luật, cung cấp kết quả cho báo cáo TV5.

### Sản phẩm bàn giao

EnvelopeRule/Alert/DailyPacing/Effort/Savings/MonthClose/Reminder/AllocationSuggestion Service; Settings/Notification/Savings Controller; Entity/Repository liên quan; trang cài đặt, tiết kiệm, thông báo và test Clock.

### Kiểm thử tối thiểu

- Hạn mức 1 triệu: chi 800k là vàng; chi đủ 1 triệu là đỏ; chưa phân bổ không lỗi chia 0.
- Còn 600k và 10 ngày gồm hôm nay → 60k/ngày; kiểm tra tháng 28/29/30/31 ngày.
- Thu nhập 25k/giờ, khoản chi 200k → 8 giờ; không chia cho mức giờ 0.
- Cuối tháng còn 300k ở phong bì sinh hoạt → tự gom đúng một lần, không cần xác nhận thủ công.
- Phong bì bảo vệ được chuyển tiếp theo chính sách, không bị quét nhầm.
- Máy chủ bỏ lỡ thời điểm cuối tháng → chạy bù đúng, không tạo lương hoặc tính thu nhập mới giả.
- Đến giờ nhắc mà kế hoạch còn nháp → nhắc; xác nhận đủ → dừng; không tạo thông báo trùng ngoài lịch.
- Ngày lương 31 trong tháng 30 ngày → dùng ngày 30.
- Nhắc 22h một lần/ngày; vẫn được ghi giao dịch đã xảy ra.
- Gợi ý từ lịch sử tổng đúng tiền khả dụng và không xâm phạm tiền khóa; chưa có lịch sử phải thông báo rõ.

## 9. Thành viên 5 — Dashboard, báo cáo, nhập câu và hóa đơn

**Mục tiêu:** người dùng thấy tình hình tiền rõ ràng và nhập giao dịch nhanh nhưng vẫn kiểm soát được dữ liệu.

### Đầu việc

- Dashboard dùng dữ liệu MySQL: tiền khả dụng, đã phân bổ, chưa phân bổ, đã chi, số dư và lợn đất.
- Hiển thị thanh năng lượng phong bì, trạng thái khóa, pacing và thông báo từ TV4.
- Báo cáo thu/chi theo tháng, danh mục, phong bì và workspace; không cộng điều chuyển vào thu/chi.
- Bản đồ luân chuyển: tổng tiền, số lần, nguồn bị rút nhiều nhất và đích nhận bù nhiều nhất; xem được giao dịch liên quan.
- Tổng kết tháng, tiền dư đã gom và huy hiệu do TV4 tính; không tự tạo bộ công thức khác trong UI.
- Xây dựng interface/dịch vụ phân tích câu tự nhiên và adapter LLM nếu có API; chuyển thành DTO bản nháp.
- Nhận diện số tiền, ghi chú, ngày và phong bì từ danh sách thực tế của người dùng; không tự tạo danh mục/phong bì mới khi AI trả tên lạ.
- Upload ảnh hóa đơn, giới hạn loại/kích thước tệp và kiểm tra quyền xem; xử lý OCR/vision để tạo nháp tên quán, ngày, tổng tiền, phong bì.
- Form cho sửa cả kết quả nhập câu và OCR; người dùng xác nhận mới gọi TransactionService của TV3.
- Có timeout, xử lý API lỗi/hết hạn mức, không lưu bản nháp sai và luôn có đường nhập tay.
- Không gửi mật khẩu, token hoặc dữ liệu tài chính không cần thiết tới API. Nếu không có dịch vụ OCR/LLM, báo rõ phần chưa hoàn thành; nhập tay không được gọi là OCR đã chạy.
- Dashboard/báo cáo cho quỹ chung tuân thủ Membership do TV1 cung cấp.
- Xuất CSV nếu còn thời gian sau khi hoàn tất chức năng bắt buộc.

### Sản phẩm bàn giao

Report/Dashboard Service và Controller, DTO tổng hợp, AITransactionDraft/OcrReceiptDraft, interface phân tích và adapter, trang dashboard/report/assistant/receipt; test lỗi API và xác nhận nháp.

### Kiểm thử tối thiểu

- Dashboard khớp số liệu DB, lọc đúng kỳ và không lộ dữ liệu người/quỹ khác.
- Bản đồ điều chuyển phản ánh đúng nguồn/đích và không làm tăng tổng chi.
- “Sáng nay ăn phở 65k” → nháp 65.000đ, gợi ý phong bì hợp lệ, chưa ghi DB.
- “Mua đôi giày 450k trên shopee” → nháp 450.000đ; người dùng có thể đổi phong bì.
- Hóa đơn rõ tạo đủ trường tối thiểu; ảnh mờ/thiếu tổng tiền phải yêu cầu sửa.
- API trả số tiền sai, tên phong bì lạ, timeout hoặc lỗi → không crash, không tự ghi tiền.
- Ảnh bị từ chối nếu sai định dạng/quá giới hạn; người ngoài không xem được ảnh riêng.
- Nháp hợp lệ nhưng phong bì thiếu tiền vẫn phải qua luồng bù, không bypass bằng AI.

## 10. Bảng phối hợp giữa các thành viên

| Công việc | Chủ trì | Phối hợp | Kết quả cần thống nhất |
|---|---|---|---|
| Auth, workspace, thành viên quỹ | TV1 | TV2, TV3, TV5 | User hiện tại, workspaceId, quyền xem/ghi/quản lý |
| Ghi nhận thu nhập | TV3 | TV2 | Account tăng một lần, kỳ tăng tiền cần phân bổ |
| Phân bổ và gợi ý tháng mới | TV2 | TV4 | DTO gợi ý, tiền bảo vệ, xác nhận đủ 100% |
| Ghi chi và bù | TV3 | TV2, TV4 | Kiểm tra đủ tiền, khóa, transaction và lịch sử |
| Tự động tổng kết | TV4 | TV2, TV3, TV5 | Trạng thái kỳ, chính sách dư, snapshot và số liệu báo cáo |
| Nhắc nhở | TV4 | TV1, TV5 | Lịch, người nhận, chống trùng, UI/kênh nhận |
| Báo cáo và huy hiệu | TV5 | TV3, TV4 | Công thức thống nhất; phân biệt bù và tái phân bổ |
| AI/OCR → giao dịch | TV5 | TV3 | DTO nháp, xác nhận, kiểm tra lại nghiệp vụ và chống ghi trùng |

Người sở hữu module cung cấp method/DTO trước để người khác tích hợp. Không sửa trực tiếp Repository hay Entity của module khác mà chưa trao đổi.

### 10.1. Danh sách đầu việc để tạo GitHub Issues

Mỗi dòng dưới đây tương ứng một issue; có thể tách thành subtask nhỏ hơn nhưng không gộp mất người chịu trách nhiệm. “Mốc PR” là ngày có bản chạy được kèm test để review, không phải ngày chỉ tạo xong Entity hoặc giao diện.

#### TV1

| Mã | Việc | Phụ thuộc | Đầu ra nghiệm thu | Mốc PR |
|---|---|---|---|---|
| T1-01 | Chuẩn hóa nền, dependency, layout, cấu hình và môi trường | Quyết định công nghệ ngày 1 | Cả nhóm khởi động được, có trang chung, quy tắc cấu hình và DB thử riêng | Ngày 2 |
| T1-02 | User, đăng ký, login/logout, lấy Actor và kiểm tra quyền | ERD User/Workspace | Đăng nhập thật, mã hóa mật khẩu, helper quyền dùng được từ Service khác | Ngày 3 |
| T1-03 | Admin và Category mặc định | T1-02 | Quản lý người dùng/danh mục; USER bị từ chối ở backend | Ngày 5 |
| T1-04 | Workspace/Membership và lời mời trong ứng dụng | Hợp đồng quyền đã chốt, T1-02 | Quỹ có chủ, người ghi/người xem; lời mời và test truy cập chéo | Ngày 8 |
| T1-05 | Tích hợp, hướng dẫn chạy, bản phát hành và kiểm tra môi trường | PR của các module | Build từ checkout sạch, lịch sử merge, README, checklist release | Duy trì mỗi ngày; chốt ngày 12 |

#### TV2

| Mã | Việc | Phụ thuộc | Đầu ra nghiệm thu | Mốc PR |
|---|---|---|---|---|
| T2-01 | ERD Account/kỳ/Envelope, migration kiểu tiền và DTO dùng chung | Thống nhất với TV1/TV3/TV4 | Schema, Entity và DTO được review; test tiền cơ bản vẫn đúng | Ngày 2 |
| T2-02 | Account CRUD/ẩn và số dư mở đầu | T2-01, hợp đồng Actor | Form + Service + test quyền; không xóa tài khoản có lịch sử | Ngày 4 |
| T2-03 | Kỳ nháp, phong bì và phân bổ 100% | T2-01, quy tắc khóa từ TV4 | Lưu nháp, hiển thị chưa phân bổ, xác nhận đủ tiền; backend chặn xác nhận sai | Ngày 5 |
| T2-04 | Nhận thu nhập bổ sung, phân bổ lại, khóa/kỳ đóng | T3-01, T4-01 | Không tính tiền hai lần, không lách khóa; tương thích giao dịch đang có | Ngày 6 |
| T2-05 | Phân bổ cho quỹ chung và UI nhận gợi ý lịch sử | T1-04, T4-05 | Chọn workspace đúng quyền; gợi ý chỉnh được, chưa tự xác nhận | Ngày 9 |

#### TV3

| Mã | Việc | Phụ thuộc | Đầu ra nghiệm thu | Mốc PR |
|---|---|---|---|---|
| T3-01 | Transaction/Transfer, thu nhập và hợp đồng số dư | T2-01, ERD chung | Thu ghi một lần, liên kết kỳ/Account đúng, có dữ liệu test | Ngày 4 |
| T3-02 | Thêm chi, lịch sử và lọc cơ bản | T2-02/T2-03 | Account + Envelope giảm đúng, quyền được kiểm tra, form hoạt động | Ngày 5 |
| T3-03 | Chi vượt, chọn nguồn bù và điều chuyển nguyên tử | T3-02, T4-01 | Từ chối nguồn sai/khóa/thiếu; rollback và không đổi tổng phân bổ | Ngày 6 |
| T3-04 | Sửa/hủy, request trùng và đồng thời | T3-03 | Test tác động ròng, khôi phục tiền, audit và xung đột hai yêu cầu | Ngày 7 |
| T3-05 | Giao dịch quỹ và nhận nháp AI/OCR | T1-04, DTO TV5 | Quỹ có lịch sử người ghi; draft xác nhận vẫn qua cùng kiểm tra nghiệp vụ | Ngày 9 |

#### TV4

| Mã | Việc | Phụ thuộc | Đầu ra nghiệm thu | Mốc PR |
|---|---|---|---|---|
| T4-01 | Khóa phong bì, ngưỡng và Service quy tắc | T2-01 | Interface ổn định, test nguồn khóa, không giảm phân bổ để lách | Ngày 4 |
| T4-02 | Pacing, effort và cấu hình giờ làm | T4-01, DTO kỳ/phong bì | Tính đúng cuối tháng/tháng nhuận; UI module mình dùng được | Ngày 6 |
| T4-03 | Lợn đất, chính sách dư, snapshot và scheduler đóng kỳ | T2-04, T3-03 | Gom tự động, giữ tiền bảo vệ, test Clock/catch-up/chạy lặp | Ngày 8 |
| T4-04 | Lịch lương, nhắc lập ngân sách và khép lại ngày | T1-02, T2-03 | Cài đặt + thông báo trong app, nhắc lặp/dừng đúng và không spam trùng | Ngày 9 |
| T4-05 | Gợi ý phân bổ lịch sử và huy hiệu | Snapshot/giao dịch và loại Transfer đã chốt | Công thức có test, gợi ý giữ tổng và tiền khóa; DTO cho TV2/TV5 | Ngày 9 |

#### TV5

| Mã | Việc | Phụ thuộc | Đầu ra nghiệm thu | Mốc PR |
|---|---|---|---|---|
| T5-01 | DTO tổng hợp và dashboard đầu tiên | T2-01, T3-01 | Dữ liệu thật, workspace đúng quyền, không dùng số cố định | Ngày 5 |
| T5-02 | Nhập câu, adapter LLM và bản nháp có xác nhận | Khả năng dùng API xác nhận từ ngày 2–3 | Parser/adapter, form sửa nháp, timeout và fallback; không tự lưu | Ngày 8 |
| T5-03 | Upload hóa đơn và OCR/vision | Thử dịch vụ từ ngày 2–4, chính sách tệp với TV1 | Ảnh hợp lệ tạo draft; ảnh lỗi sửa tay; quyền/tệp không lộ | Ngày 9 |
| T5-04 | Báo cáo, bản đồ điều chuyển, tổng kết/huy hiệu | T3-03/T4-05 | Số liệu kiểm chứng được theo kỳ, danh mục, phong bì và quỹ | Ngày 9 |
| T5-05 | Kiểm thử chéo nhập liệu/báo cáo, tài liệu, hỗ trợ demo | T3-05 và các task TV5 | Minh chứng test API lỗi, draft không bypass số dư, màn hình demo ổn | Ngày 11 |

Không chờ mọi dependency hoàn thành mới bắt đầu: có thể làm form/DTO/test trên hợp đồng đã chốt. Dữ liệu giả chỉ dùng lúc phát triển, phải thay bằng dữ liệu thật trước khi nghiệm thu task.

### 10.2. Điều kiện nhận việc và hoàn thành

**Một issue được nhận khi có:** mục tiêu, người sở hữu, màn hình/luồng cần làm, DTO/Entity liên quan, dependency, test chấp nhận và mốc PR. Thiếu thiết kế thì tạo issue quyết định thiết kế trước, không để mỗi người tự đoán.

**Một issue chỉ được Done khi:**

1. Backend và giao diện của luồng đã nối với nhau, dữ liệu lưu/đọc được từ DB.
2. Đã kiểm tra cả dữ liệu đúng, dữ liệu sai và quyền truy cập liên quan.
3. Nếu đổi tiền: có test số dư trước/sau, rollback và request trùng/đồng thời phù hợp.
4. Build/test liên quan đạt trên nhánh đã cập nhật từ `develop`.
5. Người khác làm lại được các bước demo/test từ mô tả PR.
6. Người review đã xác nhận, PR đã merge và luồng tích hợp được thử lại.
7. Tài liệu/migration thay đổi cùng code nếu có; phần hạn chế được ghi rõ.

Không dùng trạng thái Done cho “xong máy em nhưng chưa tích hợp”, “mới có giao diện” hoặc “AI trả nháp nhưng chưa có xác nhận/lưu đúng”.

### 10.3. Họp, review và xử lý khi bị chặn

- Đầu ngày 10–15 phút: mỗi người báo việc đã chạy được, việc hôm nay và dependency đang chờ.
- Khi đổi DTO/Entity/quy tắc: báo ngay vào nhóm, đưa ví dụ request/kết quả; không chỉ thông báo sau khi đã merge.
- Cuối ngày: cập nhật board và push commit nhỏ; có một buổi tích hợp ngắn để thử luồng đã merge.
- PR lõi tiền phải có TV2/TV3/TV4 review theo phần ảnh hưởng; TV1 điều phối, không phải người duyệt duy nhất mọi PR.
- Nếu chờ dependency quá nửa ngày làm việc: báo trưởng nhóm, chốt interface tối thiểu hoặc đổi thứ tự task; không tự viết Repository thay module khác.
- Nếu một người quá tải: giao một phần UI/test/fixture cho người hỗ trợ bằng issue riêng, giữ người chủ trì chịu trách nhiệm nghiệp vụ.

Board gợi ý: **Backlog → Ready → Doing → Review → Test tích hợp → Done**. Dùng nhãn `blocked` và ghi rõ đang chờ ai/điều gì.

Mẫu PR:

```markdown
## Issue và phạm vi
- Mã task:
- Người review:
- Chức năng thay đổi:

## Dữ liệu và hợp đồng
- Entity/DTO/migration thay đổi:
- Ảnh hưởng module khác:

## Cách kiểm tra
1. Chuẩn bị dữ liệu:
2. Thao tác:
3. Kết quả mong đợi:
- Test đã chạy và kết quả:
- Ảnh minh họa nếu có:

## Giới hạn còn lại
- Phần chưa hoàn thành hoặc phụ thuộc:
```

## 11. Kế hoạch 14 ngày

| Ngày | Công việc | Kết quả nghiệm thu |
|---|---|---|
| 1 | Đối chiếu ý tưởng; kiểm kê mã có sẵn; chốt ERD, kiểu tiền, quyền, trạng thái kỳ, DTO và dịch vụ AI/OCR | Có hợp đồng chung; mỗi việc có người phụ trách |
| 2 | Hoàn thiện nền Spring Boot/MySQL/Security/layout; cấu hình sandbox; mọi người clone và chạy | Ứng dụng khởi động ở môi trường của các thành viên |
| 3–4 | Phát triển song song các module; dựng form, Service và test; làm sớm adapter AI/OCR để phát hiện thiếu quyền/API | Có khung chạy được và không có xung đột Entity/DTO |
| 5 | Hoàn thiện thu nhập, phân bổ đủ 100%, chi tiêu và luồng bù đầu tiên | Demo được kế hoạch → chi → bù hợp lệ |
| 6 | PR đợt 1; tích hợp Auth/Account/Envelope/Transaction và khóa nguồn bù | Luồng P0 chạy xuyên suốt |
| 7 | Kiểm thử quyền, số dư, sửa/hủy, rollback, request trùng và đồng thời | Không còn lỗi nghiêm trọng ở luồng tiền |
| 8 | Tích hợp pacing, effort, lịch lương, nhắc nhở và dashboard | Cài đặt và cảnh báo dùng dữ liệu thật |
| 9 | Tích hợp quỹ chung, tổng kết tự động, gợi ý lịch sử, nhập câu và OCR | Mọi tính năng P1 có demo đầu tiên hoặc ghi rõ trở ngại |
| 10 | Kiểm thử chéo, đặc biệt scheduler/đổi tháng, quyền quỹ chung và API lỗi | Danh sách lỗi có người xử lý và hạn hoàn thành |
| 11 | Sửa lỗi; kiểm tra lại toàn luồng và dữ liệu nhiều người dùng | P0/P1 đạt tiêu chí hoặc có phạm vi hoãn được cả nhóm chấp thuận |
| 12 | Đóng băng tính năng, chỉ sửa bug; tạo bản demo cố định | Release candidate, dữ liệu demo và lệnh chạy ổn định |
| 13 | Hoàn thiện báo cáo, UML, ERD, README, test cases và minh chứng | Đủ tài liệu nộp, khớp với code thực tế |
| 14 | Tập demo và hỏi đáp từng module; kiểm tra máy trình bày | Mỗi người giải thích được phần mình phụ trách |

Không để đến ngày 8–9 mới thử kết nối AI/OCR lần đầu. Nếu không có API key hoặc dịch vụ không sử dụng được, phải phát hiện từ ngày 1–4 và thống nhất phạm vi xử lý.

### 11.1. Lịch làm việc của từng thành viên

| Ngày | TV1 | TV2 | TV3 | TV4 | TV5 |
|---|---|---|---|---|---|
| 1 | Tạo board, chốt User/quyền/môi trường | Vẽ Account/kỳ/Envelope | Chốt luồng tiền, transaction và audit | Chốt khóa, pacing, lịch lương/đóng kỳ | Chốt số liệu, DTO draft, khả năng dùng AI/OCR |
| 2 | T1-01, cung cấp contract Actor | T2-01, migration/DTO | Dựng Transaction/Transfer và test fixture | Dựng interface Rule/Clock | Thử API bằng dữ liệu giả và dựng layout dashboard |
| 3 | T1-02 và test login/quyền | Làm Account/form | Làm thu nhập, liên kết kỳ/Account | Rule khóa, trạng thái cảnh báo | Khung draft và kiểm tra lỗi API |
| 4 | Review Auth, làm Admin | T2-02, hoàn thiện phân bổ | T3-01, chi đủ tiền | T4-01, cung cấp Rule cho TV3 | Thử OCR, xác định dịch vụ có thực sự dùng được |
| 5 | T1-03; tích hợp luồng cá nhân | T2-03, test xác nhận 100% | T3-02, nối form thật | Làm pacing/effort | T5-01, đối chiếu dashboard với DB |
| 6 | Review/integrate, làm Membership | T2-04, phối hợp bù | T3-03, test rollback | T4-02; triển khai tổng kết | Làm draft nhập câu có xác nhận |
| 7 | Kiểm tra P0, sửa lỗi quyền | Test chéo Account/Envelope | T3-04, test đồng thời/sửa/hủy | Kiểm thử chi/bù; làm snapshot | Nối draft với luồng TV3; báo cáo cơ bản |
| 8 | T1-04, test vai trò quỹ | Giao diện quỹ/gợi ý | Bắt đầu T3-05 | T4-03, test Clock/chạy lặp | T5-02; hoàn thiện OCR và báo cáo |
| 9 | Kiểm tra P1 tích hợp | T2-05 | T3-05 | T4-04/T4-05 | T5-03/T5-04 |
| 10 | Test quyền cá nhân/quỹ, rà cấu hình | Test thu/chi và đối chiếu tiền | Test tổng kết và giao dịch quỹ | Test báo cáo/gợi ý và scheduler | Test input, upload, lỗi API và xác nhận draft |
| 11 | Điều phối sửa lỗi, build toàn bộ | Sửa lỗi module, kiểm tra lại sau merge | Sửa lỗi tiền, audit và request lặp | Sửa lỗi thời gian/nhắc/tiết kiệm | T5-05, sửa lỗi UI/report/assistant |
| 12 | Tạo release candidate, hướng dẫn chạy | Chốt dữ liệu demo và màn hình | Chốt test hồi quy luồng tiền | Chốt config/scheduler và test | Chốt giao diện/demo, không thêm tính năng |
| 13 | Tổng hợp báo cáo và cấu hình máy demo | ERD và phần Account/Budget | UML/sequence và test giao dịch | Công thức, test Clock, tiết kiệm/nhắc | Ảnh, báo cáo, AI/OCR và các giới hạn |
| 14 | Điều phối diễn tập và đóng gói nộp | Demo phân bổ, giải thích dữ liệu | Demo bù/rollback, giải thích code | Demo khóa/pacing/đóng kỳ | Demo dashboard/AI/OCR và lỗi dịch vụ |

Đây là mốc giao bản để review, không có nghĩa chỉ bắt đầu task đúng ngày ghi trong bảng. Các task scheduler, AI/OCR, quyền và kiểu tiền cần thiết kế/thử nghiệm từ sớm.

### 11.2. Bốn cổng kiểm soát tiến độ

| Cổng | Thời điểm | Điều kiện qua cổng | Nếu chưa đạt |
|---|---|---|---|
| G1 — Nền tảng | Cuối ngày 2 | ERD/DTO/kiểu tiền đã chốt; cả nhóm khởi động được; cách lấy Actor/quyền rõ | Giải quyết môi trường/hợp đồng trước; không cho mỗi người tự rẽ sang kiến trúc khác |
| G2 — Lõi tiền | Cuối ngày 7 | Thu → phân bổ 100% → chi/bù/khóa → báo cáo cơ bản; rollback/quyền cá nhân đúng | Tạm dừng làm đẹp/P2, tập trung lỗi cốt lõi; TV1 chia issue hỗ trợ |
| G3 — Hoàn thiện ý tưởng | Cuối ngày 9 | Các P1 đã có bản tích hợp; tính năng phụ thuộc API có bằng chứng chạy thật | Ghi tính năng chưa đạt, nguyên nhân và kế hoạch; nếu cần giảm phạm vi phải xác nhận với nhóm và đối chiếu yêu cầu giảng viên |
| G4 — Bản nộp | Cuối ngày 12 | Không còn lỗi nghiêm trọng; build từ checkout sạch, DB demo riêng, checklist/báo cáo khớp code | Không thêm chức năng; sửa lỗi hoặc công bố giới hạn rõ ràng, không che bằng mock |

Không đánh đổi tính đúng đắn của số dư, phân quyền và rollback để có thêm OCR, biểu đồ hoặc hiệu ứng.

### 11.3. Cách chia buổi làm việc

Mỗi buổi 3–4 giờ có thể chia thành: 10–15 phút cập nhật nhóm, khoảng 2–2,5 giờ làm task tập trung, 30–45 phút test/review, thời gian còn lại để tích hợp và ghi lại tiến độ. Đây là khung gợi ý, nhóm điều chỉnh theo thời gian thực tế.

Thứ tự khi bắt đầu một chức năng:

1. Viết ví dụ đầu vào/kết quả đúng và ít nhất một trường hợp bị từ chối.
2. Xác nhận Entity/DTO/quyền và Service liên quan.
3. Làm nghiệp vụ và test trước khi dành nhiều thời gian cho giao diện.
4. Nối form/template vào Service, hiển thị lỗi rõ ràng.
5. Test dữ liệu thật ở DB thử riêng, cập nhật issue và tạo PR nhỏ.

### 11.4. Rủi ro và cách xử lý

| Rủi ro | Dấu hiệu sớm | Cách xử lý |
|---|---|---|
| Entity/kiểu tiền bị đổi liên tục | DTO không khớp, merge chạm nhiều file chung | TV1 điều phối một PR migration chung từ đầu; mọi thay đổi sau đó phải có người ảnh hưởng review |
| Account và Envelope lệch tiền | Thu bị cộng đôi hoặc bù bị tính thành chi | TV2/TV3 chốt đẳng thức ở mục 4.5, dùng chung fixture và kiểm tra trước/sau |
| Có login nhưng lộ dữ liệu | Đổi ID vẫn xem được dữ liệu người khác | Test Service/Controller với hai tài khoản; query theo workspace và quyền, không chỉ kiểm tra UI |
| TV4/TV5 quá tải | Nhiều task Doing nhưng chưa có PR chạy được | Chia subtask template, fixture, test cho TV1/TV2 khi phần nền ổn; không đổi người sở hữu công thức giữa chừng |
| AI/OCR không có key hoặc lỗi | Không gọi được dịch vụ ở ngày 2–4 | Thử sớm, cài timeout, giữ nhập tay; ghi rõ LLM/OCR chưa hoàn thành nếu không dùng được |
| Scheduler làm hỏng lịch sử | Đóng tháng lặp, cộng lương giả | Clock test, khóa/kết quả đóng kỳ duy nhất, kiểm tra bảo toàn tiền; không đổi đồng hồ hệ điều hành |
| Lỗi Docker/đường dẫn Windows | Không nạp được build context trước khi compile | Giao TV1 xử lý đường dẫn/build context và kiểm chứng; nếu cần dùng bản checkout ở đường dẫn không dấu phải có hướng dẫn, không xóa/di chuyển tùy tiện bản gốc |
| Test làm mất DB cá nhân | Dùng nhầm URL hoặc profile create-drop | DB/volume thử riêng, kiểm tra cấu hình trước lệnh test; không dùng thông tin kết nối thật trong fixture |
| Làm muộn giao diện từng module | Chỉ TV5 có template, các module khác chỉ có Java | Mỗi người sở hữu UI của mình; TV1 chỉ cung cấp layout/thành phần chung |
| Tích hợp dồn cuối kỳ | Nhánh xa develop, demo từng module riêng | Merge nhỏ hằng ngày, kiểm thử lại luồng dùng chung; chốt feature freeze ngày 12 |

### 11.5. Môi trường thực hành và cách kiểm chứng

Theo lựa chọn của dự án, ưu tiên build/test/chạy trong sandbox Docker. Không chạy Maven/Java trực tiếp trên Windows chỉ để né lỗi container mà không báo cho nhóm.

TV1 cần xác nhận Docker Engine hoạt động, lỗi build context đã được xử lý, cổng 8081 dùng được và schema khởi tạo trong database sandbox riêng. Chỉ sau khi kiểm chứng mới đánh dấu G1 đạt.

Các lệnh dự kiến tại thư mục `smart-envelope`:

```powershell
# Kiểm tra cấu hình, không in thông tin cấu hình bí mật ra nhóm:
docker compose -f compose.sandbox.yaml config --quiet

# Build Java và chạy kiểm thử trong build container theo Dockerfile:
docker compose -f compose.sandbox.yaml build app

# Chạy app và MySQL sandbox sau khi build/context đã được kiểm chứng:
docker compose -f compose.sandbox.yaml up --build -d
docker compose -f compose.sandbox.yaml ps

# Xem log ứng dụng; tránh chia sẻ log có thông tin nhạy cảm:
docker compose -f compose.sandbox.yaml logs -f app

# Dừng và giữ dữ liệu:
docker compose -f compose.sandbox.yaml stop
```

Lệnh build là kiểm tra container có thể chạy code/test; không thay thế test tích hợp trên MySQL. Nhóm phải bổ sung và chạy suite MySQL riêng khi schema/migration đã thay đổi. Dockerfile/Compose là file dùng chung: người khác muốn sửa phải báo TV1.

Không gắn Docker socket hay toàn bộ thư mục người dùng vào container để xử lý lỗi. Container hóa app không tự giới hạn quyền của trình soạn thảo hoặc công cụ AI trên host.

### 11.6. Chuyển đổi dữ liệu và mã cũ

1. Dùng nhánh tích hợp và DB thử riêng; giữ nguyên bản dữ liệu cá nhân nếu có.
2. TV1/TV2/TV3 chốt ERD có User/Workspace/Account và kiểu tiền trước khi sửa nhiều module.
3. Viết migration có phiên bản cho số tiền, khóa ngoại, trạng thái kỳ và trường audit; không trông chờ chạy lại `CREATE TABLE IF NOT EXISTS` để tự thêm cột.
4. Trong fixture demo, phân biệt số dư mở đầu với thu nhập kỳ để tránh cộng đôi; dữ liệu một hồ sơ cũ chỉ chuyển khi xác định rõ người/workspace sở hữu.
5. Chuyển UI theo từng luồng, dùng cùng Service giữa API cũ và Thymeleaf trong giai đoạn chuyển tiếp.
6. Khi luồng mới và test đã đạt, mới ngừng dùng đường cũ; không xóa toàn bộ frontend hoặc database chỉ vì đổi cách tổ chức package.

## 12. GitHub và nguyên tắc phối hợp

```text
main
└── develop
    ├── feature/member1-auth-workspace
    ├── feature/member2-account-budget-envelope
    ├── feature/member3-transaction-transfer
    ├── feature/member4-rules-savings-reminders
    └── feature/member5-dashboard-ai-ocr
```

- `main` chỉ nhận phiên bản ổn định để demo/nộp; mỗi người làm nhánh riêng và PR vào `develop`.
- Đầu ngày cập nhật nhánh tích hợp, báo việc và vướng mắc; cuối ngày push và ghi rõ phần chạy được.
- PR phải nêu chức năng, thay đổi dữ liệu/DTO nếu có, cách chạy/test và ảnh giao diện liên quan.
- Thay đổi ERD, kiểu tiền, interface dùng chung hoặc quy tắc số dư cần review trước khi merge.
- Trưởng nhóm phân công người review; người sở hữu module chịu trách nhiệm sửa lỗi phần mình.
- Commit nhỏ theo chức năng. Không commit `.env`, dữ liệu MySQL, API key, ảnh hóa đơn hoặc thư mục build.
- Không chạy test có `create-drop`, migration phá dữ liệu hoặc demo reset vào DB cá nhân đang dùng thật.

## 13. Checklist nghiệm thu toàn hệ thống

### Tài khoản và quyền

- [ ] Đăng ký/đăng nhập/đăng xuất, mật khẩu mã hóa, tài khoản khóa.
- [ ] USER không vào Admin; không truy cập dữ liệu cá nhân của USER khác bằng ID trực tiếp.
- [ ] Thành viên quỹ chung có quyền đúng vai trò; người ngoài không xem/ghi được.

### Ngân sách và giao dịch

- [ ] Lương dự kiến không làm tăng tiền thực tế.
- [ ] Kế hoạch nháp có thể chưa đủ; chỉ xác nhận khi phân bổ đúng 100%.
- [ ] Chưa xác nhận kế hoạch thì không được ghi chi.
- [ ] Chi kiểm tra Account lẫn Envelope, không cập nhật thu nhập/số dư hai lần.
- [ ] Thiếu phong bì bắt buộc bù; nguồn khác đích, đúng kỳ/quỹ, đủ tiền và chưa khóa.
- [ ] Không lách khóa bằng cách giảm phân bổ trực tiếp.
- [ ] Bù + ghi chi rollback toàn bộ nếu có lỗi.
- [ ] Sửa/hủy hoàn tác đúng; chống request trùng và xử lý đồng thời không làm âm số dư.

### Tính năng thông minh và theo thời gian

- [ ] Màu cảnh báo đúng một bộ ngưỡng; không lỗi khi ngân sách 0.
- [ ] Pacing gồm hôm nay và xử lý đúng số ngày trong tháng.
- [ ] Effort Calculator đúng mức thu nhập theo giờ và khoản không thiết yếu.
- [ ] Tiền dư sinh hoạt tự động gom, tiền bảo vệ chuyển tiếp đúng chính sách.
- [ ] Đóng kỳ không chạy trùng và có xử lý kỳ bỏ lỡ.
- [ ] Cài ngày lương/giờ nhắc; nhắc khi chưa xác nhận và dừng sau khi hoàn tất.
- [ ] Nhắc khép lại chi tiêu đúng giờ; mô tả rõ giới hạn thông báo khi web đóng.
- [ ] Huy hiệu, bản đồ luân chuyển và lịch sử cuối tháng dùng dữ liệu thật.

### AI và hóa đơn

- [ ] Gợi ý phân bổ từ 1–3 tháng, giữ tiền bảo vệ và tổng phân bổ chính xác.
- [ ] Nhập câu tạo nháp; người dùng xác nhận mới lưu.
- [ ] Upload hóa đơn tạo nháp tên quán/ngày/tổng tiền/phong bì; sai thì cho sửa.
- [ ] AI/OCR lỗi có nhập tay, không tự ghi khoản sai hoặc bỏ qua kiểm tra số dư.
- [ ] Không lộ API key, token hoặc tệp hóa đơn riêng tư.

### Bàn giao

- [ ] Chạy được theo README trên máy thành viên khác; cấu hình sandbox được kiểm chứng khi sử dụng.
- [ ] Có UML, ERD, dữ liệu demo, test cases và danh sách tính năng thực sự hoàn thành.
- [ ] Mỗi thành viên có code, test, phần báo cáo và kịch bản demo riêng.
- [ ] Phần hoãn/giới hạn phải được ghi rõ, không trình bày chức năng giả lập như đã triển khai đầy đủ.

### 13.1. Các lớp kiểm thử cần có

| Lớp kiểm thử | Nội dung | Ai thực hiện |
|---|---|---|
| Unit/domain | Công thức, ràng buộc số tiền, khóa, pacing, effort, huy hiệu, thuật toán gợi ý | Chủ module |
| Service + DB | Account/Envelope, giao dịch, rollback, khóa đồng thời, migration và tính duy nhất đóng kỳ | TV2/TV3/TV4, chạy trên DB thử riêng |
| Controller/quyền | Form sai, User khác, vai trò quỹ, thao tác trực tiếp bằng ID/request | Chủ module + TV1 |
| Tích hợp | Login → nhận tiền → phân bổ → chi/bù → báo cáo → đóng kỳ | Người review chéo và TV1 |
| Ngoại vi AI/OCR | Đầu ra hợp lệ/sai, timeout, file xấu, thiếu key, xác nhận draft | TV5 + TV3 |
| Chạy từ đầu | Checkout sạch, cấu hình riêng, khởi tạo schema, build/test, mở UI | Một thành viên không viết hướng dẫn |

H2 phù hợp test nhanh nhưng không thay thế kiểm thử MySQL khi có thay đổi SQL/locking/constraint. Không gọi dịch vụ AI trả phí trong mọi test hồi quy; dùng adapter giả cho test lỗi và một bài kiểm chứng dịch vụ thật bằng dữ liệu giả, được thực hiện riêng.

### 13.2. Bộ dữ liệu demo thống nhất

- **User A:** chủ không gian cá nhân và chủ quỹ thử nghiệm.
- **User B:** có không gian cá nhân riêng; được mời vào quỹ của A, ban đầu quyền xem.
- **User C:** không thuộc quỹ của A, dùng để test truy cập trái quyền.
- **Admin:** chỉ có quyền quản trị theo phạm vi đã chốt; không mặc định xem sổ chi tiêu riêng.
- **Tiền khả dụng kỳ của A:** 10.000.000đ, ghi bằng hai khoản thu thực tế giả: 9.000.000đ vào ngân hàng, 1.000.000đ vào tiền mặt; số dư mở đầu trước đó bằng 0.
- **Phân bổ:** Ăn uống 2,5 triệu; Tiền trọ 3 triệu; Học tập 1 triệu; Đi chơi 1 triệu; Mua sắm 1 triệu; Khẩn cấp 1,5 triệu.
- **Khóa/chuyển tiếp:** Trọ và Khẩn cấp được bảo vệ, dùng CARRY_FORWARD; các mục còn lại dùng SWEEP_TO_PIGGY.
- **Thu nhập theo giờ:** 25.000đ; giờ nhắc kế hoạch 10:00; mốc khép lại ngày 22:00.
- **Ảnh OCR:** một ảnh rõ có tổng tiền biết trước, một ảnh mờ/không đọc được; không dùng thông tin cá nhân thật.
- **Lịch sử gợi ý:** ít nhất hai kỳ giả được tổng kết, có purposeKey tương ứng với phong bì hiện tại.

Mỗi test độc lập phải tạo/reset fixture riêng; không để kết quả test trước quyết định số dư test sau. Chỉ reset DB/volume thử đã được xác định, không reset dữ liệu cá nhân.

### 13.3. Các ca kiểm thử tích hợp cần ghi kết quả

| Mã | Kiểm tra | Kết quả cần quan sát |
|---|---|---|
| E2E-01 | Đăng ký và đăng nhập A, B; thử dùng ID của B từ phiên A | Không đọc/sửa chéo dữ liệu |
| E2E-02 | Nhập hai khoản thu 9 triệu + 1 triệu | Account và tiền khả dụng tổng 10 triệu, không cộng đôi |
| E2E-03 | Chia 6 triệu rồi xác nhận | Chỉ lưu nháp; chưa cho ghi chi |
| E2E-04 | Chia đủ 10 triệu rồi xác nhận | CONFIRMED; Account không đổi do phân bổ |
| E2E-05 | Ghi chi 65k Ăn uống từ ngân hàng | Ăn uống còn 2.435.000đ; ngân hàng còn 8.935.000đ |
| E2E-06 | Chọn tiền mặt chỉ có 1 triệu để chi 1,2 triệu từ phong bì đủ tiền | Từ chối vì Account thiếu; mọi số dư giữ nguyên |
| E2E-07 | Chi Đi chơi 900k rồi thử chi tiếp 250k | Khoản sau yêu cầu bù 150k, chưa bù thì chưa ghi |
| E2E-08 | Bù từ Trọ/nguồn khác quỹ/nguồn thiếu | Từ chối, không lưu nửa giao dịch |
| E2E-09 | Bù 150k từ Ăn uống và ghi khoản Đi chơi 250k | Đi chơi còn 0; Ăn uống còn 2.285.000đ; tổng chi 1.215.000đ |
| E2E-10 | Gửi lại cùng requestId/khoản chi vừa ghi | Không trừ tiền lần hai |
| E2E-11 | Hai phiên gửi lệnh trên cùng revision | Không âm tiền; yêu cầu cũ nhận thông báo xung đột |
| E2E-12 | Sửa/hủy một giao dịch trên fixture riêng | Tác động ròng đúng, phần bù liên quan được xử lý, có audit |
| E2E-13 | Gọi scheduler đóng kỳ bằng Clock test, rồi gọi lần nữa | Một kết quả đóng kỳ; không gom hai lần, không nhận lương giả |
| E2E-14 | Tạo kỳ mới từ kết quả trên | Tiền bảo vệ được chuyển tiếp; muốn chi cần có kế hoạch kỳ mới được xác nhận |
| E2E-15 | Nhắc tại giờ đặt khi còn DRAFT rồi xác nhận đủ | Nhắc theo lịch khi chưa xong, dừng sau xác nhận; không spam trùng |
| E2E-16 | Nhập “ăn phở 65k”, chưa bấm xác nhận | Chỉ có draft, DB và số dư chưa đổi |
| E2E-17 | Xác nhận draft nhưng phong bì thiếu tiền | Vẫn phải bù; AI không bypass luật |
| E2E-18 | Dùng ảnh rõ/mờ và giả lập API timeout | Nhận trường đúng hoặc yêu cầu sửa/nhập tay, không tự lưu sai |
| E2E-19 | B là người xem quỹ, C là người ngoài gửi lệnh ghi | Cả hai bị chặn theo quyền; đổi B sang người ghi mới ghi được |
| E2E-20 | Đối chiếu dashboard/report với các bản ghi | Không tính Transfer/sweep như thu hoặc chi mới; bộ lọc đúng kỳ/quỹ |
| E2E-21 | Gợi ý từ hai kỳ tỷ lệ ăn/chơi lần lượt 2:1 và 1:2 | Tỷ lệ trung bình 1:1 cho phần tiền được gợi ý, giữ phần tối thiểu bảo vệ và tổng tiền chính xác |
| E2E-22 | Khởi động lại ứng dụng với cùng DB | Lịch sử còn nguyên; kỳ bỏ lỡ được xử lý đúng, không nhân bản dữ liệu |

Các dòng dùng chung chuỗi E2E-02/04/05/07/09 phải chạy theo đúng thứ tự trên một fixture riêng. Các ca thay đổi số dư khác, nhất là sửa/hủy và thử đồng thời, dùng fixture độc lập.

Thuật toán gợi ý cần ghi rõ: tính tỷ lệ từng mục đích trong mỗi kỳ có tổng chi hợp lệ lớn hơn 0, lấy trung bình tối đa 3 kỳ gần nhất, rồi phân bổ phần tiền sau khi giữ tối thiểu số đã chi/đã bảo vệ. Kỳ tổng chi bằng 0 không được gây chia 0. Làm tròn phải được bù phần dư để giữ tổng chính xác.

### 13.4. Kịch bản demo xuyên suốt

Chuẩn bị dữ liệu giả, màn hình và ảnh hóa đơn trước buổi trình bày; không chỉnh trực tiếp số dư trong database để tạo hiệu ứng demo.

| Bước | Người trình bày | Thao tác | Điểm cần giải thích |
|---|---|---|---|
| 1 | TV1 | Login A, giới thiệu workspace và quyền | Dữ liệu cá nhân/quỹ được tách ở backend |
| 2 | TV2 | Nhận tiền, lưu phân bổ chưa đủ rồi hoàn thiện 100% | Tiền thật khác kế hoạch; vì sao chưa đủ thì chưa xác nhận |
| 3 | TV3 | Chi 65k, Đi chơi 900k, thử thêm 250k chưa có bù | Account và Envelope, thiếu 150k, chưa commit khi chưa hợp lệ |
| 4 | TV3 + TV4 | Thử nguồn khóa rồi bù từ Ăn uống | Chính sách bảo vệ được kiểm tra ở Service, transaction nguyên tử |
| 5 | TV4 | Hiển thị pacing và 200k tương đương 8 giờ | Công thức, Clock, không chia cho 0 |
| 6 | TV5 | Report, bản đồ luân chuyển, draft nhập câu/ảnh | Số liệu thật; nháp chưa tự ghi, có sửa và xác nhận |
| 7 | TV1 + TV3 | Đăng nhập B chỉ xem quỹ rồi thử ghi | Không thể vượt quyền bằng cách gửi request trực tiếp |
| 8 | TV4 | Chạy test tổng kết/catch-up bằng Clock | Quét tự động, giữ tiền khóa, chống chạy trùng |
| 9 | TV1 | Giới thiệu cách chạy, test và giới hạn còn lại | Nhóm hiểu sản phẩm, không chỉ có giao diện |

Điểm đối chiếu tiền sau đúng chuỗi demo thu/chi trên:

```text
Tổng tiền nhận                         10.000.000đ
Đã chi: 65.000 + 900.000 + 250.000       1.215.000đ
Tổng tiền còn ở các Account              8.785.000đ
  Ngân hàng                             7.785.000đ
  Tiền mặt                              1.000.000đ

Nếu đóng kỳ ngay trên fixture này, không thêm giao dịch khác:
  Chuyển tiếp Trọ + Khẩn cấp             4.500.000đ
  Gom lợn đất từ phong bì sinh hoạt      4.285.000đ
  Tổng chuyển tiếp + lợn đất             8.785.000đ
  Tổng số dư Account vẫn                 8.785.000đ
```

Demo nhập câu/OCR có ghi thêm chi tiêu phải dùng fixture khác hoặc đối chiếu lại số tổng; không tiếp tục dùng các con số cố định trên sau khi đã thay đổi dữ liệu.

### 13.5. Báo lỗi và quyết định sửa trước

Mỗi lỗi cần có: phiên bản/commit, tài khoản và workspace thử, dữ liệu ban đầu, bước tái hiện, kết quả mong đợi/thực tế, mức ảnh hưởng, người phụ trách và kết quả test sau sửa. Không đưa mật khẩu/API key vào issue.

- **Chặn release:** mất/tạo tiền sai, lộ dữ liệu, ghi chi trùng, rollback lỗi, đóng kỳ lặp hoặc luồng chính không chạy được.
- **Cần sửa trước demo:** lọc sai báo cáo, nhắc sai điều kiện, AI draft không chỉnh được, UI không hiển thị lỗi nghiệp vụ.
- **Có thể để sau nếu ghi rõ:** lỗi căn chỉnh nhỏ, thiếu hiệu ứng, báo cáo mở rộng ngoài phạm vi.

Ngày 12 không merge tính năng mới để xử lý một lỗi trang trí. Bản demo phải ưu tiên tiền, quyền và khả năng chạy lại.

## 14. Sử dụng AI hỗ trợ làm bài

- Chỉ yêu cầu AI sửa module được giao; cung cấp Entity/DTO/hợp đồng Service hiện có.
- Không yêu cầu tạo lại toàn bộ dự án để giải quyết một chức năng nhỏ.
- Đọc và giải thích được code do AI hỗ trợ; kiểm thử dữ liệu sai và trường hợp đồng thời.
- Không chấp nhận chỉ vì IDE hết báo lỗi: phải đối chiếu số dư, quyền sở hữu và rollback.
- AI đề xuất đổi database hoặc interface chung thì trao đổi cả nhóm trước khi áp dụng.

Mẫu yêu cầu:

> Bạn hỗ trợ module [TÊN MODULE] của SmartEnvelope. Dự án dùng Java 17+, Spring Boot/MVC, Thymeleaf, JPA và MySQL. Đọc các Entity, DTO, Service hiện có trước. Chỉ sửa file thuộc module được giao; không tự đổi quan hệ dữ liệu hoặc interface dùng chung. Dùng BigDecimal cho tiền, Validation, kiểm tra quyền theo User/Workspace và transaction khi cập nhật nhiều số dư. Nêu các file thay đổi, luồng xử lý, cách chạy và các test trường hợp đúng/sai. Không khẳng định AI/OCR hoặc thông báo nền đã hoạt động nếu chỉ có mock hay nhập tay.

## 15. Sản phẩm bàn giao và nội dung bảo vệ

### 15.1. Mỗi thành viên nộp những gì

| Người | Code và giao diện | Minh chứng kiểm thử | Phần báo cáo/giải thích |
|---|---|---|---|
| TV1 | Auth/Admin, Workspace/Membership, cấu hình/layout/môi trường | Login, khóa tài khoản, quyền cá nhân/quỹ, chạy từ đầu | Kiến trúc tổng thể, phân quyền, môi trường, quy trình tích hợp |
| TV2 | Account, kỳ, Envelope, phân bổ | 100%, tiền mở đầu/thu nhập, quyền và kế hoạch đóng | ERD phần tiền/kỳ, trạng thái và cách tránh tính trùng |
| TV3 | Giao dịch, điều chuyển, sửa/hủy, giao dịch quỹ | Số dư trước/sau, rollback, duplicate/concurrency | Sequence chi/bù, transaction DB, audit và chống ghi trùng |
| TV4 | Quy tắc, cấu hình tài chính, tiết kiệm, scheduler/nhắc, gợi ý/huy hiệu | Công thức, Clock, ngày đặc biệt, chạy lặp/catch-up | Chính sách khóa, công thức và tính bảo toàn tiền qua kỳ |
| TV5 | Dashboard/report, AI/OCR/draft và các trang liên quan | Đối chiếu DB, adapter lỗi, ảnh lỗi, xác nhận draft | Báo cáo, interface/adapter, giới hạn dịch vụ và dữ liệu gửi ra ngoài |

Mỗi người chuẩn bị một màn hình chính, một luồng Service, một ví dụ dữ liệu sai bị chặn và một test giải thích được. Không chỉ nộp ảnh giao diện.

### 15.2. OOP cần thể hiện và hiểu

| Nội dung | Ví dụ phù hợp | Điều phải giải thích được |
|---|---|---|
| Đóng gói | Trạng thái tiền private, thay đổi qua hành vi allocate/spend/transfer | Vì sao không cho Controller tự set số dư tùy ý |
| Trừu tượng | Hợp đồng Service/parser/OCR và Repository | Lớp gọi cần biết gì, không cần biết chi tiết nào |
| Đa hình | Parser theo quy tắc/LLM hoặc chính sách phong bì thường/được bảo vệ | Cùng interface có các cách xử lý khác nhau và test thay thế được |
| Kế thừa | Nếu dùng IncomeTransaction/ExpenseTransaction từ Transaction | Khác biệt hành vi là gì; chiến lược mapping JPA đã chọn, không chỉ khác tên lớp |
| Composition | BudgetPeriod có Envelope; Workspace có thành viên và dữ liệu tiền | Quan hệ sở hữu và vòng đời dữ liệu |
| Exception | Lỗi số dư, chưa phân bổ, nguồn khóa, không có quyền, xung đột | Lỗi được tạo ở đâu, rollback và hiển thị ra sao |
| Dependency injection | Service nhận repository, Clock, provider qua constructor | Cách thay thời gian/provider khi test và giảm phụ thuộc cứng |

Không thêm kế thừa hoặc interface rỗng chỉ để tăng số lớp. Nếu rubric yêu cầu cụ thể abstract class/interface, cả nhóm phải chốt ví dụ có ý nghĩa và thể hiện trong UML/code/test tương ứng.

### 15.3. Bộ tài liệu chung cuối cùng

- README: phần mềm cần có, cấu hình biến môi trường mẫu, khởi tạo DB, build/test/chạy/dừng, dữ liệu demo và lỗi môi trường đã biết.
- ERD: quan hệ User/Workspace/Account/kỳ/Envelope/giao dịch/Transfer/tiết kiệm/thông báo.
- UML lớp: đủ để thấy quan hệ OOP và trách nhiệm, không cần chụp toàn bộ getter/setter.
- Sequence: ghi chi có bù, đóng kỳ tự động, AI/OCR tạo nháp rồi xác nhận.
- Test cases: mã E2E, dữ liệu, kết quả thực tế, phiên bản đã chạy và người kiểm tra.
- Báo cáo phân công: việc mỗi người thực sự hoàn thành, PR/commit liên quan, không chỉ sao chép nhiệm vụ dự kiến.
- Danh sách giới hạn: P1/P2 chưa đạt, trạng thái AI/OCR, khả năng nhận thông báo khi web đóng.
- Bản phát hành: commit/tag cố định, cấu hình mẫu không có bí mật và hướng dẫn tái tạo dữ liệu demo.

### 15.4. Checklist trước khi nộp

1. Chọn một commit/tag, không sửa code riêng trên máy demo mà quên push.
2. Một thành viên khác checkout bản đó và chạy theo README trong môi trường riêng.
3. Chạy lại các test ảnh hưởng; xác nhận test MySQL và schema đúng phiên bản.
4. Đi lại kịch bản demo, đối chiếu tổng tiền và quyền.
5. Đối chiếu báo cáo/ảnh/UML/ERD với code thực tế.
6. Kiểm tra Git/tài liệu không chứa API key, mật khẩu, ảnh hoặc dữ liệu cá nhân thật.
7. Xác nhận cả 5 người giải thích được phần mình; dự phòng demo nhập tay nếu API ngoài không hoạt động.

## 16. Căn cứ và cách sử dụng kế hoạch

- Bản ý tưởng **[Y TUONG BTL JAVA].docx**: căn cứ cho phân bổ 100%, bù, khóa, pacing, effort, tự động gom tiền dư, quỹ chung, báo cáo, các chức năng AI và lịch nhắc.
- Bản **Ke_hoach_phan_cong_SmartEnvelope.docx**: căn cứ cho nhóm 5 người, 14 ngày, công nghệ Java, phân tầng, quy trình GitHub và yêu cầu bàn giao.
- Các ngưỡng màu, khoảng nhắc 15 phút, tiêu chí huy hiệu, chính sách chuyển tiếp, vai trò quỹ chung và chi tiết DTO là quy ước triển khai trong kế hoạch này; nhóm phải chốt trước khi code.

Trước khi bắt đầu, điền tên thật cho TV1–TV5, ngày bắt đầu và người review từng module. Khi thay đổi phạm vi, cập nhật cùng lúc kế hoạch, checklist và báo cáo để cả nhóm dùng một phiên bản thống nhất.
