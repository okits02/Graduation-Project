# Stock (Stock-In) API — Inventory Service

## Mục đích
File này mô tả chi tiết các API liên quan đến chức năng "Stock-In" (nhập kho) trong `Inventory Service`. Nội dung bao gồm: các endpoint, payload mẫu, mã lỗi, ví dụ curl và lưu ý tích hợp.

Base URLs
- Khi gọi qua API Gateway: `http://localhost:8888/api/v1/inventory-service`
- Khi gọi trực tiếp vào service: `http://localhost:8074/inventory-service`

Prefix cho các endpoint stock: `/stock-in`

---

## Danh sách Endpoint

1. `POST /stock-in/create` — Tạo phiếu nhập hàng
2. `GET /stock-in/get-by-referenceCode/{referenceCode}` — Lấy phiếu nhập theo mã tham chiếu
3. `GET /stock-in/get-history` — Lấy danh sách lịch sử nhập hàng (có lọc theo thời gian, phân trang)
4. `GET /stock-in/get-by-id/{stockInId}` — Lấy phiếu nhập theo ID
5. `DELETE /stock-in/delete/{referenceCode}` — Xóa phiếu nhập theo mã tham chiếu

---

## 1) Tạo phiếu nhập hàng

Endpoint
```
POST /stock-in/create
```

Request (JSON)
```json
{
  "supplierName": "Nhà cung cấp ABC",
  "referenceCode": "STOCK-IN-001",
  "note": "Nhập hàng đợt 1",
  "items": [
    {
      "sku": "PROD-001",
      "quantity": 100,
      "unitCost": 50000
    },
    {
      "sku": "PROD-002",
      "quantity": 50,
      "unitCost": 75000
    }
  ]
}
```

Các trường chính:
- `supplierName` (string) — tên nhà cung cấp
- `referenceCode` (string, unique) — mã tham chiếu phiếu nhập
- `note` (string, optional)
- `items` (array) — danh sách `sku`, `quantity`, `unitCost`

Response (thành công)
```json
{
  "code": 200,
  "message": "create stock receipt successfully!",
  "result": {
    "id": "<uuid>",
    "supplierName": "Nhà cung cấp ABC",
    "referenceCode": "STOCK-IN-001",
    "totalAmount": 7500000,
    "note": "Nhập hàng đợt 1",
    "createdAt": "2024-01-08T10:30:00",
    "items": [ ... ]
  }
}
```

Hành động phụ trợ:
- Tạo bản ghi `StockIn` và `StockInItem` trong DB
- Cập nhật `Inventory` : với mỗi `sku` trong items, tăng số lượng tương ứng và tạo `InventoryTransaction` (INBOUND)

Mã lỗi phổ biến:
- `400` — request body không hợp lệ
- `409` — `referenceCode` đã tồn tại
- `500` — lỗi server khi lưu DB hoặc cập nhật inventory

Ví dụ curl (qua API Gateway)
```bash
curl -X POST "http://localhost:8888/api/v1/inventory-service/stock-in/create" \
  -H "Content-Type: application/json" \
  -d '@stock_payload.json'
```

---

## 2) Lấy phiếu nhập theo mã tham chiếu

Endpoint
```
GET /stock-in/get-by-referenceCode/{referenceCode}
```

Ví dụ
```
GET /stock-in/get-by-referenceCode/STOCK-IN-001
```

Response (thành công)
- Trả về `ApiResponse` với `result` là đối tượng `StockInResponse` chứa thông tin header và danh sách items.

Mã lỗi:
- `404` — không tìm thấy phiếu với `referenceCode` cung cấp

---

## 3) Lấy lịch sử nhập hàng (filter + paging)

Endpoint
```
GET /stock-in/get-history?page=1&size=10&start={ISO_DATETIME}&end={ISO_DATETIME}
```

Tham số:
- `page` (int, mặc định 1) — trang (1-based)
- `size` (int, mặc định 10) — số bản ghi/trang
- `start` (ISO datetime) — thời điểm bắt đầu (bắt buộc)
- `end` (ISO datetime) — thời điểm kết thúc (bắt buộc)

Ví dụ
```
GET /stock-in/get-history?page=1&size=20&start=2024-01-01T00:00:00&end=2024-12-31T23:59:59
```

Response
- `ApiResponse` với `result` là `PageResponse<StockInResponse>` gồm `content`, `pageNo`, `pageSize`, `totalElements`, `totalPages`.

Lưu ý
- `start` và `end` phải ở định dạng ISO 8601 (ví dụ: `2024-01-01T00:00:00`)

---

## 4) Lấy phiếu nhập theo ID

Endpoint
```
GET /stock-in/get-by-id/{stockInId}
```

- Trả về chi tiết phiếu theo UUID
- `404` nếu không tìm thấy

---

## 5) Xóa phiếu nhập theo mã tham chiếu

Endpoint
```
DELETE /stock-in/delete/{referenceCode}
```

Hành vi:
- Xóa phiếu nhập và items liên quan (cascade)
- Tùy implementation: không khuyến khích xóa nếu đã có transaction liên quan hoặc đã ảnh hưởng tới đơn hàng; có thể chỉ cho phép nếu chưa xác nhận

Response (thành công)
```json
{
  "code": 200,
  "message": "delete stock receipt successfully!"
}
```

---

## Mối liên hệ với Inventory và Transaction
- Khi tạo `StockIn`, service sẽ: tăng `Inventory.quantity` cho mỗi `sku` theo `quantity` nhập và tạo `InventoryTransaction` kiểu `INBOUND`.
- `StockIn.referenceCode` nên được dùng làm `referenceId` trong `InventoryTransaction` để dễ truy vết.

---

## Mã lỗi & Xử lý chung
| HTTP | Ý nghĩa |
|------|---------|
| 200  | Thành công |
| 400  | Request không hợp lệ |
| 404  | Không tìm thấy tài nguyên |
| 409  | Conflict (ví dụ reference trùng) |
| 500  | Lỗi server |

Ví dụ lỗi khi `referenceCode` bị trùng:
```json
{
  "code": 409,
  "message": "Stock-In reference code already exists",
  "error": "REFERENCE_CONFLICT"
}
```

---

## Ví dụ kịch bản thực tế (end-to-end)

1. Admin tạo phiếu nhập 100 đơn vị cho `PROD-001`:
```bash
curl -X POST "http://localhost:8888/api/v1/inventory-service/stock-in/create" \
  -H "Content-Type: application/json" \
  -d '{
    "supplierName": "Supplier A",
    "referenceCode": "SI-2026-001",
    "items": [{"sku":"PROD-001","quantity":100,"unitCost":50000}]
  }'
```

2. Kiểm tra inventory sau khi nhập:
```bash
curl "http://localhost:8888/api/v1/inventory-service/inventory/get?sku=PROD-001"
```

3. Lấy lịch sử nhập hàng trong tháng:
```bash
curl "http://localhost:8888/api/v1/inventory-service/stock-in/get-history?start=2026-01-01T00:00:00&end=2026-01-31T23:59:59"
```

---

## Notes & Best Practices
- Luôn đảm bảo `referenceCode` là duy nhất để tránh duplicate
- Nếu gửi qua API Gateway, dùng prefix `/api/v1/inventory-service/stock-in/...`
- Xác thực: API có thể yêu cầu JWT Bearer token tùy cấu hình security (đảm bảo header `Authorization: Bearer <token>` nếu cần)
- Transactional: thao tác tạo `StockIn` + cập nhật `Inventory` nên ở trong transaction để tránh trạng thái không đồng nhất

---

## Vị trí file liên quan trong repo
- Controller: `src/main/java/com/okits02/inventory_service/controller/StockInController.java`
- Service: `src/main/java/com/okits02/inventory_service/service/StockInService.java` và `Impl`
- Model: `src/main/java/com/okits02/inventory_service/model/StockIn.java`, `StockInItem.java`
- Mapper: `src/main/java/com/okits02/inventory_service/mapper/StockInMapper.java` (nếu có)

---

Nếu bạn muốn, tôi có thể:
- thêm Postman collection hoặc OpenAPI snippet chỉ cho `stock` endpoints
- hoặc cập nhật `inventory-service/README.md` để nhúng phần `Stock` này

``` 
File: STOCK_README.md
```