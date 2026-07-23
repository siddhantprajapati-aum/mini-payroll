# Mini Payroll System — Backend

Spring Boot REST API for the Mini Payroll & Attendance System (SaaS MVP).

## Tech stack

- Java 17, Spring Boot 3.3
- Spring Data JPA, Validation, Web
- PostgreSQL
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito

## Architecture

Layered design:

```
controller → service → repository → entity (PostgreSQL)
                ↓
              dto / enums / exception / utils
```

| Layer | Responsibility |
|---|---|
| `controller` | HTTP endpoints, request validation |
| `service` | Business rules (leave workflow, payroll calc, attendance upsert) |
| `repository` | Data access |
| `entity` | JPA models + relationships |
| `dto` | Request/response contracts |
| `exception` | Typed errors + global handler |

## Database schema & relationships

```
employees (1) ──< attendance
employees (1) ──< leaves
```

- **Employee:** `id`, `name`, `role` (WFH / OFFICE / ONSITE), `salaryType` (MONTHLY / DAILY), `salaryAmount`
- **Attendance:** `id`, `employee_id` (FK), `date`, `status` (PRESENT / ABSENT), `remarks`  
  Unique constraint: `(employee_id, date)`
- **Leave:** `id`, `employee_id` (FK), `startDate`, `endDate`, `reason`, `status` (PENDING / APPROVED / REJECTED)

## Payroll logic

- **Monthly:** `(salaryAmount / 30) × presentDays`
- **Daily:** `salaryAmount × presentDays`
- Response includes present/absent/unmarked days and a formula string.

## Extra feature

**Bulk attendance:** `POST /api/v1/attendances/{employeeId}/bulk`

## Setup

1. Create PostgreSQL database `mini_payroll`
2. Update credentials in `src/main/resources/application.yaml` if needed
3. Run:

```bash
./mvnw spring-boot:run
```

- API base: `http://localhost:8080`
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Main API endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/employees` | Create employee |
| GET | `/api/v1/employees` | List employees (paginated) |
| POST | `/api/v1/attendances/{employeeId}` | Mark / upsert attendance |
| POST | `/api/v1/attendances/{employeeId}/bulk` | Bulk mark attendance |
| GET | `/api/v1/attendances/{employeeId}` | Attendance history (paginated) |
| POST | `/api/v1/leaves/{employeeId}` | Apply leave |
| PUT | `/api/v1/leaves/{leaveId}/status` | Approve / reject leave |
| GET | `/api/v1/leaves` | All leaves (paginated) |
| GET | `/api/v1/leaves/employee/{employeeId}` | Leaves by employee |
| GET | `/api/v1/payroll/{employeeId}?year=&month=` | Payroll breakdown |

Swagger is the live source of truth for request/response schemas.

## Tests

```bash
./mvnw test
```

## Assumptions

- Present days are counted only from attendance records marked `PRESENT`
- Days with no attendance row are treated as unmarked (not paid)
- Leave does not auto-modify attendance or payroll
- Payroll month divisor is fixed at **30** (per assignment formula)
- CORS is open for `/api/**` to support the Angular frontend during development
