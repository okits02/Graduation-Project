# Profile Service API

Tài liệu chi tiết các endpoint của `profile-service` (quản lý profile, địa chỉ, avatar).

## Base URL
- Local: `http://localhost:8081` (hoặc cổng cấu hình trong `application.yml`).

## Authentication
- Các endpoint user yêu cầu JWT Bearer token: `Authorization: Bearer <token>`.
- Admin endpoints yêu cầu thêm quyền ADMIN.

## Response Format

Tất cả API trả về format chung:

```json
{
  "code": 200,
  "message": "Thành công",
  "result": { }
}
```

## Profile Endpoints

### 1. **GET /profile/myInfo** - Lấy thông tin profile của user hiện tại

**Mô tả:** Lấy profile đầy đủ gồm thông tin cá nhân, avatar, và danh sách địa chỉ.

**Auth:** Yêu cầu Bearer token.

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Profile retrieved successfully",
  "result": {
    "userId": "user_123",
    "avatarUrl": "https://example.com/avatars/user_123.jpg",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0912345678",
    "dob": "1990-01-15",
    "address": [
      {
        "id": "addr_001",
        "street": "123 Main Street",
        "city": "Ho Chi Minh",
        "postalCode": "700000"
      }
    ]
  }
}
```

---

### 2. **PUT /profile** - Cập nhật profile của user hiện tại

**Mô tả:** Cập nhật thông tin cá nhân (tên, phone, ngày sinh).

**Auth:** Yêu cầu Bearer token.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe Updated",
  "phone": "0987654321",
  "dob": "1990-01-15"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Profile updated successfully",
  "result": {
    "userId": "user_123",
    "avatarUrl": "https://example.com/avatars/user_123.jpg",
    "firstName": "John",
    "lastName": "Doe Updated",
    "phone": "0987654321",
    "dob": "1990-01-15",
    "address": []
  }
}
```

---

## Admin Profile Endpoints

### 3. **GET /profile/admin** - Lấy thông tin profile của user bằng userId

**Mô tả:** Admin lấy profile chi tiết của một user cụ thể.

**Auth:** Yêu cầu Bearer token + quyền ADMIN.

**Query Parameters:**
- `userId` (required) - ID của user cần lấy thông tin

**Example:**
```
GET /profile/admin?userId=user_123
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Profile retrieved successfully!",
  "result": {
    "userId": "user_123",
    "avatarUrl": "https://example.com/avatars/user_123.jpg",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0912345678",
    "dob": "1990-01-15",
    "address": []
  }
}
```

---

### 4. **PUT /profile/admin/{userId}** - Cập nhật profile user (Admin)

**Mô tả:** Admin cập nhật profile của một user.

**Auth:** Yêu cầu Bearer token + quyền ADMIN.

**Path Parameters:**
- `userId` - ID của user cần cập nhật

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0912345678",
  "dob": "1990-01-15"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Profile update successfully!",
  "result": {
    "userId": "user_123",
    "avatarUrl": "https://example.com/avatars/user_123.jpg",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0912345678",
    "dob": "1990-01-15",
    "address": []
  }
}
```

---

### 5. **DELETE /profile/admin/{userId}** - Xóa profile user (Admin)

**Mô tả:** Admin xóa profile và dữ liệu liên quan của một user.

**Auth:** Yêu cầu Bearer token + quyền ADMIN.

**Path Parameters:**
- `userId` - ID của user cần xóa

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Profile deleted successfully",
  "result": null
}
```

---

### 6. **GET /profile/admin/getAll** - Lấy tất cả profile (Admin)

**Mô tả:** Admin lấy danh sách tất cả profile với pagination.

**Auth:** Yêu cầu Bearer token + quyền ADMIN.

**Query Parameters:**
- `page` (optional, default: 0) - Trang thứ mấy
- `size` (optional, default: 10) - Số phần tử trên một trang

**Example:**
```
GET /profile/admin/getAll?page=0&size=10
```

**Response (200 OK):**
```json
{
  "code": 200,
  "result": {
    "content": [
      {
        "userId": "user_123",
        "avatarUrl": "https://example.com/avatars/user_123.jpg",
        "firstName": "John",
        "lastName": "Doe",
        "phone": "0912345678",
        "dob": "1990-01-15",
        "address": []
      },
      {
        "userId": "user_456",
        "avatarUrl": "https://example.com/avatars/user_456.jpg",
        "firstName": "Jane",
        "lastName": "Smith",
        "phone": "0987654321",
        "dob": "1992-05-20",
        "address": []
      }
    ],
    "totalPages": 1,
    "totalElements": 2,
    "currentPage": 0
  }
}
```

---

## Address Endpoints

### 7. **POST /address** - Tạo địa chỉ mới

**Mô tả:** Thêm một địa chỉ mới cho user.

**Auth:** Yêu cầu Bearer token.

**Request Body:**
```json
{
  "street": "123 Main Street",
  "city": "Ho Chi Minh",
  "postalCode": "700000"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Create successful address!",
  "result": {
    "id": "addr_001",
    "street": "123 Main Street",
    "city": "Ho Chi Minh",
    "postalCode": "700000",
    "addressType": "home"
  }
}
```

---

### 8. **GET /address** - Lấy tất cả địa chỉ

**Mô tả:** Lấy danh sách tất cả địa chỉ của user đang đăng nhập.

**Auth:** Yêu cầu Bearer token.

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Lấy danh sách địa chỉ thành công",
  "result": [
    {
      "id": "addr_001",
      "street": "123 Main Street",
      "city": "Ho Chi Minh",
      "postalCode": "700000",
      "addressType": "home"
    },
    {
      "id": "addr_002",
      "street": "456 Work Avenue",
      "city": "Hanoi",
      "postalCode": "100000",
      "addressType": "work"
    }
  ]
}
```

---

### 9. **PUT /address** - Cập nhật địa chỉ

**Mô tả:** Cập nhật thông tin địa chỉ hiện có.

**Auth:** Yêu cầu Bearer token.

**Request Body:**
```json
{
  "id": "addr_001",
  "street": "789 Updated Street",
  "city": "Da Nang",
  "postalCode": "550000"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Cập nhật địa chỉ thành công",
  "result": {
    "id": "addr_001",
    "street": "789 Updated Street",
    "city": "Da Nang",
    "postalCode": "550000",
    "addressType": "home"
  }
}
```

---

### 10. **DELETE /address/{addressId}** - Xóa địa chỉ

**Mô tả:** Xóa một địa chỉ theo ID.

**Auth:** Yêu cầu Bearer token.

**Path Parameters:**
- `addressId` - ID của địa chỉ cần xóa

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Xoá địa chỉ thành công",
  "result": "Địa chỉ đã được xoá"
}
```

---

## Internal Endpoints (Service-to-Service)

### 11. **GET /profile/rating/getProfile** - Lấy profile cho rating service

**Mô tả:** Endpoint nội bộ được gọi bởi rating-service.

**Response (200 OK):**
```json
{
  "code": 200,
  "result": {
    "userId": "user_123",
    "firstName": "John",
    "lastName": "Doe",
    "avatarUrl": "https://example.com/avatars/user_123.jpg"
  }
}
```

---

### 12. **POST /profile/internal/avatar** - Cập nhật avatar (Internal)

**Mô tả:** Endpoint nội bộ để cập nhật URL avatar của user (được gọi bởi media-service).

**Query Parameters:**
- `avatarUrl` (required) - URL của avatar mới

**Example:**
```
POST /profile/internal/avatar?avatarUrl=https://example.com/avatars/new_avatar.jpg
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "creation avatar for user successfully!"
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

Từ thư mục `profile-service`:

```bash
mvn clean install
mvn spring-boot:run
```

## Data Models

### UserProfile Entity
```
- userId: String (Primary Key)
- avatarUrl: String
- firstName: String
- lastName: String
- phone: String
- dob: Date
- address: List<UserAddress>
```

### UserAddress Entity
```
- id: String (Primary Key)
- userId: String (Foreign Key)
- street: String
- city: String
- postalCode: String
- addressType: String (home, work, other)
```

## Notes

- Các endpoint user yêu cầu đăng nhập với JWT Bearer token
- Admin endpoints yêu cầu quyền ADMIN
- Tất cả response đều tuân theo cấu trúc ApiResponse chung
- Avatar URL được quản lý bởi media-service
