# HƯỚNG DẪN THIẾT LẬP CƠ SỞ DỮ LIỆU (DATABASE SETUP GUIDE) - SMARTENVELOPE

Tài liệu này hướng dẫn chi tiết từng bước cho các thành viên trong nhóm thiết lập và chạy cơ sở dữ liệu **SmartEnvelope** trên máy cá nhân (Local Development).

---

## 1. Yêu cầu môi trường
- **Hệ quản trị CSDL**: MySQL Server phiên bản 8.0 trở lên.
- **Công cụ quản trị trực quan**: MySQL Workbench hoặc DBeaver.
- **Môi trường ứng dụng**: JDK 17 trở lên, Apache Maven 3.8+.

---

## 2. Bước 1: Tạo Database trên MySQL Server

Mở **MySQL Workbench** (hoặc terminal MySQL) và thực thi lệnh SQL sau:

```sql
CREATE DATABASE IF NOT EXISTS smart_envelope 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

---

## 3. Bước 2: Chọn phương thức khởi tạo dữ liệu

Nhóm có thể lựa chọn 1 trong 2 phương thức dưới đây:

### 🌟 Cách 1: Tự động thông qua Spring Boot + Flyway (Khuyên dùng)
Dự án đã tích hợp sẵn **Flyway Migration** trong `src/main/resources/db/migration/`:
- `V1__create_core_tables.sql` (bảng `users`, `workspaces`, `memberships`, `categories`)
- `V2__create_finance_tables.sql` (bảng `accounts`, `budget_periods`, `envelopes`)
- `V3__create_transaction_tables.sql` (bảng `transactions`, `envelope_transfers`, `transaction_audits`)
- `V4__create_savings_and_notification_tables.sql` (bảng `user_finance_settings`, `savings_goals`, `piggy_banks`, `piggy_bank_entries`, `month_closes`, `notifications`)
- `V5__insert_default_categories.sql` (10 danh mục thu/chi mặc định)

**Cách chạy:**
1. Tạo file `.env` từ `.env.example` và điền mật khẩu MySQL của bạn:
   ```properties
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=smart_envelope
   DB_USERNAME=root
   DB_PASSWORD=mat_khau_cua_ban
   ```
2. Khởi chạy ứng dụng Spring Boot:
   ```powershell
   mvn spring-boot:run
   ```
3. Flyway sẽ tự động phát hiện, thực thi toàn bộ migration theo thứ tự và Hibernate sẽ validate cấu trúc bảng thành công.

---

### 🛠️ Cách 2: Thủ công qua MySQL Workbench (Dành cho việc kiểm tra độc lập)
Nếu bạn chưa muốn chạy ứng dụng Spring Boot mà muốn import ngay toàn bộ cấu trúc và dữ liệu mẫu kiểm thử vào MySQL:

1. Mở **MySQL Workbench** -> Kết nối tới Local MySQL Server.
2. Chọn menu **File** -> **Open SQL Script...** -> Chọn file `database/schema.sql` trong thư mục dự án.
3. Nhấn nút Execute (biểu tượng tia sét ⚡) để tạo toàn bộ 16 bảng và ràng buộc khóa ngoại.
4. Tiếp tục chọn **File** -> **Open SQL Script...** -> Chọn file `database/demo-data.sql`.
5. Nhấn nút Execute (⚡) để nạp bộ dữ liệu thử nghiệm chuẩn kịch bản 10.000.000 VNĐ.

---

## 4. Danh sách tài khoản demo và kịch bản thử nghiệm

Khi nạp dữ liệu từ `database/demo-data.sql`, hệ thống được chuẩn bị sẵn các tài khoản sau (Mật khẩu mặc định của tất cả tài khoản là: `password123`):

| Username | Họ tên | Vai trò hệ thống | Vai trò Workspace | Kịch bản kiểm thử |
| :--- | :--- | :---: | :---: | :--- |
| `nguyenvana` | Nguyễn Văn A | `USER` | `OWNER` (Quỹ gia đình) | Chủ quỹ có thu nhập 10.000.000 VNĐ, đã phân bổ 100% vào 6 phong bì theo quy tắc ZBB. Đã có 1 giao dịch chi tiêu 400.000 VNĐ tiền chợ. |
| `tranthib` | Trần Thị B | `USER` | `VIEWER` (Quỹ gia đình) | Thành viên gia đình được mời vào quỹ với quyền chỉ xem. |
| `lethic` | Lê Thị C | `USER` | `OWNER` (Quỹ riêng) | Người dùng độc lập có quỹ cá nhân riêng, dùng để kiểm tra tính bảo mật dữ liệu giữa các workspace. |
| `admin` | Quản Trị Viên | `ADMIN` | - | Tài khoản quản trị toàn hệ thống. |

---

## 5. Kịch bản kiểm tra toàn vẹn nghiệp vụ Zero-based Budgeting

Sau khi import dữ liệu demo, bạn có thể thực thi các câu truy vấn sau trong MySQL Workbench để kiểm tra:

### 1. Kiểm tra nguyên tắc Zero-based Budgeting (Thu nhập = Tổng phân bổ):
```sql
SELECT 
    name, 
    total_income, 
    total_allocated, 
    (total_income - total_allocated) AS remaining_unallocated,
    status 
FROM budget_periods 
WHERE id = 1;
```
*Kết quả kỳ vọng*: `total_income = 10000000`, `total_allocated = 10000000`, `remaining_unallocated = 0`, `status = 'CONFIRMED'`.

### 2. Kiểm tra danh sách 6 phong bì và tiến độ chi tiêu:
```sql
SELECT 
    name, 
    allocated_amount, 
    spent_amount, 
    remaining_amount, 
    warning_threshold 
FROM envelopes 
WHERE budget_period_id = 1;
```
*Kết quả kỳ vọng*: 6 phong bì có tổng số tiền phân bổ đúng 10.000.000 VNĐ. Phong bì "Tiền ăn & Đi chợ" có `allocated_amount = 3000000`, `spent_amount = 400000`, `remaining_amount = 2600000`.

### 3. Kiểm tra tính khớp số dư kép giữa Tài khoản và Giao dịch:
```sql
SELECT 
    a.name AS account_name,
    a.balance AS current_balance,
    (10000000 - 400000) AS expected_balance
FROM accounts a
WHERE a.id = 1;
```
*Kết quả kỳ vọng*: Tài khoản Vietcombank ban đầu có 10.000.000 VNĐ lương, sau khi chi tiêu 400.000 VNĐ thì số dư còn đúng 9.600.000 VNĐ.

---

## 6. Xử lý sự cố thường gặp (Troubleshooting)

1. **Lỗi `Access denied for user 'root'@'localhost'`**:
   - Kiểm tra lại mật khẩu root trong file `.env` hoặc thiết lập biến môi trường `DB_PASSWORD`.
2. **Lỗi `Table 'smart_envelope.users' doesn't exist` khi khởi động**:
   - Đảm bảo thuộc tính `spring.flyway.enabled=true` trong `application.properties` và Flyway đã chạy xong trước khi Hibernate validate.
3. **Lỗi `Unknown database 'smart_envelope'`**:
   - Chạy lệnh `CREATE DATABASE smart_envelope;` trên MySQL trước khi start app.

