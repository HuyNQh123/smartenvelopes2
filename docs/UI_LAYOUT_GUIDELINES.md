# SmartEnvelope2 — Hướng dẫn layout và điều hướng dùng chung

## Phạm vi Step 2 và audit

Nền tảng màu/chữ/component tiếp tục theo `UI_DESIGN_SYSTEM.md`. Step 2 chỉ thay layout,
header, sidebar, footer và tương tác điều hướng; không chỉnh nội dung trang hoặc backend.

Audit tại thời điểm bắt đầu:

- 27 template trang: 11 dùng `layouts/main`, 12 dùng `fragments/resources`, 4 auth/error
  tự nạp tài nguyên. Lỗi nạp CSS Step 1 đã được sửa; 55 kiểm thử render của Step 1 đạt.
- Layout cũ dùng Bootstrap grid để chia sidebar theo tỷ lệ, trong khi CSS có chiều rộng
  sidebar riêng. Header dùng collapse, sidebar tự transform với `.show`, hai cơ chế chưa đồng bộ.
- Các trang giữ bố cục riêng có sidebar nhưng chưa có header/nút mở điều hướng mobile.
- Header có workspace switcher, POST `/workspace/switch`, POST `/logout`; sidebar/account menu
  có `sec:authorize="hasRole('ADMIN')"`. Giữ nguyên cơ chế và các route này.
- Model dùng chung do `GlobalModelAttributeAdvice` cung cấp: `currentUser`, `currentWorkspace`,
  `currentWorkspaceId`, `availableWorkspaces`. Không thêm dependency model mới.
- Route đã có: dashboard, accounts, transactions, envelopes, budgets, savings, reports,
  alerts/notifications, workspaces, invitations, admin/users, admin/categories.
  Không có route hồ sơ/cài đặt để thêm vào menu.
- Header/sidebar có CSS inline, tên loại quỹ/role dạng enum và nhiều màu cảnh báo;
  footer hiển thị thông tin công nghệ. Step 2 thay bằng nhãn tiếng Việt và footer tối giản.
- Working tree đã chứa thay đổi Step 1 chưa commit. Không hoàn nguyên hoặc đưa chúng vào commit tự động.

## Cấu trúc master layout

Hợp đồng giữ nguyên:

```html
<html th:replace="~{layouts/main :: layout('Tiêu đề', 'accounts', ~{::#main-content}, ~{}, ~{})}">
  <body><div id="main-content">Nội dung module</div></body>
</html>
```

Layout chứa `.app-body` dạng flex, sidebar và `main#app-main.main-content`, sau đó footer.
Header fixed cao 64px; body dành sẵn khoảng trống 64px để nội dung không nằm bên dưới.
Main co giãn với `min-width: 0`, padding desktop 24px/mobile 16px. Không đặt max-width hẹp
cho main, giữ không gian cho bảng tài chính rộng. Grid Bootstrap bên trong nội dung vẫn hoạt động.

### Hợp đồng fragment và tương thích 12 trang có bố cục riêng

- `header` giữ chữ ký không tham số.
- `sidebar(activeMenu)` giữ chữ ký và nay ghép **header trước, aside sau** bằng `th:block`.
  Cả master layout và trang giữ bố cục riêng gọi sidebar đúng một lần, vì vậy đều nhận một header.
- Master layout đã bỏ lời gọi header riêng. **Không gọi cả header lẫn sidebar trên cùng trang**.
- `footer` giữ chữ ký không tham số.
- `resources :: head(title)` và `resources :: scripts` giữ chữ ký; scripts nay nạp thêm `layout.js`.

```html
<head th:replace="~{fragments/resources :: head('Danh sách tài khoản')}"></head>
<body>
  <div class="d-flex">
    <div th:replace="~{fragments/sidebar :: sidebar('accounts')}"></div>
    <main class="flex-grow-1 p-4">Nội dung module được giữ nguyên</main>
  </div>
  <div th:replace="~{fragments/footer :: footer}"></div>
  <th:block th:replace="~{fragments/resources :: scripts}"></th:block>
</body>
```

Đây là điều chỉnh tích hợp layout nhỏ cần thiết cho Step 2, thực hiện hoàn toàn trong fragment chung;
không sửa lại 12 template module. CSS nhận diện shell qua `.app-header`; JS bổ sung ID/tabindex
cho main cũ nếu thiếu để skip link hoạt động, không thay ID đã có.

## Header và workspace switcher

- Một header dùng chung: danh tính ứng dụng, nút menu khi dưới 1280px, workspace switcher,
  nút tài khoản. Không có thanh tìm kiếm/thông báo/hồ sơ giả.
- Workspace switcher hiện ở mọi kích thước; tên dài rút gọn trên nút, nhưng hiển thị đầy đủ
  trong dropdown. Nhãn trợ năng có tên quỹ hiện tại; phân biệt “Quỹ cá nhân” và “Quỹ chung”.
- Không có quỹ: hiển thị “Chọn quỹ làm việc”; danh sách rỗng có thông báo và link quản lý quỹ.
- Danh sách chỉ lấy từ `availableWorkspaces` do server cung cấp, không tải thêm danh sách qua JS.
- Mỗi lựa chọn là form POST `/workspace/switch`, input `workspaceId` giữ nguyên. Spring thêm
  CSRF qua `th:action`. JS không lưu hoặc tự đổi workspace trong localStorage/cookie.
- Menu tài khoản dùng button thật, đọc được tên/email dài, có link tổng quan, link quản trị
  chỉ dành cho ADMIN và form POST `/logout`. Không thay cấu hình bảo mật đang tồn tại.
- Avatar trong nội dung admin/workspace và biểu tượng auth giữ quy tắc Step 1;
  chỉ avatar/biểu tượng nằm trong header mới dùng kích thước 32px và nền nhẹ.

## Sidebar và quy tắc navigation

- Nhóm Tài chính giữ đủ các tính năng hiện có: tổng quan, tài khoản, giao dịch, phong bì,
  ngân sách, tiết kiệm, báo cáo, cảnh báo. Không xóa link nghiệp vụ.
- Nhóm Không gian làm việc có quản lý quỹ và lời mời `/invitations` đã có controller.
  Thành viên vẫn được truy cập qua trang chi tiết quỹ, không phát minh route mới.
- Nhóm quản trị và link quản trị trong account menu giữ `sec:authorize`.
- `navItem(route, label, icon, selected)` gom markup link, icon, active và `aria-current`.
  Active có nền nhẹ, chữ 600 và viền trái, không chỉ đổi màu.
- `activeMenu` tiếp tục cung cấp trạng thái ban đầu từ server. `layout.js` so URL các link đã
  render, chọn đường dẫn khớp dài nhất để hỗ trợ trang con. `/notifications` dùng alias của alerts.
  Context path được giữ vì so với URL do Thymeleaf tạo, không hardcode root trong JavaScript.
- Trang lời mời trước đây truyền `workspaces`; JS chọn lại đúng link `/invitations` khi tải.
  Nếu JavaScript bị tắt, giữ trạng thái server cũ; navigation link vẫn dùng URL thực.
- Không dùng `href="#"` cho tính năng chưa có. Skip link `#app-main` là đích nội dung thật.

## Responsive và trợ năng

| Chiều rộng | Chiến lược |
| --- | --- |
| Từ 1280px | Sidebar rộng 256px, sticky dưới header; ẩn nút menu/đóng |
| 768–1279px | Bootstrap Offcanvas, main dùng toàn bộ chiều rộng |
| 320–767px | Cùng offcanvas; header thu gọn brand và tên tài khoản, workspace vẫn truy cập được |

Một phần tử `#sidebarMenu.offcanvas` phục vụ cả ba nhóm; không kết hợp collapse với transform riêng.
Bootstrap quản lý backdrop, Escape, focus trap, khóa cuộn và trả focus cho trigger. Nút đóng có
nhãn tiếng Việt. `layout.js` đồng bộ `aria-expanded` qua sự kiện Bootstrap và đóng overlay khi
chuyển sang desktop để không để lại backdrop/khóa cuộn. Link điều hướng có touch target 44px.
Focus-visible và reduced motion dùng nền tảng Step 1; không tắt outline.

Tham khảo hành vi offcanvas chính thức:
[Bootstrap 5.3 Offcanvas](https://getbootstrap.com/docs/5.3/components/offcanvas/).

## Footer và vùng tiêu đề trang

Footer chỉ còn “© 2026 SmartEnvelope”, giữ năm đã có trong ứng dụng, bỏ thông tin công nghệ,
phiên bản không cần thiết và nội dung quảng bá. Footer nằm trong luồng tài liệu, không che nội dung.

Trang mới có thể dùng `.page-header`, `.page-description`, `.page-actions` cho tiêu đề,
mô tả và hành động. Không tự thêm breadcrumb nếu không giúp định hướng. Step 2 không chuyển
nội dung các trang hiện tại sang các class này.

## Tài nguyên và quy tắc cho Thành viên 2–5

1. Bootstrap CSS → Icons → `style.css` → CSS trang qua extraHead. Entry `style.css` nhập
   tokens, base, components, utilities, rồi layout. Không nạp layout.css trực tiếp lần nữa.
2. Bootstrap bundle → app.js → layout.js → script trang. Master layout/resources chịu trách nhiệm
   nạp đúng một lần. Không thêm bản Bootstrap khác hoặc ghi đè `.show`/`.collapse` toàn cục.
3. Không tự gắn header/sidebar lần thứ hai. Giữ fragment signatures ở trên.
4. Dùng route hiện hữu, nhãn tiếng Việt và icon Bootstrap đã có. Không copy điều kiện ADMIN
   sang kiểm tra bằng JS; vẫn phải có `sec:authorize` và phân quyền server.
5. Bảng phải nằm trong `.table-responsive`; không che overflow toàn body để giấu bảng quá rộng.
   CSS trang không đặt min-width cố định cho shell/main hoặc sửa `--sidebar-width` cục bộ.
6. Tên người/quỹ dài cần truncation có chỗ xem đầy đủ, không cắt dữ liệu trong model.
7. Không tự quản lý backdrop/focus/khóa cuộn ngoài Bootstrap. Không sửa backend để phục vụ layout.

## Bằng chứng kiểm thử Step 2

- `mvn -Dtest=DesignSystemTemplateIntegrationTest,SharedLayoutSecurityTest -Dui.preview.dir=target/layout-preview test`:
  61 tests đạt. Renderer thật kiểm tra 27 trang ở root và context path; mỗi trang ứng dụng có
  một header/sidebar/layout.js, auth/error không có shell, không trùng CSS hoặc Bootstrap bundle.
- `SharedLayoutSecurityTest`: security filter/dialect thật, service mock và dữ liệu trong bộ nhớ.
  USER không thấy admin; ADMIN thấy link; form chuyển quỹ/logout có CSRF; chuyển quỹ hợp lệ gọi
  handler/service hiện có; thiếu CSRF bị chặn; POST logout chuyển về login; quỹ rỗng render được.
- `mvn test`: **173 tests đạt, 0 failures/errors/skipped**. Không sửa backend hoặc migration.
- `node --check src/main/resources/static/js/layout.js` và `git diff --check`: đạt.
- Ứng dụng đang chạy tại `localhost:8080` trả 200 cho `style.css`, `layout.css`, `layout.js`;
  nội dung HTTP khớp mã nguồn cuối cùng, MIME tương ứng `text/css` và `text/javascript`.
- Preview chỉ bind `127.0.0.1:18081`, phục vụ HTML render từ template thật và CSS/JS nguồn;
  không kết nối DB hoặc chấp nhận POST. Người dùng/quỹ trong preview đều là fixture, tên rất dài.
  Đây là môi trường kiểm tra frontend, không phải phiên đăng nhập sản phẩm.
- Đã xem ảnh dashboard tại desktop 1440px và mobile 320px; kiểm tra hình học tại 1440, 1280,
  1024, 768, 375, 320px: không tràn ngang. Sidebar rộng 256px từ 1280px, ẩn ở nhóm tablet/mobile;
  main bắt đầu bên phải sidebar trên desktop và tại x=0 dưới 1280px, không bị header che.
- Mobile: đã kiểm tra nút mở, nút đóng, backdrop, Escape, khóa/giải phóng cuộn, focus trong menu,
  trả focus và cleanup khi resize sang desktop. Workspace dropdown chứa tên đầy đủ trong khung
  280px, account menu có nút logout. Không submit form trên preview.
- Đường dẫn con `/accounts/new` chọn Tài khoản; `/invitations` chọn Lời mời;
  `/notifications` chọn Cảnh báo chi tiêu. Form cũ nhận một header/sidebar, padding mobile 16px.
- Đo DOM của 27 trang ở 320px trên preview; ảnh từng trang không được coi là đã kiểm tra bằng mắt
  chỉ vì được chụp. Ngoại trừ danh sách giao dịch, các trang đo không tràn ngang. Bảng admin có
  overflow bên trong `.table-responsive`, không đẩy rộng body.
- Ảnh kiểm chứng đặt tại `target/layout-screenshots/` (không đưa vào Git).

## Giới hạn và vấn đề để lại

- Tài khoản demo `nguyenvana` hiển thị ở login bị ứng dụng cục bộ từ chối đăng nhập.
  Chưa kiểm chứng chuyển workspace/logout bằng phiên browser đăng nhập thật; không tự sửa
  tài khoản, mật khẩu hoặc cấu hình security. Kết quả Security/MockMvc không thay thế kiểm thử
  end-to-end với tài khoản và quyền membership thực tế.
- `transaction/list.html` đã có bảng không được bọc `.table-responsive`; ở 320px, preview đo
  scrollWidth 399px. Không sửa template Member 3 hoặc che overflow để giấu lỗi trong Step 2.
  Chủ sở hữu module cần bọc bảng trong bước chỉnh giao diện giao dịch.
- Không chỉnh nội dung demo, emoji lời chào, card, utility, formatter hoặc TODO nghiệp vụ trong dashboard/module.
- Chưa kiểm tra bằng trình đọc màn hình, chưa bật giả lập reduced-motion trong browser.
- Favicon thiếu là vấn đề tài nguyên có sẵn; không thêm thương hiệu mới trong Step 2.
- Chỉ xác nhận các tiêu chí có bằng chứng trên. Chưa đánh dấu toàn bộ Step 2 hoàn tất end-to-end
  khi còn giới hạn phiên đăng nhập thật và lỗi bảng giao dịch cần chủ sở hữu module xử lý.


## Cập nhật sau Steps 3–6

Phần Step 2 ở trên giữ lại audit và bằng chứng tại thời điểm đó. Trạng thái Member 1 mới nhất:
[MEMBER1_UI_REFACTOR_REPORT.md](MEMBER1_UI_REFACTOR_REPORT.md). Landing đã được dọn panel
Master Layout, nút phân công thành viên, emoji và số liệu hardcode; các route tài chính thật
vẫn nằm trong sidebar. Không xây dashboard tài chính mới hoặc đổi controller của Member 5.

`style.css` nay nạp thêm `member1.css`; master layout/resources nạp thêm `member1.js` đúng
một lần. Không thay hợp đồng fragment của Step 2. `/admin` tiếp tục chuyển tới user list,
không phát minh admin dashboard hoặc thống kê. Có thêm error view 403, nâng inventory lên 28.
