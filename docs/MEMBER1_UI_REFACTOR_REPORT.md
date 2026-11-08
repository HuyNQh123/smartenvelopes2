# Báo cáo hoàn thiện giao diện Member 1 — Steps 3–6

## Phạm vi và kết quả audit

Làm việc trên `develop`, giữ toàn bộ thay đổi Step 1–2 chưa commit. Đã đọc README,
MODULE_OWNERSHIP, UI_DESIGN_SYSTEM, UI_LAYOUT_GUIDELINES; kiểm tra template, controller,
DTO validation, service phân quyền, routes và test thực tế trước khi sửa.

- Auth có POST login/register, đăng ký bằng `registerRequest`; chưa có quên mật khẩu/hồ sơ riêng.
- Workspace có danh sách, tạo mới, chi tiết/thành viên, lời mời và nhận lời mời qua token.
  Quyền OWNER/MANAGER/WRITER/VIEWER cùng cờ canBeManaged/canBeRemoved đã có ở server.
- `/admin` chuyển hướng sang `/admin/users`, không có dashboard quản trị riêng hoặc số liệu tổng hợp.
  Giữ nguyên chuyển hướng, thiết kế lại danh sách người dùng làm điểm vào quản trị thực tế.
- Category có các action tạo/sửa/ẩn/sử dụng lại/xóa và field icon/color tùy chỉnh.
- Dashboard cũ không có model số liệu tài chính: hiển thị các giá trị hardcode, panel Master Layout,
  code Thymeleaf và nút phân công TV2–TV5. Login còn hiển thị thông tin đăng nhập demo.
- Error 404/500 trước đây in trực tiếp errorMessage; thiếu view 403. Giữ handler hiện có,
  thay bằng thông điệp trình bày an toàn và thêm template error/403 theo cơ chế Spring Boot.

## Giao diện đã triển khai

### Auth — Step 3

- Form tối đa 440px, nền trung tính, bỏ demo credentials và thông điệp quảng bá dài.
- Nhãn tiếng Việt, autocomplete phù hợp, required/minlength/maxlength khớp DTO hiện có.
- Giữ nguyên th:field, POST, CSRF và các trạng thái lỗi/logout/đăng ký thành công.
- Hiện/ẩn mật khẩu bằng button, aria-controls, aria-pressed và nhãn thay đổi theo trạng thái.
- Lỗi server gần trường, aria-invalid/aria-describedby; dịch thông báo ràng buộc HTML sang tiếng Việt.
  Không thêm quy tắc mật khẩu mới hoặc thay thế validation server.
- Submit có loading opt-in; không disable input hoặc submitter có name, tránh mất dữ liệu POST.
  Khôi phục nút khi quay lại trang bằng pageshow. Chưa kiểm chứng mọi trường hợp bfcache/network timeout.

### Workspace, thành viên, lời mời — Step 4

- Danh sách quỹ dùng hàng gọn với tên, loại quỹ, chủ quỹ, số thành viên, vai trò và trạng thái hiện tại.
- Có empty state riêng khi chưa có quỹ/lời mời/thành viên. Mọi hành động đều đi đến route có sẵn.
- Form tạo và chỉnh sửa quỹ có nhãn, giới hạn đúng DTO và feedback cho tên/mô tả.
- Bảng thành viên giữ khoảng rộng tên/email; bảng cuộn trong container trên màn hình hẹp.
  Hiển thị vai trò và trạng thái membership từ DTO thật; không thêm dữ liệu mới vào backend.
- Giữ mọi action và điều kiện OWNER/MANAGER/canBeManaged/canBeRemoved. Chuyển chủ sở hữu,
  xóa thành viên, rời/xóa quỹ tiếp tục có xác nhận; quỹ cá nhân không có form rời/xóa.
- Dropdown phân quyền dùng Popper fixed để không bị cắt bởi vùng bảng responsive.
- Lời mời có trạng thái chờ, vai trò tiếng Việt và hạn dd/MM/yyyy HH:mm; email không khớp
  không có form chấp nhận. Token chỉ nằm trong hidden input cần thiết, không in ra nội dung.
- Không tạo workspace selector thứ hai: switcher chính ở header giữ nguyên.

### Quản trị — Step 5

- Giữ `/admin` → `/admin/users`. Không tạo route/dashboard giả hoặc thống kê hardcode.
- User list: tìm kiếm, lọc role/status, bảng, status badges, chi tiết, khóa/mở khóa và phân trang.
  Phân trang giữ các tham số tìm kiếm/lọc; hiển thị cửa sổ trang gọn và aria-current.
- Chi tiết tài khoản chỉ dùng thông tin có thật; không hiển thị thuật toán/kiến trúc nội bộ.
  Giữ bảo vệ tài khoản admin hiện tại; quy tắc admin cuối cùng tiếp tục do service kiểm tra.
- Category list/forms có nhãn loại/trạng thái tiếng Việt, feedback name/type/icon/color,
  xác nhận thao tác ẩn/xóa. Icon vẫn là field tùy chỉnh tự do với datalist gợi ý; không làm mất
  mã icon cũ hoặc khả năng để trống. Color picker và text input đồng bộ bằng JS dùng chung.

### Dọn nội dung phát triển và QA — Step 6

- Bỏ panel “Khung Giao Diện Dùng Chung (Master Layout v1.0)”, hướng dẫn Thymeleaf,
  thông báo sẵn sàng phát triển và các nút `(TV2)`–`(TV5)` khỏi landing.
- Bỏ bốn số liệu tài chính hardcode và lời chào emoji; không thay bằng số liệu giả mới.
  Landing nay chỉ hiển thị workspace thật từ model, đường dẫn thành viên/lời mời hoặc empty state.
  Đây là ngoại lệ dọn nội dung dashboard được người dùng yêu cầu rõ ràng; không xây nghiệp vụ Member 5.
- Bỏ thông tin tài khoản kiểm thử khỏi login; không sửa tài khoản/mật khẩu trong DB.
- Error 403/404/500 dùng thông điệp an toàn, không in errorMessage/stack trace từ server.
- Không xóa TODO nguồn ở module ngoài phạm vi vì từ khóa; không sửa template tài chính của Member 2–5.

## Kiến trúc và quy ước tích hợp

- `member1.css` được import đúng một lần ở cuối `style.css`, sau nền tảng/layout.
  Style nội dung được scope bằng `.member-page`, `.auth-page`, `.error-page`.
- `member1.js` nạp sau Bootstrap/app/layout, qua master layout hoặc resources::scripts.
  Tương tác chỉ gắn vào data attribute hoặc form Member 1; không thay JS nghiệp vụ module khác.
- `fragments/member-ui.html` gom roleLabel, userStatus, invitationList và categoryFields.
  Không thay chữ ký fragment layout/header/sidebar/footer/resources hiện có.
- Tất cả template Member 1 có lang vi, UTF-8, H1 rõ ràng và nhãn trường có liên kết ID hợp lệ.
- Không thu nhỏ font để ép bảng vừa màn hình; giữ cột tên/email và `.table-responsive`.
- Badge neutral/inactive trong Member 1 dùng token chữ chính trên nền neutral: tương phản 13,15:1,
  thay cho chữ phụ 4,35:1 trên cùng nền; không tạo màu mới hoặc thay quy tắc trạng thái.
- Xác nhận destructive dùng confirm nguyên bản với lời nhắc tiếng Việt; không áp dụng cho lọc/nhập liệu thường.
- Không thêm framework, thư viện/build tool hoặc sửa cấu hình Spring Security.

## Danh sách file của đợt Steps 3–6

| File | Thay đổi |
| --- | --- |
| `templates/auth/login.html` | Thiết kế login, bỏ credentials demo, thêm toggle/loading |
| `templates/auth/register.html` | Thiết kế register và feedback từng field |
| `templates/workspaces/index.html` | Danh sách quỹ gọn và lời mời chờ |
| `templates/workspaces/create.html` | Form tạo quỹ, validation tên/mô tả |
| `templates/workspaces/detail.html` | Thành viên, lời mời, cài đặt, thao tác nhạy cảm |
| `templates/workspaces/invitations.html` | Lời mời nhận được và empty state |
| `templates/workspaces/join.html` | Tiếp nhận token, thông tin/hạn mời, email mismatch |
| `templates/admin/users.html` | Điểm vào admin, tìm/lọc, bảng, khóa/mở, phân trang |
| `templates/admin/user-detail.html` | Thông tin tài khoản và thao tác phù hợp |
| `templates/admin/categories.html` | Danh sách/lọc, trạng thái và thao tác danh mục |
| `templates/admin/category-create.html` | Form tạo dùng fragment fields |
| `templates/admin/category-edit.html` | Form sửa dùng fragment fields |
| `templates/error/403.html` — mới | View 403 tương thích error resolver của Boot |
| `templates/error/404.html` | Thông báo không tìm thấy an toàn |
| `templates/error/500.html` | Thông báo sự cố chung, không lộ exception |
| `templates/dashboard/index.html` | Chỉ dọn dev UI/số liệu giả, dùng workspace model có sẵn |
| `templates/fragments/member-ui.html` — mới | Badge, danh sách lời mời, fields danh mục tái sử dụng |
| `templates/layouts/main.html` | Nạp member1.js một lần |
| `templates/fragments/resources.html` | Nạp member1.js cho trang dùng fragment tài nguyên |
| `static/css/style.css` | Nạp member1.css, chuyển styling auth sang scope mới |
| `static/css/member1.css` — mới | Auth/workspace/admin/error và responsive |
| `static/js/member1.js` — mới | Toggle password, loading, localized validity, color sync |
| `src/test/.../ui/DesignSystemTemplateIntegrationTest.java` | 28 trang × context path × dữ liệu rỗng/có dữ liệu |
| `src/test/.../ui/Member1FormsUiTest.java` — mới | Validation/form/security/controller và quyền hiển thị |
| `src/test/.../ui/ErrorPageIntegrationTest.java` — mới | Error dispatch 403/404/500 thật |
| `docs/UI_DESIGN_SYSTEM.md` | Ghi quy ước scope/form và inventory mới |
| `docs/UI_LAYOUT_GUIDELINES.md` | Liên kết trạng thái mới, giữ bằng chứng Step 2 lịch sử |
| `docs/MEMBER1_UI_REFACTOR_REPORT.md` — mới | Báo cáo này và giới hạn kiểm chứng |

Đường dẫn templates/static trong bảng tương đối từ `src/main/resources/`.
Các thay đổi Step 1–2 còn trong working tree nhưng không được coi là thay đổi mới của đợt này.

## Kiểm thử đã thực thi

### Tự động

- `mvn -Dui.preview.dir=target/member1-preview test`, bằng Maven đi kèm NetBeans.
  **BUILD SUCCESS: 257 tests, 0 failures, 0 errors, 0 skipped**. Kết quả cuối được ghi tại
  `target/member1-full-test.log` và Surefire reports.
- Inventory hiện tại: **28 trang**, gồm 23 application views và 5 auth/error views.
  **113 tests render/tài nguyên** = 28 × 2 context path × 2 trạng thái dữ liệu + 1 inventory.
- Giữ 112 tests controller/service/application hiện có và 6 tests shared layout/security.
  Bổ sung 23 tests form Member 1 với controller và security filter thật, service mock;
  cùng 3 tests Spring Boot BasicErrorController chọn đúng error view/status 403/404/500.
- HTTP ứng dụng thật `localhost:8080`: `style.css`, `member1.css`, `member1.js` trả 200,
  MIME đúng và nội dung khớp source; login mới không chứa demo credentials. Một POST login
  sai bằng thông tin kiểm thử không tồn tại, có CSRF và session cookie, chuyển đúng về
  `/login?error=true`. Không tạo tài khoản hoặc thay dữ liệu người dùng để thực hiện kiểm tra này.
- Đã thực thi đăng nhập hợp lệ/sai qua DaoAuthenticationProvider với UserDetails fixture và BCrypt,
  đăng ký lỗi/thành công, mật khẩu không khớp/email trùng, tạo quỹ lỗi/thành công, bốn vai trò thành viên,
  bảo vệ quỹ cá nhân, mời email sai/đúng, token email mismatch, admin redirect, search/filter/page,
  khóa/mở tài khoản, category validation/create và giữ custom icon.
- Các kiểm tra controller/service này dùng mock, không phải quy trình tạo dữ liệu thật trong DB.
  Các integration tests hiện có vẫn dùng MySQL cục bộ; không có schema/migration mới.
- Script so hash với baseline đầu phiên: backend, migration và 12 template nghiệp vụ Member 2–5 không đổi.
  Tập `th:action` và `th:field` cũ của auth/admin/workspace vẫn có đầy đủ sau khi gom fragment.
- Kiểm tra UTF-8, CSS import/token/resource, `node --check member1.js`, `git diff --check`.
- Một lần test đầu gặp lỗi classpath javac tạm thời dù class production có mặt; chạy lại không sửa
  backend đã qua được. Các kết quả pass chỉ ghi nhận ở những lần chạy thực sự thành công.

### Trình duyệt

- Dùng server preview chỉ bind localhost, đọc HTML render từ template thật với fixture;
  không đăng nhập giả vào production, không xử lý POST, không ghi DB. Có trạng thái populated,
  empty, lỗi đăng ký/tạo quỹ/danh mục, vai trò thành viên và email mismatch.
- 16 trang Member 1/landing/error × 6 chiều rộng 1440/1280/1024/768/375/320 = **96 phép đo**.
  Không có scrollWidth vượt clientWidth hoặc label[for] trỏ tới ID thiếu trong các phép đo này.
- Đã xem ảnh thực tế login desktop, register validation mobile, workspace list mobile,
  chi tiết thành viên desktop, category form và bảng user/category desktop. Ảnh các trang khác
  được chụp/đo không đồng nghĩa mọi chi tiết đều được review bằng mắt.
- Đã kiểm tra password toggle bằng click/Space, đổi nhãn/aria-pressed/type; color text cập nhật picker;
  validation HTML tiếng Việt; chọn radio sau lỗi không giữ customValidity sai; submit không bị khóa
  khi form chưa hợp lệ; dropdown phân quyền hiển thị bên ngoài vùng bảng mà không bị cắt.
- Thử confirm khóa tài khoản trong preview gây timeout CDP; sau đó preview nhận POST và trả 405.
  Không có dữ liệu thật bị tác động; không đánh dấu luồng hủy/đồng ý confirm đã kiểm chứng qua browser.
- Ảnh và JSON bằng chứng: `target/member1-screenshots/`, không đưa vào Git. Ảnh Step 2 cũ ở
  `target/layout-screenshots/` cung cấp tham chiếu trước cleanup nếu còn tồn tại.

## Giới hạn và việc ngoài phạm vi

1. Chưa có phiên browser với tài khoản thật để chạy end-to-end toàn bộ quy trình ghi dữ liệu.
   Tài khoản demo cũ đã từng bị ứng dụng từ chối; không thay tài khoản/mật khẩu hoặc tự tạo dữ liệu thử
   khó dọn trong database người dùng. Kết quả security/controller mock không thay thế E2E persistence.
2. Quyền membership, admin cuối cùng, token validation, gửi email và quy tắc category vẫn do
   backend hiện có quyết định. Không sửa backend để đơn giản hóa giao diện.
   `EmailService.sendWorkspaceInvitationEmail` hiện chỉ ghi log DEV, không gọi SMTP;
   không coi tạo lời mời thành công là bằng chứng email đã được chuyển phát. Cần xử lý
   riêng ở backend nếu muốn nhận liên kết mời trong hộp thư thật.
3. Các form invite/update workspace hiện redirect và chỉ cung cấp flash error ở một số nhánh;
   không tự thêm BindingResult/model mới. Hiển thị feedback chi tiết gần field chỉ khi server cung cấp.
4. Không sửa semantics HTTP của GlobalExceptionHandler hiện có; chỉ view lỗi được làm an toàn.
5. Chưa kiểm tra bằng trình đọc màn hình hoặc bật giả lập reduced motion trong browser;
   quy tắc focus/reduced motion từ Step 1 được giữ nguyên.
6. Bảng `transaction/list.html` ngoài phạm vi vẫn thiếu wrapper responsive như đã ghi ở Step 2.
   Các module tài chính có TODO/nghiệp vụ chưa hoàn thiện vẫn giữ nguyên; không suy kết quả 16 trang
   Member 1 thành cam kết toàn bộ sản phẩm không còn lỗi responsive/chức năng.
7. Favicon thiếu vẫn là vấn đề tài nguyên có sẵn, không tạo thêm tài sản thương hiệu ngoài yêu cầu.

Đã triển khai các giao diện Steps 3–6. Chỉ đánh dấu các hạng mục có bằng chứng bên trên;
chưa tuyên bố hoàn tất kiểm chứng toàn bộ sản phẩm/E2E hoặc mọi tiêu chí accessibility.
Không commit, push, reset hoặc rebase.
