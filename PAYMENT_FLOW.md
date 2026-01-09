# Flow Thanh Toán Sản Phẩm - Chi Tiết API

Tài liệu này mô tả toàn bộ flow thanh toán từ client (web/mobile) đến hệ thống backend.

---

## Tổng Quan Flow

```
1. Thêm sản phẩm vào giỏ hàng (Cart Service)
   ↓
2. Xem giỏ hàng và kiểm tra thông tin (Cart Service)
   ↓
3. Tạo đơn hàng (Order Service)
   ↓
4. Kiểm tra và áp dụng voucher (Order Service)
   ↓
5. Lấy số tiền cần thanh toán (Order Service)
   ↓
6. Khởi tạo thanh toán (Payment Service - VNPay)
   ↓
7. Chuyển hướng đến cổng thanh toán VNPay
   ↓
8. Người dùng nhập thông tin thanh toán
   ↓
9. VNPay xác nhận thanh toán
   ↓
10. Callback/IPN từ VNPay → Backend xử lý
    ↓
11. Cập nhật trạng thái đơn hàng
    ↓
12. Hiển thị kết quả thanh toán cho người dùng
```

---

## Chi Tiết Các Bước

### **Bước 1: Thêm Sản Phẩm Vào Giỏ Hàng**

**Endpoint:** `POST /cart/create-cart`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/cart/create-cart`

**Auth:** Yêu cầu Bearer token (JWT)

**Request Body:**
```json
{
  "sku": "PROD-001",
  "quantity": 2
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "result": {
    "id": "cart_123",
    "userId": "user_123",
    "totalPrice": 500000,
    "items": [
      {
        "id": "cart_item_001",
        "sku": "PROD-001",
        "productName": "Laptop Dell XPS",
        "price": 250000,
        "quantity": 2,
        "image": "https://..."
      }
    ]
  }
}
```

**Validation:**
- `sku`: Phải tồn tại trong hệ thống
- `quantity`: Phải > 0 và ≤ tồn kho
- User phải đã đăng nhập

**Lỗi Có Thể Xảy Ra:**
- 401 - Chưa đăng nhập
- 400 - SKU không tồn tại
- 400 - Số lượng vượt quá tồn kho

---

### **Bước 2: Cập Nhật Giỏ Hàng**

**Endpoint:** `PUT /cart/update-cart`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/cart/update-cart`

**Auth:** Yêu cầu Bearer token

**Request Body:**
```json
{
  "sku": "PROD-001",
  "quantity": 3
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "result": {
    "id": "cart_123",
    "userId": "user_123",
    "totalPrice": 750000,
    "items": [
      {
        "id": "cart_item_001",
        "sku": "PROD-001",
        "productName": "Laptop Dell XPS",
        "price": 250000,
        "quantity": 3,
        "image": "https://..."
      }
    ]
  }
}
```

---

### **Bước 3: Lấy Giỏ Hàng Hiện Tại**

**Endpoint:** `GET /cart/get-my-cart`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/cart/get-my-cart`

**Auth:** Yêu cầu Bearer token

**Response (200 OK):**
```json
{
  "code": 200,
  "result": {
    "id": "cart_123",
    "userId": "user_123",
    "totalPrice": 750000,
    "items": [
      {
        "id": "cart_item_001",
        "sku": "PROD-001",
        "productName": "Laptop Dell XPS",
        "price": 250000,
        "quantity": 3,
        "image": "https://..."
      }
    ]
  }
}
```

---

### **Bước 4: Xóa Sản Phẩm Khỏi Giỏ Hàng**

**Endpoint:** `DELETE /cart/delete-items`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/cart/delete-items`

**Auth:** Yêu cầu Bearer token

**Request Body:**
```json
{
  "cartItemIds": ["cart_item_001", "cart_item_002"]
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "result": {
    "id": "cart_123",
    "userId": "user_123",
    "totalPrice": 0,
    "items": []
  }
}
```

---

### **Bước 5: Tạo Đơn Hàng**

⚠️ **Bước quan trọng nhất - tại đây đơn hàng được khởi tạo**

**Endpoint:** `POST /order/create`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/order/create`

**Auth:** Yêu cầu Bearer token

**Request Body:**
```json
{
  "orderDate": "2026-01-09T10:30:00",
  "orderDesc": "Mua laptop gaming",
  "orderFee": 50000,
  "addressId": "addr_001",
  "voucher": "TECH2024",
  "items": [
    {
      "sku": "PROD-001",
      "quantity": 3,
      "price": 250000
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "save order successfully!",
  "result": {
    "id": "order_123",
    "userId": "user_123",
    "status": "PENDING",
    "orderDate": "2026-01-09T10:30:00",
    "orderDesc": "Mua laptop gaming",
    "totalAmount": 800000,
    "orderFee": 50000,
    "discountAmount": 50000,
    "finalAmount": 800000,
    "addressId": "addr_001",
    "voucher": "TECH2024",
    "items": [
      {
        "sku": "PROD-001",
        "quantity": 3,
        "price": 250000,
        "subTotal": 750000
      }
    ],
    "createdAt": "2026-01-09T10:30:00"
  }
}
```

**Validation:**
- `addressId`: Phải là địa chỉ của người dùng
- `items`: Không được rỗng
- `voucher`: Nếu có, phải còn hiệu lực
- Tồn kho phải đủ cho tất cả items

**Lỗi Có Thể Xảy Ra:**
- 401 - Chưa đăng nhập
- 400 - Địa chỉ không tồn tại
- 400 - Voucher không hợp lệ
- 400 - Tồn kho không đủ

**Quy Trình Bên Trong:**
1. Kiểm tra tồn kho tất cả sản phẩm (Inventory Service)
2. Kiểm tra và áp dụng voucher (Promotion Service)
3. Tính toán tổng tiền = (giá sản phẩm) + phí - chiết khấu voucher
4. Lưu đơn hàng với trạng thái PENDING
5. Trừ tồn kho sản phẩm

---

### **Bước 6: Lấy Số Tiền Cần Thanh Toán**

**Endpoint:** `GET /order/internal/getAmount`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/order/internal/getAmount?orderId=order_123`

**Auth:** Yêu cầu Bearer token

**Query Parameters:**
- `orderId` - ID của đơn hàng

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Get amount successfully!",
  "result": {
    "orderId": "order_123",
    "amount": 800000,
    "currency": "VND",
    "description": "Thanh toán đơn hàng #order_123"
  }
}
```

---

### **Bước 7: Khởi Tạo Thanh Toán (VNPay)**

⚠️ **Bước này sẽ trả về URL thanh toán VNPay - client phải chuyển hướng đến URL này**

**Endpoint:** `POST /pay/create`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/pay/create`

**Auth:** Yêu cầu Bearer token

**Request Body:**
```json
{
  "method": "VNPAY",
  "orderId": "order_123"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "create payment successfully!",
  "result": {
    "paymentUrl": "https://sandbox.vnpayment.vn/paygate?vnp_Command=pay&vnp_Version=2.1.0&vnp_TmnCode=...",
    "orderId": "order_123",
    "amount": 800000,
    "paymentMethod": "VNPAY",
    "status": "INITIATED",
    "createdAt": "2026-01-09T10:35:00"
  }
}
```

**Client Action:**
```javascript
// Lấy paymentUrl từ response
const paymentUrl = response.result.paymentUrl;

// Chuyển hướng người dùng đến cổng thanh toán
window.location.href = paymentUrl;
```

---

### **Bước 8: Xử Lý Kết Quả Thanh Toán**

**Scenario 1: Thanh toán thành công**

VNPay sẽ redirect người dùng về callback URL (được cấu hình bên Payment Service):

**Endpoint:** `GET /pay/vnpay-return`

**URL (ví dụ):** `http://localhost:8080/api/v1/pay/vnpay-return?vnp_Amount=800000&vnp_BankCode=NCB&vnp_OrderInfo=order_123&vnp_ResponseCode=00&vnp_TxnRef=order_123&...`

**Query Parameters Từ VNPay:**
- `vnp_Amount` - Số tiền (đơn vị: 100 VND)
- `vnp_BankCode` - Ngân hàng (NCB, VCB, MB, etc.)
- `vnp_OrderInfo` - Thông tin đơn hàng
- `vnp_ResponseCode` - Mã phản hồi (00 = thành công)
- `vnp_TxnRef` - Mã giao dịch = Order ID
- `vnp_SecureHash` - Hash xác thực

**Response (200 OK):**
```html
Thanh toán thành công! Mã đơn hàng: order_123
```

**Scenario 2: Thanh toán thất bại**

```html
Thanh toán thất bại. Mã lỗi: 01
```

**VNPay Response Codes:**
| Code | Meaning |
|------|---------|
| 00 | Giao dịch thành công |
| 01 | Giao dịch bị từ chối |
| 02 | Merchant đóng kết nối |
| 04 | Từ chối giao dịch bởi ngân hàng |
| 05 | Giao dịch bị lỗi |
| 06 | Khách hàng hủy giao dịch |
| 07 | Giao dịch bị nghi ngờ |
| 09 | Giao dịch bị hủy bỏ |

---

### **Bước 9: Cập Nhật Trạng Thái Đơn Hàng**

⚠️ **Sau khi nhận callback từ VNPay, backend sẽ tự động cập nhật trạng thái**

**Endpoint:** `POST /order/status`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/order/status?orderId=order_123&status=CONFIRMED`

**Auth:** Yêu cầu Bearer token (Internal service call)

**Query Parameters:**
- `orderId` - ID của đơn hàng
- `status` - Trạng thái mới (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)

**Response (200 OK):**
```json
{
  "code": 200,
  "result": {
    "id": "order_123",
    "status": "CONFIRMED",
    "updatedAt": "2026-01-09T10:40:00"
  }
}
```

**Order Statuses:**
| Status | Meaning |
|--------|---------|
| PENDING | Đơn hàng vừa tạo, chờ thanh toán |
| CONFIRMED | Thanh toán thành công, chời xác nhận |
| SHIPPED | Đơn hàng đã gửi đi |
| DELIVERED | Đơn hàng đã giao |
| CANCELLED | Đơn hàng bị hủy |
| REFUNDED | Đơn hàng hoàn tiền |

---

### **Bước 10: Lấy Lịch Sử Thanh Toán**

**Endpoint:** `GET /pay/history`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/pay/history?page=1&size=10`

**Auth:** Yêu cầu Bearer token

**Query Parameters:**
- `page` - Trang (mặc định: 1)
- `size` - Số bản ghi/trang (mặc định: 10)

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "get history successfully!",
  "result": {
    "content": [
      {
        "id": "payment_001",
        "orderId": "order_123",
        "amount": 800000,
        "method": "VNPAY",
        "status": "SUCCESS",
        "bankCode": "NCB",
        "transactionRef": "order_123",
        "paidAt": "2026-01-09T10:40:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

### **Bước 11: Lấy Thông Tin Đơn Hàng**

**Endpoint:** `GET /order?orderId=order_123`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/order?orderId=order_123`

**Auth:** Yêu cầu Bearer token

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "get order by id successfully",
  "result": {
    "id": "order_123",
    "userId": "user_123",
    "status": "CONFIRMED",
    "orderDate": "2026-01-09T10:30:00",
    "totalAmount": 800000,
    "orderFee": 50000,
    "discountAmount": 50000,
    "finalAmount": 800000,
    "addressId": "addr_001",
    "voucher": "TECH2024",
    "items": [
      {
        "sku": "PROD-001",
        "productName": "Laptop Dell XPS",
        "price": 250000,
        "quantity": 3,
        "subTotal": 750000
      }
    ],
    "payment": {
      "method": "VNPAY",
      "status": "SUCCESS",
      "paidAt": "2026-01-09T10:40:00"
    },
    "createdAt": "2026-01-09T10:30:00",
    "updatedAt": "2026-01-09T10:40:00"
  }
}
```

---

### **Bước 12: Lấy Danh Sách Đơn Hàng của Người Dùng**

**Endpoint:** `GET /order/get-my-order`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/order/get-my-order?userId=user_123&status=CONFIRMED&page=1&size=10`

**Auth:** Yêu cầu Bearer token

**Query Parameters:**
- `userId` - ID của user
- `status` - Trạng thái (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- `page` - Trang (mặc định: 1)
- `size` - Số bản ghi/trang (mặc định: 10)

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "get order for user successfully!",
  "result": {
    "content": [
      {
        "id": "order_123",
        "status": "CONFIRMED",
        "totalAmount": 800000,
        "createdAt": "2026-01-09T10:30:00",
        "items": [
          {
            "sku": "PROD-001",
            "productName": "Laptop Dell XPS",
            "quantity": 3
          }
        ]
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "currentPage": 0
  }
}
```

---

### **Bước 13: Hủy Đơn Hàng**

**Endpoint:** `PUT /order/cancel`

**URL (qua API Gateway):** `http://localhost:8080/api/v1/order/cancel?orderId=order_123`

**Auth:** Yêu cầu Bearer token

**Query Parameters:**
- `orderId` - ID của đơn hàng cần hủy

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "cancel order successfully",
  "result": {}
}
```

**Conditions:**
- Chỉ có thể hủy đơn hàng có trạng thái PENDING hoặc CONFIRMED
- Nếu đã thanh toán, phải hoàn tiền trước khi hủy

**Quy Trình Hủy:**
1. Kiểm tra trạng thái đơn hàng
2. Cập nhật trạng thái thành CANCELLED
3. Hoàn lại tồn kho
4. Hoàn lại tiền voucher nếu có

---

## Quy Trình Validation Voucher

Khi tạo đơn hàng với voucher, hệ thống sẽ:

1. **Check Voucher:** Kiểm tra voucher có tồn tại và còn hiệu lực
2. **Kiểm tra điều kiện:**
   - Còn lượt sử dụng
   - Chưa quá hạn
   - Giá trị đơn hàng >= giá trị tối thiểu (nếu có)
3. **Tính chiết khấu:**
   - Nếu % discount: `discount = totalAmount * discountPercent / 100`
   - Nếu fixed discount: `discount = discountAmount` (tối đa không quá totalAmount)
4. **Cập nhật lượt sử dụng:** Giảm số lần sử dụng còn lại

---

## Quy Trình Xử Lý Stock (Tồn Kho)

### Khi Tạo Đơn Hàng:
1. **Check Stock:** Kiểm tra tồn kho từ Inventory Service
2. **Reserve Stock:** Giữ chỗ (trừ tồn kho)
3. **Nếu Thất Bại:** Rollback và báo lỗi

### Khi Thanh Toán Thành Công:
1. **Confirm Stock:** Xác nhận trừ tồn kho
2. **Không cần action thêm:** Stock đã bị trừ ở step 1

### Khi Hủy Đơn Hàng:
1. **Release Stock:** Hoàn lại tồn kho
2. **Update Quantity:** Tăng lại quantity trong Inventory Service

---

## Error Handling

### Các Lỗi Thường Gặp:

| Error | Code | Giải Pháp |
|-------|------|----------|
| Chưa đăng nhập | 401 | Login lại |
| Không có quyền | 403 | Kiểm tra quyền |
| Sản phẩm không tồn tại | 400 | Chọn sản phẩm khác |
| Tồn kho không đủ | 400 | Giảm số lượng hoặc chọn sản phẩm khác |
| Địa chỉ không hợp lệ | 400 | Chọn/thêm địa chỉ mới |
| Voucher không hợp lệ | 400 | Xoá voucher hoặc chọn voucher khác |
| Lỗi kết nối VNPay | 500 | Thử lại thanh toán |
| Timeout | 504 | Kiểm tra lại đơn hàng, có thể thanh toán thành công |

---

## Client-Side Implementation Example (JavaScript)

```javascript
// 1. Thêm sản phẩm vào giỏ
async function addToCart(sku, quantity) {
  const response = await fetch('http://localhost:8080/api/v1/cart/create-cart', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ sku, quantity })
  });
  return response.json();
}

// 2. Lấy thông tin giỏ hàng
async function getCart() {
  const response = await fetch('http://localhost:8080/api/v1/cart/get-my-cart', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return response.json();
}

// 3. Tạo đơn hàng
async function createOrder(orderData) {
  const response = await fetch('http://localhost:8080/api/v1/order/create', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(orderData)
  });
  return response.json();
}

// 4. Khởi tạo thanh toán
async function initiatePayment(orderId) {
  const response = await fetch('http://localhost:8080/api/v1/pay/create', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      method: 'VNPAY',
      orderId: orderId
    })
  });
  
  const data = await response.json();
  
  // 5. Redirect đến VNPay
  window.location.href = data.result.paymentUrl;
}

// 6. Lấy lịch sử thanh toán
async function getPaymentHistory(page = 1, size = 10) {
  const response = await fetch(
    `http://localhost:8080/api/v1/pay/history?page=${page}&size=${size}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  return response.json();
}

// 7. Hủy đơn hàng
async function cancelOrder(orderId) {
  const response = await fetch(
    `http://localhost:8080/api/v1/order/cancel?orderId=${orderId}`,
    {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  return response.json();
}

// Ví dụ sử dụng:
async function checkout() {
  try {
    // 1. Thêm sản phẩm vào giỏ
    await addToCart('PROD-001', 2);
    
    // 2. Tạo đơn hàng
    const orderResult = await createOrder({
      orderDate: new Date().toISOString(),
      orderDesc: 'Mua sắm online',
      orderFee: 50000,
      addressId: 'addr_001',
      voucher: 'TECH2024',
      items: [
        {
          sku: 'PROD-001',
          quantity: 2,
          price: 250000
        }
      ]
    });
    
    if (orderResult.code !== 200) {
      alert('Tạo đơn hàng thất bại: ' + orderResult.message);
      return;
    }
    
    const orderId = orderResult.result.id;
    
    // 3. Khởi tạo thanh toán
    await initiatePayment(orderId);
    
  } catch (error) {
    console.error('Checkout error:', error);
    alert('Lỗi xảy ra: ' + error.message);
  }
}
```

---

## Flow Diagram (ASCII)

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (Web/Mobile)                      │
└─────────────────────────────────────────────────────────────────┘
         │                                                 ▲
         │ 1. Login (User Service)                       │
         │─────────────────────────────────────────→      │
         │                                                 │
         │ 2. Add to Cart (Cart Service)                  │
         │─────────────────────────────────────────→      │
         │                                                 │
         │ 3. Create Order (Order Service)                │
         │─────────────────────────────────────────→      │
         │    ├─ Check Stock (Inventory Service)          │
         │    ├─ Apply Voucher (Promotion Service)        │
         │    └─ Create Order                             │
         │                                                 │
         │ 4. Initiate Payment (Payment Service)          │
         │─────────────────────────────────────────→      │
         │    └─ Get Payment URL                          │
         │                                                 │
         │ 5. Redirect to VNPay Gateway                   │
         │─────────────────────────────────────────→      │ Payment URL
         │                                                 │
         ├────────────────────────────────────────────────┤
         │ User enters payment info on VNPay              │
         │                                                 │
         │ VNPay processes payment                        │
         │                                                 │
         │ VNPay redirects to callback URL                │
         ├────────────────────────────────────────────────┤
         │                                                 │
         │ 6. Payment Result (Payment Service)            │
         │ ← ─────────────────────────────────────────── │
         │                                                 │
         │ 7. Update Order Status (Order Service)         │
         │ ← ─────────────────────────────────────────── │
         │                                                 │
         │ 8. Show Result Page                            │
         │ ← ─────────────────────────────────────────── │
         │                                                 │
         │ Success/Failure Message                        │
         └─────────────────────────────────────────────────┘
```

---

## Testing Payment Flow

### Test Cases:

#### Test Case 1: Successful Payment
1. Add product to cart ✓
2. Create order with valid data ✓
3. Initiate payment ✓
4. Complete payment on VNPay ✓
5. Verify order status = CONFIRMED ✓

#### Test Case 2: Invalid Voucher
1. Create order with expired voucher
2. Expect error response
3. Verify order not created ✓

#### Test Case 3: Insufficient Stock
1. Add product quantity > available stock
2. Expect error: "Insufficient stock"
3. Verify cart updated ✓

#### Test Case 4: Missing Address
1. Create order without addressId
2. Expect error: "Invalid address"
3. Verify order not created ✓

#### Test Case 5: Cancel Order
1. Create order (PENDING status)
2. Cancel order
3. Verify status = CANCELLED ✓
4. Verify stock restored ✓

---

## Important Notes

1. **Timeout Handling:** Nếu không nhận được callback từ VNPay trong 5 phút, client nên kiểm tra lại trạng thái đơn hàng
2. **Duplicate Prevention:** Hệ thống kiểm tra duplicates dựa trên `vnp_TxnRef` để tránh xử lý 2 lần
3. **Security:** Luôn xác thực signature từ VNPay trước khi cập nhật trạng thái
4. **Logging:** Log tất cả transactions cho mục đích audit
5. **Retry Logic:** Nếu lỗi kết nối, hãy retry tối đa 3 lần

---

## References

- VNPay Docs: https://sandbox.vnpayment.vn/
- Order Service: `/order-service/README.md`
- Payment Service: `/payment-service/README.md`
- Cart Service: `/cart-service/README.md`
