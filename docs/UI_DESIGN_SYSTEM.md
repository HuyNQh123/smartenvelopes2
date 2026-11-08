# SmartEnvelope2 — Hệ thống thiết kế dùng chung

## Phạm vi và nguyên tắc

Step 1 thiết lập nền tảng Neutral Finance: bố cục thực dụng, phân cấp rõ, màu trung tính,
viền nhẹ, tương tác kín đáo. Không thêm framework, build system hoặc dark mode.
Không sửa nghiệp vụ, JavaScript, cấu trúc trang của Thành viên 2–5 hoặc điều hướng Step 2.

## Audit trước triển khai

- `src/main/resources/static/` ban đầu chỉ có `css/style.css` và `js/app.js`.
  Không có CSS theo module hoặc Bootstrap vendor nội bộ.
- Các template gồm auth, admin, workspace, dashboard, error và các khung account,
  envelope, transaction, budget, savings, report, alert; layout chung ở `layouts/main.html`,
  fragment ở `fragments/header.html`, `sidebar.html`, `footer.html`.
- Master layout, auth và error nạp Bootstrap 5.3.3, Bootstrap Icons 1.11.3 từ CDN.
  Master layout/error còn nạp Plus Jakarta Sans; auth dùng fallback khác. Font này đã bỏ
  để thống nhất system font khi Inter chưa có sẵn, không thêm phụ thuộc mạng cho font.
- CSS cũ có alias màu xanh dương, gradient logo/avatar, shadow và hover nâng card.
  Login/register lặp CSS nhúng cho body/auth card/header/body và ghi đè `.brand-icon`.
- Admin/workspace sử dụng Bootstrap utilities, badge subtle, table, dropdown, input group,
  alert và CSS inline cho chiều rộng, cỡ chữ, màu danh mục. Các giá trị màu danh mục động
  và CSS inline chức năng được giữ nguyên; không áp màu thương hiệu vào dữ liệu người dùng.
- JS chung chỉ lắng nghe DOMContentLoaded và ghi log. Bootstrap bundle điều khiển collapse,
  dropdown và dismiss alert. Category form liên kết color picker bằng ID và onchange.
  Giữ nguyên selector, ID, data attributes, form, validation, CSRF và fragment signatures.
- Sidebar có position/sticky/fixed, breakpoint 767.98px và `.sidebar.show`; giữ nguyên
  cơ chế này. Không tự ý thay responsive navigation.
- Các template khung của Thành viên 2–5 gọi `header(title)` và `sidebar` theo hợp đồng cũ,
  trong khi fragment hiện tại là `header` và `sidebar(activeMenu)`. Đây là vấn đề có sẵn,
  đã được xử lý trong đợt tiếp tục Step 1 bằng fragment tài nguyên riêng (xem bảng kiểm chứng bên dưới).
- Bootstrap utility có `!important` (shadow, border, padding, font weight, text color)
  vẫn có quyền ưu tiên. Không thêm override diện rộng để vô hiệu hóa chúng. Ngoại lệ:
  `.text-warning` và `.text-danger` dùng màu chữ đậm hơn nhằm bảo đảm đọc được trên nền sáng.

## Kiến trúc CSS và thứ tự nạp

1. Bootstrap 5.3.3 và Bootstrap Icons.
2. `style.css` nhập tuần tự `tokens.css`, `base.css`, `components.css`, `utilities.css`, `layout.css`, `member1.css` (Steps 2–6).
3. `layout.css` quản lý shell; `member1.css` quản lý nội dung auth/workspace/admin/error có scope riêng.
4. `extraHead` của master layout cho CSS trang khi thật sự cần.

Giữ `style.css` làm điểm nạp duy nhất để auth, error, master layout và các trang có bố cục riêng cùng nhận nền tảng.
Các trang có bố cục riêng dùng `fragments/resources :: head(title)` và `fragments/resources :: scripts`;
không dùng fragment thanh điều hướng `header` thay cho thẻ `<head>`.
CSS import dùng đường dẫn tương đối, hoạt động cả khi triển khai dưới context path.
Không nạp trực tiếp các tệp con lần nữa. HTTP/CSS import có thêm request nhưng không cần build.

- `tokens.css`: nguồn màu, spacing, radius, font, alias cũ và RGB cho Bootstrap utilities.
- `base.css`: biến Bootstrap toàn cục, typography, keyboard focus, reduced motion.
- `components.css`: Bootstrap local variables và component dùng chung.
- `utilities.css`: helper opt-in cho tiền, heading, empty state, action column.
- `layout.css`: bố cục/header/sidebar/footer dùng chung (Step 2).
- `member1.css`: `.member-page`, `.auth-page`, `.error-page`, chỉ nội dung Member 1.
- `style.css`: entry point, không định nghĩa lại token hoặc component.

## Màu sắc

| Token | Giá trị | Vai trò |
| --- | --- | --- |
| `--color-background` | `#F8F9F7` | Nền ứng dụng |
| `--color-surface` | `#FFFFFF` | Card, form, bảng |
| `--color-primary` | `#243D35` | Hành động chính |
| `--color-primary-hover` | `#1B3029` | Hover |
| `--color-primary-active` | `#14261F` | Nhấn/chọn |
| `--color-primary-subtle` | `#EAF0EC` | Nền chọn nhẹ |
| `--color-text` | `#202923` | Nội dung chính |
| `--color-text-secondary` | `#66736D` | Nội dung phụ |
| `--color-border` | `#E2E7E3` | Viền cấu trúc |
| `--color-control-border` | `#849189` | Viền control dễ nhận biết |
| `--color-success` | `#287A56` | Thành công |
| `--color-warning` | `#C78A43` | Nền/viền cảnh báo |
| `--color-warning-text` | `#795020` | Chữ cảnh báo trên nền sáng |
| `--color-danger` | `#B94A48` | Lỗi, phá hủy |
| `--color-danger-text` | `#A13D3B` | Chữ lỗi trên nền danger nhạt |
| `--color-info` | `#356B7B` | Thông tin |

Các nhóm có token hover/subtle tương ứng. Màu warning gốc dùng với chữ tối;
không dùng chữ trắng trên nền warning. Bootstrap `--bs-*-rgb` cần kênh RGB tách riêng:
khi đổi màu gốc, cập nhật RGB cùng lúc. Alias cũ như `--primary-color`, `--light-bg`,
`--card-border` tiếp tục hoạt động, nhưng code mới nên dùng token semantic.
Trạng thái luôn có chữ mô tả hoặc biểu tượng có nhãn; không chỉ dựa vào màu.

## Typography, spacing và bề mặt

- Stack: Inter, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, sans-serif.
  Inter chỉ được dùng khi có sẵn; system font hỗ trợ dấu tiếng Việt, không tải web font mới.
- Root giữ mặc định trình duyệt 16px. Body 14px, line-height 1.5; không ép cỡ chữ toàn bộ con.
- H1/H2 hoặc `.page-title`: 24px. H3/`.section-title`: 20px. H4: 18px.
  H5/H6/`.card-title`: 16px. Chữ phụ 13px, `.helper-text` 12px.
- Heading mặc định 600, body 400, label/button 500. Utility font weight cũ được giữ.
- `.money`, `.numeric`, `.financial-summary` dùng tabular numbers; summary 28px.
- Spacing: `--space-1/2/3/4/5/6/8` = 4/8/12/16/20/24/32px.
- Radius: sm 4px, md 8px, lg 10px. Border mặc định 1px.
- Không nâng card khi hover. Shadow dùng cho overlay; utility shadow có sẵn vẫn hoạt động.

## Quy ước component

- Button chuẩn `.btn` 40px tối thiểu, `.btn-sm` 32px (40px trên thiết bị pointer coarse),
  `.btn-lg` 48px. Ưu tiên chuẩn 40px cho hành động chính. Padding utility có thể tăng chiều cao.
  Primary/secondary/success/warning/danger/info và outline có hover, active, disabled;
  ghost dùng `.btn-ghost`, text dùng `.btn-link`. Dùng `disabled` thật cho button không khả dụng.
  Link disabled cần `aria-disabled="true"`, `tabindex="-1"` và xử lý ngăn kích hoạt phù hợp.
- Loading là opt-in: giữ label trong DOM để không đổi chiều rộng, thêm `.is-loading`,
  `disabled`, `aria-busy="true"`. Caller chịu trách nhiệm bật/tắt trạng thái, không thêm JS global.
- Form: `.form-control`, `.form-select`, `.input-group-text` tối thiểu 40px; textarea 96px.
  Small/large và floating label vẫn theo Bootstrap. Dùng label có `for`, ID tương ứng,
  `aria-describedby` cho helper/error. Giữ `th:field`, `th:errors`, `.is-invalid`, `.was-validated`.
  Không thay đổi display của validation feedback. Readonly/disabled có nền trung tính.
- Card dùng `.card` và `.card-body`, `.card-header`, `.card-footer`; `.card-custom` được giữ.
  Không ép card ngoài vào các section và không thêm padding cho card chứa bảng nguyên khối.
- Bảng dùng `.table`, `.table-hover`, `.align-middle` trong `.table-responsive`.
  Monetary column tự thêm `.money`, action column `.table-actions`; không suy đoán cột từ vị trí.
  `.table-sm` dành cho bảng dày. Empty row dùng `colspan` đúng và `.empty-state`.
- Badge: ưu tiên `.badge.status-active/inactive/pending/success/warning/danger/neutral`.
  Bootstrap `bg-*-subtle text-*` vẫn nhận token. Các badge nền đặc cũ vẫn giữ semantics Bootstrap.
- Alert dùng `.alert.alert-success/danger/warning/info`, `role="alert"` khi thông báo cần đọc ngay.
  Dismiss giữ `data-bs-dismiss="alert"`; nhãn đóng mới phải là `aria-label="Đóng"`.
- Modal giữ cấu trúc Bootstrap, title có ID liên kết `aria-labelledby`, header/body/footer
  cùng spacing, footer gap 8px. Không tự sửa z-index, focus trap, `.show`, `.fade`, `.modal-backdrop`.
- Dropdown giữ data attributes và Bootstrap bundle; hỗ trợ focus/hover/active/disabled.
  Pagination dùng `.pagination`, `aria-current="page"` và nhãn phân trang tiếng Việt.

## Ví dụ dùng đúng

```html
<button type="submit" class="btn btn-primary">Lưu thay đổi</button>
<button type="button" class="btn btn-outline-danger">Xóa danh mục</button>
<button type="button" class="btn btn-ghost">Hủy</button>
<button type="button" class="btn btn-primary is-loading" disabled aria-busy="true" aria-label="Đang lưu thay đổi">
  <span class="btn-label">Lưu thay đổi</span>
  <span class="spinner-border spinner-border-sm loading-spinner" aria-hidden="true"></span>
</button>
<span class="badge status-pending">Đang chờ</span>
<div class="table-responsive" tabindex="0" aria-label="Danh sách giao dịch">
  <table class="table table-hover align-middle">
    <thead><tr><th scope="col">Nội dung</th><th scope="col" class="money">Số tiền</th></tr></thead>
    <tbody><tr><td>Chi tiêu sinh hoạt</td><td class="money">−1.500.000 ₫</td></tr></tbody>
  </table>
</div>
```

## Quy tắc cho Thành viên 2–5

1. Dùng master layout `layout(title, activeMenu, content, extraHead, extraScripts)` khi tích hợp
   module; không chép lại CSS/JS vendor hoặc tạo lại header/sidebar. Nếu cần giữ bố cục riêng,
   dùng `fragments/resources :: head(title)` và `fragments/resources :: scripts`, mỗi fragment đúng một lần.
   Sidebar phải nhận `activeMenu`, ví dụ `sidebar('accounts')`. Không kết hợp hai cơ chế nạp CSS trên cùng trang.
2. Ưu tiên Bootstrap class và token. Không thêm bảng màu riêng, gradient, shadow trang trí,
   font web hoặc framework mới. Không override `.row`, `.col-*`, `.collapse`, `.show`.
3. Component mới dùng class tiếng Anh; UI, label và thông báo dùng tiếng Việt tự nhiên.
   UTF-8; ngày `dd/MM/yyyy`; tiền VND ví dụ `1.500.000 ₫`, giữ dấu âm rõ ràng.
   CSS không định dạng giá trị: dùng formatter hiện có, không sửa nghiệp vụ ở Step 1.
4. Không dùng màu duy nhất để báo lỗi hoặc trạng thái. Không xóa outline bàn phím.
5. CSS trang phải scope trong root của module và chỉ xử lý bố cục riêng; không định nghĩa lại
   `.btn`, `.card`, font body hoặc token global. Dùng extraHead sau CSS chung.
6. Kiểm tra 1440/1280/768/375/320px, focus bàn phím và reduced motion; bảng tràn nằm trong
   `.table-responsive`, không che tràn ngang toàn body để giấu lỗi layout.

## Vấn đề cố ý để lại

Responsive sidebar/collapse hiện tại và header hẹp thuộc Step 2. Inline dimension, utility
`fw-bold`, shadow, rounded lớn và nội dung dashboard hướng dẫn phát triển thuộc bước chỉnh trang.
Không thay nhãn vai trò enum, formatter tiền/ngày hoặc các chuỗi accessibility tiếng Anh cũ
trong Step 1. Các ví dụ mới phải tuân thủ vi-VN. Các trang Member 2–5 nay đã nạp tài nguyên chung
nhưng vẫn giữ bố cục và các TODO nghiệp vụ ban đầu; kiểm chứng CSS không đồng nghĩa module nghiệp vụ đã hoàn thiện.
Repository còn thiếu `static/favicon.ico` dù layout/error tham chiếu tới tài nguyên này;
log ứng dụng ghi nhận NoResourceFoundException khi trình duyệt yêu cầu favicon. Không thêm
tài sản thương hiệu ngoài phạm vi Step 1.

## Bằng chứng kiểm tra đợt Step 1 ban đầu

- Chạy `& 'C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd' test`:
  BUILD SUCCESS, 112 tests, 0 failures, 0 errors, 0 skipped. Chạy trên Java 26.0.1,
  project vẫn target Java 17. MySQL cục bộ kết nối được; Flyway báo schema v7 đã cập nhật,
  không cần migration. Có warning JDK/Byte Buddy hiện hữu, không gây test fail.
- Chạy `git diff --check` và script Python kiểm tra UTF-8, CSS imports, resource references,
  biến CSS, cân bằng ngoặc, property trùng và so sánh toàn bộ thuộc tính Thymeleaf/security
  với HEAD. Đây là kiểm tra tĩnh, không thay thế parser hoặc kiểm thử tương tác.
- Chạy ứng dụng tạm ở cổng 18080 bằng Maven `spring-boot:run`.
  HTTP GET `/login` trả 200 HTML; `/css/style.css`, `/css/tokens.css`, `/css/base.css`,
  `/css/components.css`, `/css/utilities.css` đều trả 200, content type `text/css`.
- Trình duyệt in-app xác nhận computed style trên login: body 14px, nền rgb(248,249,247),
  button primary rgb(36,61,53), input cao 40px. Đã xem ảnh desktop 1440px và mobile 320px.
  Đo `documentElement.scrollWidth === innerWidth` ở 1440, 1280, 768, 375, 320px trên login:
  không tràn ngang. Có focus rõ trên input và thông báo lỗi giữ chữ/icon cùng màu trạng thái.
- Các cặp chữ chính/phụ, primary, success, warning, danger và info trên nền tương ứng
  được tính tương phản theo WCAG; danger dùng token chữ đậm sau khi phép đo ban đầu
  phát hiện màu danger gốc chỉ đạt 4,42:1 trên nền subtle.
- Chưa kiểm chứng end-to-end trang sau đăng nhập, trang đăng ký trên mọi breakpoint,
  dropdown, modal, pagination hoặc navigation mobile. Bootstrap JS và markup chức năng
  giữ nguyên nhưng không coi đó là bằng chứng tương tác đã pass. Công cụ trình duyệt có
  timeout và trạng thái DOM/screenshot không đồng nhất khi chuyển trang; không suy diễn kết quả.
- Chưa kiểm tra riêng reduced-motion bằng trình duyệt hoặc trình đọc màn hình; CSS có
  media query tương ứng và quy tắc focus-visible. Không tuyên bố accessibility toàn ứng dụng đạt chuẩn.


## Tiếp tục Step 1 — tích hợp đủ 27 trang

### Thay đổi có giới hạn

Thêm `templates/fragments/resources.html` với hai fragment:

```html
<head th:replace="~{fragments/resources :: head('Danh sách tài khoản')}"></head>
<!-- Giữ nguyên body/bố cục/form của trang. -->
<div th:replace="~{fragments/sidebar :: sidebar('accounts')}"></div>
<!-- Đặt cuối body, sau nội dung và footer. -->
<th:block th:replace="~{fragments/resources :: scripts}"></th:block>
```

`head(title)` chứa UTF-8, viewport, title, Bootstrap 5.3.3, Bootstrap Icons 1.11.3 và một link
`/css/style.css`. `scripts` nạp Bootstrap bundle và `app.js` đúng một lần, phục vụ dropdown
trong sidebar dùng chung. Không sửa JavaScript hoặc thêm xử lý nghiệp vụ. Không thêm thanh
điều hướng vào các trang cũ, không chuyển chúng sang master layout hoặc đổi lớp bố cục.
Chỉ sửa 12 trang bằng ba thay đổi: lời gọi head, tham số sidebar và fragment script cuối body.
Không chỉnh form action, validation, nội dung tiếng Việt, ID hoặc class trong nội dung trang.

### Bảng kiểm chứng từng trang

| Template (tương đối từ `templates/`) | Cơ chế nạp | Render gốc | Render context path |
| --- | --- | --- | --- |
| `account/form.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `account/list.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `transaction/form.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `transaction/list.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `envelope/form.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `envelope/list.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `budget/form.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `budget/list.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `savings/form.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `savings/list.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `report/index.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `alert/list.html` | Fragment resources — đã sửa | Đạt | Đạt |
| `auth/login.html` | Link CSS trực tiếp | Đạt | Đạt |
| `auth/register.html` | Link CSS trực tiếp | Đạt | Đạt |
| `error/404.html` | Link CSS trực tiếp | Đạt | Đạt |
| `error/500.html` | Link CSS trực tiếp | Đạt | Đạt |
| `dashboard/index.html` | Master layout | Đạt | Đạt |
| `admin/categories.html` | Master layout | Đạt | Đạt |
| `admin/category-create.html` | Master layout | Đạt | Đạt |
| `admin/category-edit.html` | Master layout | Đạt | Đạt |
| `admin/users.html` | Master layout | Đạt | Đạt |
| `admin/user-detail.html` | Master layout | Đạt | Đạt |
| `workspaces/index.html` | Master layout | Đạt | Đạt |
| `workspaces/create.html` | Master layout | Đạt | Đạt |
| `workspaces/detail.html` | Master layout | Đạt | Đạt |
| `workspaces/invitations.html` | Master layout | Đạt | Đạt |
| `workspaces/join.html` | Master layout | Đạt | Đạt |

### Phương pháp và giới hạn

- `DesignSystemTemplateIntegrationTest` dùng Spring MVC MockMvc và SpringTemplateEngine thật,
  render toàn bộ template bằng controller chỉ nằm trong mã test và model mẫu trong bộ nhớ.
  Không thêm endpoint production, không gọi service/repository hoặc kết nối DB trong test này.
- 54 lần render (27 trang × đường dẫn gốc/context path `/smartenvelope`) và một kiểm tra
  danh mục/tài nguyên: **55 tests đạt, 0 failures/errors/skipped**.
- Lệnh kiểm tra riêng: `mvn -Dtest=DesignSystemTemplateIntegrationTest test`.
  Lệnh kiểm tra hồi quy: `mvn test` đạt **167 tests, 0 failures/errors/skipped, BUILD SUCCESS**
  (112 tests hiện có + 55 tests tích hợp tài nguyên). Maven sử dụng bản đi kèm NetBeans như đợt đầu.
- HTTP GET năm CSS tại `localhost:8080` đều trả **200, text/css** và nội dung trùng với mã nguồn
  hiện tại. Không suy kết quả redirect về login là bằng chứng trang được bảo vệ đã render thành công.
- Mỗi HTML render có head hợp lệ với UTF-8/viewport, đúng một link Design System,
  đúng một Bootstrap CSS đứng trước nó, không nạp trực tiếp CSS con. Kiểm tra nguồn CSS entry
  nhập mỗi tệp con đúng một lần, các tài nguyên có mặt trên classpath.
- 12 trang sửa có đúng một Bootstrap bundle và một `app.js`; test xác nhận các lớp bố cục
  body/main cũ vẫn còn và không bị chuyển sang main-content của master layout.
- Script đối chiếu với HEAD xác minh 12 template chỉ thay đúng ba điểm tích hợp nêu trên.
- Test render dùng danh sách mẫu rỗng và đối tượng cần thiết cho form/detail; không kiểm thử
  mọi nhánh dữ liệu, trạng thái đăng nhập, phân quyền hoặc thao tác ghi. Security dialect/filter
  không được kích hoạt trong renderer độc lập; các kiểm thử security của ứng dụng vẫn thuộc suite hiện có.
- Đạt 27/27 về tích hợp tài nguyên/render, chưa coi đó là kiểm chứng trực quan, responsive và
  end-to-end cho cả 27 trang. Các giới hạn tương tác/reduced-motion ở đợt đầu vẫn còn áp dụng.


## Liên kết với Step 2

Hướng dẫn hiện tại về header/sidebar/footer, responsive offcanvas và cách ghép header cho
12 trang có bố cục riêng nằm tại [UI_LAYOUT_GUIDELINES.md](UI_LAYOUT_GUIDELINES.md).
Các phần audit/bằng chứng Step 1 bên trên ghi lại trạng thái tại thời điểm đó; cơ chế sidebar
đã được thay thế bằng offcanvas ở Step 2. `resources :: scripts` nay nạp thêm `layout.js`;
`sidebar(activeMenu)` ghép một header dùng chung. Không gọi header riêng bên cạnh sidebar.
Không thay đổi token/component Step 1 ngoài việc điều chỉnh chiều rộng sidebar từ 260px
thành 256px theo layout mới.


## Quy ước hiện tại sau Steps 3–6

Inventory đã tăng lên **28 trang** do thêm `error/403.html`; số liệu 27 ở các mục trước là
bằng chứng lịch sử Step 1–2. Chi tiết thay đổi và kiểm thử hiện tại ở
[MEMBER1_UI_REFACTOR_REPORT.md](MEMBER1_UI_REFACTOR_REPORT.md).

- Nội dung Member 1 dùng `.member-page` và `.member-section`; bảng trong `.table-responsive`.
- Auth dùng `.auth-page`/`.auth-card`, tối đa 440px; error dùng `.error-page`/`.error-container`.
- Role/status dùng fragment `member-ui`; label tiếng Việt, giá trị enum/field vẫn là tiếng Anh.
- Form cần loading dùng `data-submit-feedback`, button `data-loading-button` và cấu trúc
  `.btn-label`/`.loading-spinner`. `member1.js` chỉ điều khiển trình bày, không gửi AJAX hoặc thay nghiệp vụ.
- Required/maxlength/minlength bám theo DTO. Thông báo native được dịch tiếng Việt, không bỏ validation server.
- Icon danh mục giữ input tùy chỉnh kèm datalist gợi ý, không loại bỏ mã cũ/giá trị trống.
- Thay CSS trang bằng class có scope, không override form/bảng của module Member 2–5.
