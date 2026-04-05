# Finance Dashboard Backend

A RESTful backend for a multi-role finance dashboard system built with **Java 17**, **Spring Boot 3.2**, **Spring Security (JWT)**, and an **H2 in-memory database**. Designed as part of a backend engineering assessment for Zorvyn FinTech Pvt. Ltd.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Default Users](#default-users)
- [API Reference](#api-reference)
- [Role-Based Access Control](#role-based-access-control)
- [Data Model](#data-model)
- [Assumptions & Design Decisions](#assumptions--design-decisions)
- [Optional Enhancements Implemented](#optional-enhancements-implemented)

---

## Overview

This system serves as the backend for a finance dashboard where different users interact with financial records based on their assigned role. It supports:

- JWT-based stateless authentication
- Role-based access control (VIEWER / ANALYST / ADMIN)
- Full CRUD operations on financial records with soft delete
- Dashboard analytics including totals, category breakdowns, and monthly trends
- Input validation with field-level error messages
- Pagination, filtering, and sorting on record listings
- Seeded data on startup for immediate testing

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 In-Memory |
| Validation | Jakarta Bean Validation (JSR-380) |
| Build Tool | Maven |
| Utilities | Lombok |

---

## Project Structure

```
src/main/java/com/finance/
├── config/
│   └── SecurityConfig.java          # JWT filter chain, role guards, BCrypt
├── controller/
│   ├── AuthController.java          # POST /api/auth/login
│   ├── UserController.java          # CRUD on users (ADMIN only)
│   ├── FinancialRecordController.java # CRUD on records
│   └── DashboardController.java     # Aggregated analytics
├── dto/
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── UserRequest.java / UserResponse.java / UpdateUserRequest.java
│   ├── FinancialRecordRequest.java / FinancialRecordResponse.java
│   ├── DashboardSummaryResponse.java
│   ├── ApiResponse.java             # Generic wrapper for all responses
│   └── PagedResponse.java           # Pagination wrapper
├── entity/
│   ├── User.java
│   └── FinancialRecord.java
├── enums/
│   ├── Role.java                    # VIEWER, ANALYST, ADMIN
│   ├── TransactionType.java         # INCOME, EXPENSE
│   └── Category.java                # SALARY, RENT, FOOD, etc.
├── exception/
│   ├── GlobalExceptionHandler.java  # Centralized error handling
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── AccessDeniedException.java
├── repository/
│   ├── UserRepository.java
│   └── FinancialRecordRepository.java  # Custom JPQL filters + aggregations
├── security/
│   ├── JwtUtil.java                 # Token generation and validation
│   ├── JwtAuthenticationFilter.java # Per-request token extraction
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── UserService.java
    ├── FinancialRecordService.java
    └── DashboardService.java

src/main/resources/
├── application.properties
└── data.sql                         # Seed data (3 users + 10 records)
```

---

## Architecture

The application follows a standard layered architecture:

```
Client Request
     │
     ▼
JWT Auth Filter  ──►  Validates Bearer token on every request
     │
     ▼
Spring Security  ──►  @PreAuthorize role check per endpoint
     │
     ▼
Controller       ──►  Receives validated request, delegates to service
     │
     ▼
Service          ──►  Business logic, orchestration
     │
     ▼
Repository       ──►  Spring Data JPA, custom JPQL queries
     │
     ▼
H2 Database      ──►  In-memory, auto-seeded on startup
```

**Key design choices:**
- All responses are wrapped in a uniform `ApiResponse<T>` envelope with `success`, `message`, `data`, and `timestamp` fields.
- Financial records use **soft delete** — records are never physically removed, only flagged with `deleted=true`. All queries exclude soft-deleted records automatically.
- The JWT is stateless — no session management. Every request is independently validated.
- `@PreAuthorize` annotations at the controller method level enforce role checks after authentication passes.

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Run the application

```bash
# Clone the repository
git clone <your-repo-url>
cd finance-backend

# Build and run
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

### Access the H2 Console

Navigate to `http://localhost:8080/h2-console` and connect with:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:financedb` |
| Username | `sa` |
| Password | *(leave blank)* |

---

## Default Users

These are seeded automatically on every startup via `data.sql`:

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `password123` | ADMIN | Full access — manage users, records, view dashboard |
| `analyst` | `password123` | ANALYST | View and filter records, access all dashboard analytics |
| `viewer` | `password123` | VIEWER | View dashboard summary only |

---

## API Reference

### Authentication

All protected endpoints require the header:
```
Authorization: Bearer <token>
```

Obtain a token by calling the login endpoint first.

---

### POST /api/auth/login
**Access:** Public

**Request:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "username": "admin",
    "email": "admin@finance.com",
    "role": "ADMIN"
  }
}
```

---

### User Management

All user endpoints require **ADMIN** role.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user (email, password, role, status) |
| PATCH | `/api/users/{id}/toggle-status` | Activate or deactivate a user |

**Create user request body:**
```json
{
  "username": "newuser",
  "email": "newuser@finance.com",
  "password": "securepass",
  "role": "ANALYST"
}
```

Valid roles: `VIEWER`, `ANALYST`, `ADMIN`

---

### Financial Records

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/records` | ANALYST, ADMIN | List records with filters and pagination |
| GET | `/api/records/{id}` | ANALYST, ADMIN | Get single record |
| POST | `/api/records` | ADMIN | Create a record |
| PUT | `/api/records/{id}` | ADMIN | Update a record |
| DELETE | `/api/records/{id}` | ADMIN | Soft delete a record |

**Create/Update record request body:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "FREELANCE",
  "recordDate": "2024-03-15",
  "notes": "Website project payment"
}
```

**Valid types:** `INCOME`, `EXPENSE`

**Valid categories:** `SALARY`, `FREELANCE`, `INVESTMENT`, `RENTAL_INCOME`, `OTHER_INCOME`, `RENT`, `UTILITIES`, `FOOD`, `TRANSPORT`, `ENTERTAINMENT`, `HEALTHCARE`, `EDUCATION`, `SHOPPING`, `OTHER_EXPENSE`

**Query parameters for GET /api/records:**

| Parameter | Type | Description |
|---|---|---|
| `type` | `INCOME` \| `EXPENSE` | Filter by transaction type |
| `category` | Category enum | Filter by category |
| `startDate` | `yyyy-MM-dd` | Filter from date (inclusive) |
| `endDate` | `yyyy-MM-dd` | Filter to date (inclusive) |
| `page` | integer (default: 0) | Page number |
| `size` | integer (default: 10) | Records per page |
| `sortBy` | string (default: `recordDate`) | Sort field |
| `sortDir` | `asc` \| `desc` (default: `desc`) | Sort direction |

**Example:**
```
GET /api/records?type=INCOME&category=SALARY&startDate=2024-01-01&page=0&size=5
```

---

### Dashboard Analytics

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary` | VIEWER, ANALYST, ADMIN | Full summary — totals, categories, recent activity, monthly trends |
| GET | `/api/dashboard/income-by-category` | ANALYST, ADMIN | Income totals grouped by category |
| GET | `/api/dashboard/expense-by-category` | ANALYST, ADMIN | Expense totals grouped by category |

**Summary response includes:**
- `totalIncome` — sum of all non-deleted INCOME records
- `totalExpenses` — sum of all non-deleted EXPENSE records
- `netBalance` — totalIncome minus totalExpenses
- `categoryWiseTotals` — map of category name to total amount
- `recentActivity` — last 10 records sorted by date
- `monthlyTrends` — array of `{ year, month, income, expense, net }` objects

---

## Role-Based Access Control

| Endpoint Group | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| `POST /api/auth/login` | ✅ | ✅ | ✅ |
| `GET /api/dashboard/summary` | ✅ | ✅ | ✅ |
| `GET /api/dashboard/*-by-category` | ❌ | ✅ | ✅ |
| `GET /api/records` | ❌ | ✅ | ✅ |
| `GET /api/records/{id}` | ❌ | ✅ | ✅ |
| `POST /api/records` | ❌ | ❌ | ✅ |
| `PUT /api/records/{id}` | ❌ | ❌ | ✅ |
| `DELETE /api/records/{id}` | ❌ | ❌ | ✅ |
| All `/api/users/**` | ❌ | ❌ | ✅ |

Unauthorized access returns `403 Forbidden`. Missing or invalid token returns `403`.

---

## Data Model

### User

| Field | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `username` | String | Unique, 3–50 characters |
| `email` | String | Unique, valid email format |
| `password` | String | BCrypt hashed |
| `role` | Enum | `VIEWER`, `ANALYST`, `ADMIN` |
| `active` | Boolean | False = login disabled |
| `createdAt` | LocalDateTime | Set on creation, immutable |

### FinancialRecord

| Field | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `amount` | BigDecimal | Precision 15, scale 2. Must be > 0 |
| `type` | Enum | `INCOME` or `EXPENSE` |
| `category` | Enum | 14 valid categories |
| `recordDate` | LocalDate | Cannot be in the future |
| `notes` | String | Optional, max 500 characters |
| `createdBy` | User | FK to user who created the record |
| `deleted` | Boolean | Soft delete flag. False by default |
| `createdAt` | LocalDateTime | Auto-set on creation |
| `updatedAt` | LocalDateTime | Auto-updated on save |

---

## Assumptions & Design Decisions

1. **Registration is admin-only.** In a real finance system, accounts should not be self-registered. Only an ADMIN can create new users and assign their roles.

2. **Soft delete over hard delete.** Financial records are never permanently removed. This preserves audit history, which is critical in finance applications. The `deleted` flag hides records from all queries but keeps them in the database.

3. **JWT expiry is set to 24 hours** (`jwt.expiration=86400000` ms). For production this would be shorter with a refresh token mechanism.

4. **H2 is used for simplicity.** The assignment explicitly allows in-memory storage. All data resets on application restart. To switch to a persistent database (e.g. PostgreSQL), change the datasource properties and driver dependency in `pom.xml` — no service or repository code changes are needed.

5. **VIEWER cannot view raw records.** Viewers can see aggregated dashboard data (totals, trends) but cannot browse individual financial records. This is intentional — viewers get insights without access to sensitive transaction details.

6. **Pagination defaults to page 0, size 10.** All record listings are paginated by default to prevent large data loads.

7. **Category and TransactionType are enums.** This enforces data integrity at the API layer and prevents free-text inconsistencies in financial categorization.

---

## Optional Enhancements Implemented

- ✅ **JWT Authentication** — stateless token-based auth with 24-hour expiry
- ✅ **Pagination** — all record listings support `page`, `size`, `sortBy`, `sortDir`
- ✅ **Filtering** — records filterable by type, category, and date range simultaneously
- ✅ **Soft delete** — financial records are logically deleted, not physically removed
- ✅ **Field-level validation errors** — 400 responses include a map of `fieldName → errorMessage`
- ✅ **Seed data** — 3 users and 10 financial records loaded automatically on startup
- ✅ **Uniform API response envelope** — every response follows `{ success, message, data, timestamp }`