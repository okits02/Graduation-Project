# User Service API

Tài liệu chi tiết các API của `user-service` (đăng nhập, đăng ký, quản lý người dùng).

## Base URL
- Local: `http://localhost:8080` (hoặc cổng cấu hình trong `application.yml`).

## Authentication
- Hầu hết các endpoint yêu cầu JWT Bearer token trong header: `Authorization: Bearer <token>`.

## Response Format

Tất cả API trả về format chung:

```json
{
  "code": 200,
  "message": "Thành công",
  "result": { }
}
```

## Authentication Endpoints

### 1. **POST /auth/login** - Đăng nhập

**Mô tả:** Xác thực người dùng và trả về JWT token.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "authenticated": true
  }
}
```

**Status Codes:**
- `200 OK` - Đăng nhập thành công
- `400 Bad Request` - Username/password không hợp lệ
- `401 Unauthorized` - Thông tin xác thực sai

---

### 2. **POST /auth/introspect** - Kiểm tra tính hợp lệ của token

**Mô tả:** Xác minh và lấy thông tin từ JWT token.

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "valid": true,
    "verified": true
  }
}
```

**Status Codes:**
- `200 OK` - Token hợp lệ
- `401 Unauthorized` - Token không hợp lệ

---

### 3. **POST /auth/refresh** - Làm mới token

**Mô tả:** Lấy access token mới từ token cũ.

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "authenticated": true
  }
}
```

---

### 4. **POST /auth/verify** - Xác thực OTP (Đăng ký)

**Mô tả:** Xác thực email bằng mã OTP khi đăng ký.

**Request Body:**
```json
{
  "username": "john_doe",
  "otp": "123456"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "User is verify"
}
```

---

### 5. **POST /auth/forgot-password** - Xác thực OTP quên mật khẩu

**Mô tả:** Xác thực OTP và trả về token để đặt lại mật khẩu.

**Request Body:**
```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "email": "john@example.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "authenticated": true
  }
}
```

---

### 6. **POST /auth/logout** - Đăng xuất

**Mô tả:** Thu hồi JWT token (thêm vào blacklist).

**Auth:** Yêu cầu Bearer token.

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Logout successful"
}
```

---

## User Management Endpoints

### 7. **POST /users/register** - Đăng ký người dùng mới

**Mô tả:** Tạo tài khoản người dùng mới.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "0912345678",
  "dob": "1990-01-15"
}
```

**Validation:**
- `username`: Min 3 ký tự
- `password`: Min 8 ký tự
- `email`: Định dạng email hợp lệ
- `phone`: Ít nhất 10 ký tự

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "User Created",
  "result": {
    "id": "user_123",
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phone": "0912345678",
    "dob": "1990-01-15",
    "role": "USER"
  }
}
```

**Status Codes:**
- `200 OK` - Đăng ký thành công
- `400 Bad Request` - Dữ liệu không hợp lệ

---

### 8. **POST /users/verifyEmail/send-otp** - Gửi OTP xác thực email

**Mô tả:** Gửi mã OTP qua email để xác thực.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "0912345678",
  "dob": "1990-01-15"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "OTP sent to email"
}
```

---

### 9. **POST /users/forgot-password/send-otp** - Gửi OTP quên mật khẩu

**Mô tả:** Gửi OTP để reset mật khẩu.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "0912345678",
  "dob": "1990-01-15"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "OTP sent to email"
}
```

---

### 10. **PUT /users/reset-password** - Đổi mật khẩu

**Mô tả:** Thay đổi mật khẩu (cần đăng nhập).

**Auth:** Yêu cầu Bearer token.

**Request Body:**
```json
{
  "oldPassword": "old_password123",
  "newPassword": "new_password456"
}
```

**Validation:**
- Cả `oldPassword` và `newPassword` phải có ít nhất 8 ký tự

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Password changed successfully"
}
```

---

### 11. **GET /users/myInfo** - Lấy thông tin user hiện tại

**Mô tả:** Lấy profile của user đang đăng nhập.

**Auth:** Yêu cầu Bearer token.

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "id": "user_123",
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phone": "0912345678",
    "dob": "1990-01-15",
    "role": "USER"
  }
}
```

---

### 12. **GET /users/getUserId** - Lấy ID người dùng

**Mô tả:** Endpoint nội bộ để lấy user ID (dùng bởi các service khác).

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "id": "user_123"
  }
}
```

---

### 13. **PUT /users/admin/{userId}/status** - Thay đổi trạng thái user (Admin)

**Mô tả:** Admin thay đổi trạng thái tài khoản (active/inactive).

**Auth:** Yêu cầu quyền ADMIN.

**Path Parameters:**
- `userId` - ID của user cần thay đổi

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "User status updated"
}
```

---

### 14. **GET /users/admin/get-user/id/{userId}** - Lấy thông tin user (Admin)

**Mô tả:** Admin lấy thông tin chi tiết của một user.

**Auth:** Yêu cầu quyền ADMIN.

**Path Parameters:**
- `userId` - ID của user cần lấy thông tin

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "id": "user_123",
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phone": "0912345678",
    "dob": "1990-01-15",
    "role": "USER"
  }
}
```

---

### 15. **DELETE /users/{userId}** - Xóa user

**Mô tả:** Xóa người dùng (Admin).

**Auth:** Yêu cầu quyền ADMIN.

**Path Parameters:**
- `userId` - ID của user cần xóa

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "User deleted successfully"
}
```

---

## Common Response Codes

| Code | Meaning |
|------|---------|
| `200` | OK - Thực hiện thành công |
| `201` | Created - Tạo tài nguyên mới |
| `400` | Bad Request - Dữ liệu không hợp lệ |
| `401` | Unauthorized - Không xác thực |
| `403` | Forbidden - Không có quyền |
| `404` | Not Found - Tài nguyên không tồn tại |
| `500` | Internal Server Error - Lỗi server |

## Run locally

Từ thư mục `user-service`:

```bash
mvn clean install
mvn spring-boot:run
```

## Notes

- Tất cả password phải có ít nhất 8 ký tự
- Username phải có ít nhất 3 ký tự
- Điện thoại phải có ít nhất 10 ký tự
- Các endpoint admin yêu cầu token có quyền ADMIN
