# 🚌 Bus Booking Microservices System (Saga Pattern)

## 📌 Overview

This project demonstrates a **Microservices-based Bus Booking System** using:

* **Spring Boot 3 + Java 21**
* **Spring Cloud (Eureka, OpenFeign)**
* **Saga Pattern (Orchestration)**
* **Oracle DB (Payment Service)**
* **MySQL / JPA (Booking Service)**
* **MongoDB (Notification Service)**

The system ensures **data consistency across distributed services** using the **Saga Orchestration Pattern**.

---

## 🏗️ Architecture

```
Client → Orchestrator Service
                ↓
        ┌───────────────┐
        │ Booking Service│
        └───────────────┘
                ↓
        ┌───────────────┐
        │ Payment Service│
        └───────────────┘
                ↓
        ┌───────────────┐
        │ Notification   │
        └───────────────┘
```

---

## 🔥 Microservices

### 1️⃣ Booking Service

* Manages booking data
* Stores:

  * Customer info
  * Booking status
  * Payment ID

---

### 2️⃣ Payment Service

* Handles payment processing
* Uses **Oracle DB**
* Generates `paymentId` using sequence

---

### 3️⃣ Notification Service

* Sends:

  * Email
  * SMS
* Uses **MongoDB**

---

### 4️⃣ Orchestrator Service

* Implements **Saga Pattern**
* Controls workflow:

  * Create Booking
  * Process Payment
  * Update Booking
  * Send Notification
  * Handle Failures (Compensation)

---

## ⚙️ Technologies Used

* Java 21
* Spring Boot 3
* Spring Cloud 2025
* OpenFeign
* Spring Data JPA
* Oracle DB
* MongoDB
* Eureka Server
* Lombok
* Maven

---

## 🔁 Saga Flow

### ✅ Success Flow

1. Create Booking → `PENDING`
2. Process Payment → `SUCCESS`
3. Update Booking → `BOOKED`
4. Send Notification → Email & SMS

---

### ❌ Failure Flow

1. Create Booking → `PENDING`
2. Process Payment → `FAILED`
3. Update Booking → Save `paymentId`
4. Send Notification → Failure Alert
5. Cancel Booking → `CANCELLED`

---

## 📦 API Endpoints

### 🔹 Orchestrator

```
POST /orchestrator/start
```

#### Sample Request

```json
{
  "customerName": "Chandan",
  "email": "chandan@gmail.com",
  "amount": 1500
}
```

---

### 🔹 Booking Service

```
POST /booking/bookingdatasave
POST /booking/updatepayment
POST /booking/cancelbusbooking
```

---

### 🔹 Payment Service

```
POST /payment/process
POST /payment/refund
```

---

### 🔹 Notification Service

```
POST /notify/sendemail
POST /notify/sendsms
```

---

## 🧪 Sample Outputs

### ✅ Success

```json
{
  "status": "SUCCESS",
  "message": "Booking completed successfully"
}
```

---

### ❌ Failure

```json
{
  "status": "FAILED",
  "message": "Payment failed, booking cancelled"
}
```

---

## 🛠️ Setup Instructions

### 1️⃣ Start Services in Order

1. Eureka Server
2. Config Server (optional)
3. Booking Service
4. Payment Service
5. Notification Service
6. Orchestrator Service

---

### 2️⃣ Run Databases

* Oracle → Payment Service
* MongoDB → Notification Service
* MySQL → Booking Service

---

### 3️⃣ Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

---

## ⚠️ Key Learnings

* Distributed transactions using Saga Pattern
* Service-to-service communication using Feign
* Handling failures with compensation logic
* DTO vs Entity separation
* Microservices architecture design

---

## 🚀 Future Enhancements

* Kafka-based Saga (Event-driven)
* Retry mechanism for failed payments
* Circuit Breaker (Resilience4j)
* API Gateway integration
* Admin Dashboard (Spring Boot Admin)

---

## 👨‍💻 Author

**Chandan Singh Patel**
