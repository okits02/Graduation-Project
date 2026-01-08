# Inventory Service API Documentation

## 📋 Mục Lục
1. [Giới thiệu](#giới-thiệu)
2. [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
3. [Cài đặt và chạy](#cài-đặt-và-chạy)
4. [Cấu trúc dự án](#cấu-trúc-dự-án)
5. [API Endpoints](#api-endpoints)
6. [Ví dụ sử dụng](#ví-dụ-sử-dụng)
7. [Kafka Events](#kafka-events)
8. [Lỗi và Xử lý](#lỗi-và-xử-lý)

---

## 🎯 Giới thiệu

**Inventory Service** là một microservice trong hệ thống e-commerce quản lý tồn kho sản phẩm. Service này:

- ✅ Quản lý số lượng sản phẩm theo SKU (Stock Keeping Unit)
- ✅ Tự động đồng bộ hóa sản phẩm từ Product Service qua Kafka
- ✅ Kiểm tra tính khả dụng sản phẩm cho Order Service
- ✅ Ghi lại lịch sử giao dịch tồn kho (tăng/giảm)
- ✅ Quản lý phiếu nhập hàng từ nhà cung cấp

**Port**: 8074  
**Context Path**: `/inventory-service`  
**Base URL**: `http://localhost:8888/api/v1/inventory-service`

---

## 💻 Yêu cầu hệ thống

- **Java**: 21 hoặc cao hơn
- **Maven**: 3.8.9 trở lên
- **PostgreSQL**: 12+
- **Kafka**: 3.x+
- **Eureka Server**: (cho service discovery)

### Database Configuration
```yaml
Database: inventory_db
Username: admin
Password: admin123
URL: jdbc:postgresql://localhost:5432/inventory_db
```

### Kafka Configuration
```yaml
Bootstrap Servers: localhost:9094
Topics: product-event
```

---

## 🚀 Cài đặt và Chạy

### 1. Thiết lập Database
```bash
# Tạo database PostgreSQL
createdb -U admin inventory_db

# Hoặc qua psql
psql -U admin
CREATE DATABASE inventory_db;
```

### 2. Build Project
```bash
# Từ thư mục inventory-service
mvn clean install

# Hoặc từ thư mục gốc
mvn clean install -pl inventory-service
```

### 3. Chạy Service
```bash
# Option 1: Maven
mvn spring-boot:run

# Option 2: Chạy JAR trực tiếp
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar

# Option 3: Docker (nếu có Dockerfile)
docker build -t inventory-service .
docker run -p 8074:8074 inventory-service
```

### 4. Kiểm tra Service
```bash
# Health check
curl http://localhost:8074/inventory-service/actuator/health

# Swagger UI
http://localhost:8074/inventory-service/swagger-ui.html

# API Docs
http://localhost:8074/inventory-service/v3/api-docs
```

---

## 📁 Cấu trúc Dự Án

```
inventory-service/
├── src/
│   ├── main/
│   │   ├── java/com/okits02/inventory_service/
│   │   │   ├── InventoryServiceApplication.java       # Entry point
│   │   │   ├── configurations/                         # Spring configs
│   │   │   │   ├── OpenApiConfig.java                 # Swagger/OpenAPI
│   │   │   │   ├── SecurityConfig.java                # JWT security
│   │   │   │   ├── KafkaConsumerConfig.java           # Kafka setup
│   │   │   │   └── CustomJwtDecoder.java              # JWT decoder
│   │   │   ├── controller/                            # REST controllers
│   │   │   │   ├── InventoryController.java
│   │   │   │   └── StockInController.java
│   │   │   ├── service/                               # Business logic
│   │   │   │   ├── InventoryService.java
│   │   │   │   ├── StockInService.java
│   │   │   │   └── Impl/
│   │   │   ├── model/                                 # JPA entities
│   │   │   │   ├── Inventory.java
│   │   │   │   ├── InventoryTransaction.java
│   │   │   │   ├── StockIn.java
│   │   │   │   └── StockInItem.java
│   │   │   ├── repository/                            # Data access
│   │   │   ├── dto/                                   # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── mapper/                                # MapStruct mappers
│   │   │   ├── consumer/                              # Kafka consumers
│   │   │   │   └── ProductConsumer.java
│   │   │   ├── kafka/                                 # Kafka models
│   │   │   ├── exceptions/                            # Custom exceptions
│   │   │   ├── enums/                                 # Enumerations
│   │   │   └── validator/                             # Input validators
│   │   └── resources/
│   │       └── application.yml                        # Configuration
│   └── test/                                          # Unit tests
├── pom.xml                                            # Maven dependencies
└── README.md                                          # This file
```

---

## 📡 API Endpoints

### 1. **Lấy thông tin tồn kho theo SKU**

```http
GET /inventory/get?sku=PROD-001
```

**Query Parameters:**
| Tham số | Kiểu | Yêu cầu | Mô tả |
|---------|------|--------|-------|
| sku | string | ✅ | Mã sản phẩm |

**Response:**
```json
{
  "code": 200,
  "message": "Get product information at inventory success!",
  "result": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "sku": "PROD-001",
    "quantity": 150,
    "updatedAt": "2024-01-08T10:30:00"
  }
}
```

**Status Codes:**
- `200`: OK
- `404`: Sản phẩm không tìm thấy

---

### 2. **Kiểm tra sản phẩm còn hàng**

```http
POST /inventory/check-inStock
Content-Type: application/json

{
  "items": [
    {
      "sku": "PROD-001",
      "quantity": 5
    },
    {
      "sku": "PROD-002",
      "quantity": 3
    }
  ]
}
```

**Request Body:**
```json
{
  "items": [
    {
      "sku": "string",
      "quantity": "integer"
    }
  ]
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Check success",
  "result": true  // true = có đủ hàng, false = không đủ
}
```

**Status Codes:**
- `200`: OK
- `400`: Request không hợp lệ
- `409`: Không đủ hàng

---

### 3. **Giảm tồn kho (Khi đặt hàng)**

```http
POST /inventory/decrease-stock
Content-Type: application/json

{
  "sku": "PROD-001",
  "quantity": 5,
  "orderId": "ORDER-12345"
}
```

**Request Body:**
| Trường | Kiểu | Yêu cầu | Mô tả |
|-------|------|--------|-------|
| sku | string | ✅ | Mã sản phẩm |
| quantity | integer | ✅ | Số lượng giảm |
| orderId | string | ✅ | ID đơn hàng (để tracking) |

**Response:**
```json
{
  "code": 200,
  "message": "Decrease success!"
}
```

**Status Codes:**
- `200`: OK
- `400`: Request không hợp lệ
- `404`: SKU không tìm thấy
- `409`: Không đủ hàng để giảm

---

### 4. **Tăng tồn kho (Khi hoàn hàng)**

```http
POST /inventory/increase-stock
Content-Type: application/json

{
  "sku": "PROD-001",
  "quantity": 5,
  "orderId": "ORDER-12345"
}
```

**Request Body:** Giống như decrease-stock

**Response:**
```json
{
  "code": 200,
  "message": "increase success!"
}
```

---

### 5. **Lấy danh sách toàn bộ tồn kho (Có phân trang)**

```http
GET /inventory/get-all?page=1&size=10
```

**Query Parameters:**
| Tham số | Kiểu | Mặc định | Mô tả |
|---------|------|---------|-------|
| page | integer | 1 | Trang (bắt đầu từ 1) |
| size | integer | 10 | Số bản ghi/trang |

**Response:**
```json
{
  "code": 200,
  "message": "get all inventory successfully!",
  "result": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "sku": "PROD-001",
        "quantity": 150,
        "updatedAt": "2024-01-08T10:30:00"
      }
    ],
    "pageNo": 1,
    "pageSize": 10,
    "totalElements": 245,
    "totalPages": 25
  }
}
```

---

### 6. **Lấy lịch sử giao dịch tồn kho**

```http
GET /inventory/transactions?sku=PROD-001&page=1&size=10
```

**Query Parameters:**
| Tham số | Kiểu | Yêu cầu | Mô tả |
|---------|------|--------|-------|
| sku | string | ✅ | Mã sản phẩm |
| page | integer | - | Trang (mặc định: 1) |
| size | integer | - | Số bản ghi/trang (mặc định: 10) |

**Response:**
```json
{
  "content": [
    {
      "id": "tx-001",
      "sku": "PROD-001",
      "quantity": -5,
      "referenceId": "ORDER-12345",
      "transactionType": "OUTBOUND",
      "createdAt": "2024-01-08T10:30:00"
    },
    {
      "id": "tx-002",
      "sku": "PROD-001",
      "quantity": 50,
      "referenceId": "STOCK-IN-001",
      "transactionType": "INBOUND",
      "createdAt": "2024-01-07T14:20:00"
    }
  ],
  "pageNo": 1,
  "pageSize": 10,
  "totalElements": 24,
  "totalPages": 3
}
```

---

### 7. **Tạo phiếu nhập hàng**

```http
POST /stock-in/create
Content-Type: application/json

{
  "supplierName": "Nhà cung cấp ABC",
  "referenceCode": "STOCK-IN-001",
  "note": "Nhập từ kho phía Bắc",
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

**Request Body:**
```json
{
  "supplierName": "string",
  "referenceCode": "string (unique)",
  "note": "string (optional)",
  "items": [
    {
      "sku": "string",
      "quantity": "integer",
      "unitCost": "number"
    }
  ]
}
```

**Response:**
```json
{
  "code": 200,
  "message": "create stock receipt successfully!",
  "result": {
    "id": "stock-in-uuid",
    "supplierName": "Nhà cung cấp ABC",
    "referenceCode": "STOCK-IN-001",
    "totalAmount": 7500000,
    "note": "Nhập từ kho phía Bắc",
    "createdAt": "2024-01-08T10:30:00",
    "items": [
      {
        "sku": "PROD-001",
        "quantity": 100,
        "unitCost": 50000
      }
    ]
  }
}
```

**Status Codes:**
- `200`: OK
- `400`: Request không hợp lệ
- `409`: Reference code đã tồn tại

---

### 8. **Lấy thông tin phiếu nhập theo mã tham chiếu**

```http
GET /stock-in/get-by-referenceCode/STOCK-IN-001
```

**Response:**
```json
{
  "code": 200,
  "message": "get stock receipt by reference code successfully!",
  "result": {
    "id": "stock-in-uuid",
    "supplierName": "Nhà cung cấp ABC",
    "referenceCode": "STOCK-IN-001",
    "totalAmount": 7500000,
    "note": "Nhập từ kho phía Bắc",
    "createdAt": "2024-01-08T10:30:00",
    "items": [...]
  }
}
```

---

### 9. **Lấy lịch sử nhập hàng (Có lọc theo ngày)**

```http
GET /stock-in/get-history?page=1&size=10&start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
```

**Query Parameters:**
| Tham số | Kiểu | Yêu cầu | Mô tả | Format |
|---------|------|--------|-------|--------|
| page | integer | - | Trang (mặc định: 1) | số |
| size | integer | - | Số bản ghi/trang (mặc định: 10) | số |
| start | datetime | ✅ | Thời gian bắt đầu | ISO 8601 |
| end | datetime | ✅ | Thời gian kết thúc | ISO 8601 |

**Response:**
```json
{
  "code": 200,
  "message": "get all history for stock receipt successfully!",
  "result": {
    "content": [...],
    "pageNo": 1,
    "pageSize": 10,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 10. **Lấy thông tin phiếu nhập theo ID**

```http
GET /stock-in/get-by-id/{stockInId}
```

**Path Parameters:**
| Tham số | Kiểu | Mô tả |
|---------|------|-------|
| stockInId | string | UUID của phiếu nhập |

**Response:** Giống như endpoint 8

---

### 11. **Xóa phiếu nhập hàng**

```http
DELETE /stock-in/delete/{referenceCode}
```

**Path Parameters:**
| Tham số | Kiểu | Mô tả |
|---------|------|-------|
| referenceCode | string | Mã tham chiếu của phiếu |

**Response:**
```json
{
  "code": 200,
  "message": "delete stock receipt successfully!"
}
```

**Status Codes:**
- `200`: OK
- `404`: Phiếu không tìm thấy

---

## 💡 Ví dụ Sử dụng

### Scenario: Khách hàng đặt hàng

```bash
# 1. Kiểm tra xem sản phẩm còn hàng không
curl -X POST http://localhost:8888/api/v1/inventory-service/inventory/check-inStock \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"sku": "PROD-001", "quantity": 5}
    ]
  }'

# Response: {"code": 200, "result": true}

# 2. Nếu còn hàng, giảm tồn kho
curl -X POST http://localhost:8888/api/v1/inventory-service/inventory/decrease-stock \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": 5,
    "orderId": "ORD-2024-001"
  }'

# 3. Nếu khách hoàn hàng sau đó, tăng lại tồn kho
curl -X POST http://localhost:8888/api/v1/inventory-service/inventory/increase-stock \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "PROD-001",
    "quantity": 5,
    "orderId": "ORD-2024-001"
  }'
```

### Scenario: Nhập hàng từ nhà cung cấp

```bash
# Tạo phiếu nhập hàng
curl -X POST http://localhost:8888/api/v1/inventory-service/stock-in/create \
  -H "Content-Type: application/json" \
  -d '{
    "supplierName": "ABC Supplier",
    "referenceCode": "SI-2024-001",
    "note": "Nhập hàng đợt 1",
    "items": [
      {
        "sku": "PROD-001",
        "quantity": 100,
        "unitCost": 50000
      }
    ]
  }'

# Xem lịch sử nhập hàng
curl "http://localhost:8888/api/v1/inventory-service/stock-in/get-history?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59"
```

### Scenario: Xem lịch sử giao dịch

```bash
# Xem tất cả giao dịch của sản phẩm PROD-001
curl "http://localhost:8888/api/v1/inventory-service/inventory/transactions?sku=PROD-001&page=1&size=20"
```

---

## 🔄 Kafka Events

### Event nhận từ Product Service

Service này lắng nghe các event từ topic `product-event`:

**Event Type: CREATE**
```json
{
  "eventType": "CREATED",
  "productId": "prod-001",
  "productVariants": [
    {
      "variantId": "var-001",
      "sku": "PROD-001-RED-M",
      "initialQuantity": 0
    }
  ]
}
```

**Hành động**: Tạo record Inventory mới với số lượng 0

**Event Type: DELETE**
```json
{
  "eventType": "DELETED",
  "productVariants": [
    {
      "sku": "PROD-001-RED-M"
    }
  ]
}
```

**Hành động**: Xóa record Inventory liên quan

---

## 🚨 Lỗi và Xử lý

### Mã Lỗi

| Mã | Thông báo | Nguyên nhân |
|----|-----------|-----------|
| 200 | Success | Thành công |
| 400 | Bad Request | Dữ liệu không hợp lệ |
| 404 | Not Found | Sản phẩm/phiếu không tìm thấy |
| 409 | Conflict | Không đủ hàng hoặc reference code trùng |
| 500 | Internal Error | Lỗi server |

### Ví dụ Error Response

```json
{
  "code": 409,
  "message": "Insufficient inventory for SKU: PROD-001",
  "error": "INVENTORY_INSUFFICIENT"
}
```

---

## 🔐 Bảo Mật

Service sử dụng **JWT Bearer Token** cho authentication:

```bash
curl -X GET "http://localhost:8888/api/v1/inventory-service/inventory/get?sku=PROD-001" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Lấy JWT Token:**
- Đăng nhập qua User Service
- Token được cấp với các roles (ROLE_USER, ROLE_ADMIN, ...)

---

## 📊 Database Schema

### Bảng: inventory
```sql
CREATE TABLE inventory (
  id UUID PRIMARY KEY,
  sku VARCHAR(255) UNIQUE NOT NULL,
  quantity INTEGER,
  updated_at TIMESTAMP
);
```

### Bảng: inventory_transaction
```sql
CREATE TABLE inventory_transaction (
  id UUID PRIMARY KEY,
  inventory_id UUID REFERENCES inventory(id),
  quantity_change INTEGER,
  transaction_type VARCHAR(50),  -- INBOUND, OUTBOUND
  reference_id VARCHAR(255),     -- OrderId hoặc StockInId
  created_at TIMESTAMP
);
```

### Bảng: stock_in
```sql
CREATE TABLE stock_in (
  id UUID PRIMARY KEY,
  supplier_name VARCHAR(255),
  reference_code VARCHAR(255) UNIQUE,
  total_amount DECIMAL(19,2),
  note TEXT,
  created_at TIMESTAMP
);
```

### Bảng: stock_in_item
```sql
CREATE TABLE stock_in_item (
  id UUID PRIMARY KEY,
  stock_in_id UUID REFERENCES stock_in(id),
  sku VARCHAR(255),
  quantity INTEGER,
  unit_cost DECIMAL(19,2)
);
```

---

## 🛠️ Troubleshooting

### Service không kết nối được database
```
Error: Connection to localhost:5432 refused
Giải pháp: 
1. Kiểm tra PostgreSQL đang chạy: psql --version
2. Kiểm tra connection string trong application.yml
3. Tạo database: createdb -U admin inventory_db
```

### Kafka consumer không nhận event
```
Error: No partitions assigned to partition group...
Giải pháp:
1. Kiểm tra Kafka đang chạy
2. Kiểm tra topic product-event tồn tại
3. Xem logs: docker logs kafka-container
```

### JWT Token hết hạn
```
Error: 401 Unauthorized
Giải pháp: Lấy token mới từ User Service
```

---

## 📚 Tài Liệu Thêm

- **Swagger UI**: http://localhost:8074/inventory-service/swagger-ui.html
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Kafka Docs**: https://kafka.apache.org/documentation/
- **PostgreSQL Docs**: https://www.postgresql.org/docs/

---

## 👥 Support

Nếu gặp vấn đề:
1. Kiểm tra logs: `tail -f logs/inventory-service.log`
2. Xem Swagger UI để test API trực tiếp
3. Liên hệ team backend

---

**Last Updated**: 2024-01-08  
**Version**: 1.0.0
