# Project Setup and Run Instructions

This guide explains how to set up and run the microservices project.

## Prerequisites
- **Java 21**
- **Maven**
- **Docker Desktop** (for infrastructure)

## 1. Start Infrastructure
The project uses Docker Compose for databases and messaging.
Run the following command in the project root:

```bash
docker-compose up -d
```
This starts:
- MySQL (Port 3306)
- PostgreSQL (Port 5432)
- MongoDB (Port 27017)
- Neo4j (Port 7474, 7687)
- Redpanda/Kafka (Port 9092)
- Elasticsearch & Kibana

## 2. Build the Project
Build all services and the common library:

```bash
mvn clean install
```
*Note: This is critical ensuring `common-lib` is available for other services.*

## 3. Run Microservices
It is recommended to run services in the following order:

1.  **Discovery Service** (Eureka)
    - Path: `discovery-service`
    - Command: `mvn spring-boot:run`
2.  **API Gateway**
    - Path: `api-gateway`
    - Command: `mvn spring-boot:run`
3.  **Core Services** (Start these as needed or all together)
    - `user-service`
    - `profile-service`
    - `product-service`
    - `order-service`
    - `inventory-service`
    - `notification-service`
    - `media-service`
    - `search-service`
    - `payment-service`
    - `rating-service`
    - `promotion-service`
    - `cart-service`

## Access Points
- **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761) (Default port)
- **API Gateway**: [http://localhost:8080](http://localhost:8080) (Default port, check `application.yml` if different)
