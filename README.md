# Mini Payroll & Attendance System

Production-oriented SaaS MVP for managing employees, attendance, leave, and payroll.

**Stack:** Java 17 · Spring Boot 3.3 · PostgreSQL · Angular 19

| Layer | Location |
|-------|----------|
| Backend API | [`backend/`](./backend) |
| Frontend UI | [`frontend/`](./frontend) |

> **Live URL:** _Add after Render deploy (e.g. `https://mini-payroll-app.onrender.com`)_  
> **Swagger (local):** http://localhost:8080/swagger-ui/index.html  
> **Swagger (live):** `https://<your-service>.onrender.com/swagger-ui/index.html`

---

## Features

- **Employees** — create and list (role, salary type, salary amount)
- **Attendance** — mark present/absent, history by employee, **bulk mark** (extra feature)
- **Leave** — apply, approve, reject
- **Payroll** — monthly/daily formulas with salary breakdown

---

## Architecture

```
┌─────────────────┐     HTTP /api/v1/*      ┌──────────────────────┐
│  Angular (SPA)  │ ──────────────────────► │  Spring Boot API     │
│  frontend/      │ ◄────────────────────── │  backend/            │
└─────────────────┘                         └──────────┬───────────┘
                                                       │ JPA
                                                       ▼
                                              ┌─────────────────┐
                                              │   PostgreSQL    │
                                              └─────────────────┘
```

### Backend layers

```
controller → service → repository → entity (PostgreSQL)
               ↓
         dto / enums / exception / utils / constants
```

| Layer | Responsibility |
|-------|----------------|
| Controller | REST endpoints, request binding |
| Service | Business rules (attendance upsert, leave workflow, payroll) |
| Repository | Data access |
| Entity | Persistence model + relationships |
| DTO | Request/response contracts + validation |
| Exception | Typed errors + global handler |

### Frontend structure

```
frontend/src/app/
  layout/shell/          # App navigation
  features/              # Employees, Attendance, Leave, Payroll pages
  core/services/         # API clients
  core/models/           # TypeScript models
  core/utils/            # Date rules, API error parsing
```

Local Angular calls use `/api/v1` and are proxied to `http://localhost:8080` via `frontend/proxy.conf.json`.

---

## Database schema & relationships

```
employees (1) ──────── < attendance
     │
     └─────────────── < leaves
```

### Entity relationship diagram

```mermaid
erDiagram
    EMPLOYEES ||--o{ ATTENDANCE : has
    EMPLOYEES ||--o{ LEAVES : has

    EMPLOYEES {
        bigint id PK
        string name
        string role
        string salaryType
        double salaryAmount
        datetime createdAt
        datetime updatedAt
    }

    ATTENDANCE {
        bigint id PK
        bigint employee_id FK
        date date
        string status
        string remarks
        datetime createdAt
    }

    LEAVES {
        bigint id PK
        bigint employee_id FK
        date startDate
        date endDate
        string reason
        string status
        datetime appliedAt
        datetime reviewedAt
    }
```

### Tables

**`employees`**
| Column | Type | Notes |
|--------|------|--------|
| id | BIGINT PK | Auto-generated |
| name | VARCHAR | Required |
| role | ENUM | `WFH`, `OFFICE`, `ONSITE` |
| salary_type | ENUM | `MONTHLY`, `DAILY` |
| salary_amount | DOUBLE | Required |
| created_at / updated_at | TIMESTAMP | Managed by entity hooks |

**`attendance`**
| Column | Type | Notes |
|--------|------|--------|
| id | BIGINT PK | Auto-generated |
| employee_id | BIGINT FK | → `employees.id` |
| date | DATE | Required |
| status | ENUM | `PRESENT`, `ABSENT` |
| remarks | VARCHAR | Optional |
| created_at | TIMESTAMP | |

Unique constraint: `(employee_id, date)` — one record per employee per day (upsert on re-mark).

**`leaves`**
| Column | Type | Notes |
|--------|------|--------|
| id | BIGINT PK | Auto-generated |
| employee_id | BIGINT FK | → `employees.id` |
| start_date / end_date | DATE | Required |
| reason | VARCHAR | Required |
| status | ENUM | `PENDING`, `APPROVED`, `REJECTED` |
| applied_at / reviewed_at | TIMESTAMP | |

---

## Payroll logic

### Formulas (assignment)

| Salary type | Formula |
|-------------|---------|
| Monthly | `(monthlySalary / 30) × presentDays` |
| Daily | `dailyWage × presentDays` |

The `/30` divisor is intentional and matches the assignment (not calendar or working-day count).

### Breakdown fields returned by API

- `workingDays` — expected days for the period  
  - **Monthly:** weekdays only (Mon–Fri)  
  - **Daily:** all calendar days (weekends allowed)
- `presentDays` / `absentDays` / `unmarkedDays`
- `calculatedSalary`
- `formula` — human-readable expression used for the amount

`unmarkedDays = workingDays − presentDays − absentDays`

For monthly employees, weekend attendance rows (if any) are ignored in payroll totals so numbers stay consistent with weekday-only working days.

---

## Extra feature: bulk attendance

`POST /api/v1/attendances/{employeeId}/bulk`

Accepts a list of attendance records and upserts each day. The Angular UI supports marking a date range in one action. For monthly employees, weekends in the range are skipped; for daily wage employees, weekends are included.

---

## API endpoints

Base URL (local): `http://localhost:8080`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/employees` | Create employee |
| `GET` | `/api/v1/employees?page=&size=` | List employees (paginated) |
| `POST` | `/api/v1/attendances/{employeeId}` | Mark / upsert attendance |
| `POST` | `/api/v1/attendances/{employeeId}/bulk` | Bulk mark attendance |
| `GET` | `/api/v1/attendances/{employeeId}?page=&size=` | Attendance history |
| `POST` | `/api/v1/leaves/{employeeId}` | Apply leave |
| `PUT` | `/api/v1/leaves/{leaveId}/status?status=` | Approve / reject (`APPROVED` \| `REJECTED`) |
| `GET` | `/api/v1/leaves?page=&size=` | List all leaves |
| `GET` | `/api/v1/leaves/employee/{employeeId}` | Leaves by employee |
| `GET` | `/api/v1/payroll/{employeeId}?year=&month=` | Payroll breakdown |

Interactive docs: [Swagger UI](http://localhost:8080/swagger-ui/index.html)

### Example requests

**Create employee**
```json
POST /api/v1/employees
{
  "name": "Priya Sharma",
  "role": "OFFICE",
  "salaryType": "MONTHLY",
  "salaryAmount": 60000
}
```

**Mark attendance**
```json
POST /api/v1/attendances/1
{
  "date": "2026-07-22",
  "status": "PRESENT",
  "remarks": "On time"
}
```

**Bulk attendance**
```json
POST /api/v1/attendances/1/bulk
{
  "records": [
    { "date": "2026-07-20", "status": "PRESENT" },
    { "date": "2026-07-21", "status": "ABSENT" }
  ]
}
```

**Apply leave**
```json
POST /api/v1/leaves/1
{
  "startDate": "2026-07-28",
  "endDate": "2026-07-29",
  "reason": "Family function"
}
```

**Payroll**
```http
GET /api/v1/payroll/1?year=2026&month=7
```

---

## Local setup

### Prerequisites

- Java 17+
- Maven (or use included `backend/mvnw`)
- Node.js 18+ / npm
- PostgreSQL 14+

### 1. Database

```sql
CREATE DATABASE mini_payroll;
```

Default local config (`backend/src/main/resources/application.yaml`) uses localhost Postgres.
Override anytime with env vars:

| Variable | Example |
|----------|---------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/mini_payroll` |
| `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` |
| `DATABASE_URL` | `postgresql://user:pass@host/db` (Render/Neon style; auto-converted) |

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

- API: http://localhost:8080  
- Swagger: http://localhost:8080/swagger-ui/index.html

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

- App: http://localhost:4200  
- Dev proxy forwards `/api` → `http://localhost:8080`

### 4. Tests (backend)

```bash
cd backend
./mvnw test
```

---

## Frontend pages

| Route | Page | Capabilities |
|-------|------|--------------|
| `/employees` | Employees | Create + list |
| `/attendance` | Attendance | Single mark, bulk mark, history |
| `/leave` | Leave | Apply, approve/reject |
| `/payroll` | Payroll | Generate monthly breakdown |

---

## Architecture decisions

1. **Layered Spring Boot API** — clear separation for maintainability and testing.
2. **PostgreSQL + JPA** — relational model with FK integrity; unique attendance per employee/day.
3. **Fixed `/30` payroll divisor** — follows assignment formula exactly; stored in `AppConstants`.
4. **Working days for unmarked counts** — monthly staff use weekdays so unmarked days are meaningful; daily staff use calendar days.
5. **Bulk attendance as extra feature** — practical for MVP operators; chosen over RBAC/overtime for delivery within 48 hours.
6. **Attendance upsert** — re-marking the same date updates status instead of failing on unique constraint.
7. **Open CORS on `/api/**` for local SPA** — simplifies Angular integration during development.
8. **Lean Angular UI** — four focused pages, shared styles, no heavy UI framework (fits 48-hour SaaS MVP scope).

---

## Assumptions (48-hour SaaS MVP)

Documented intentionally for scope and discussion:

1. **Payroll is attendance-driven.** Calculated from `PRESENT` days only. Approved leave does **not** automatically create attendance or change payroll. If leave should reduce pay, mark those days `ABSENT` in attendance.
2. **Unmarked ≠ absent.** Days with no attendance row are unpaid (`unmarkedDays`); they are not auto-filled as absent.
3. **Monthly divisor is 30** even when a month has 28–31 calendar days or ~22–23 weekdays (assignment requirement).
4. **Weekend policy (product rule beyond assignment):**
   - Monthly employees: weekends blocked for attendance/leave in the UI; ignored in payroll totals
   - Daily wage employees: weekends allowed
5. **No authentication / RBAC in this MVP.** APIs are open. Extra feature delivered is bulk attendance. Auth can be added later without changing domain APIs.
6. **Hibernate `ddl-auto: update`** is used for fast local/demo setup. For hardened production, prefer migrations (Flyway/Liquibase) and `validate`/`none`.
7. **Local DB credentials** live in `application.yaml` for development. Deployment will use environment variables (no secrets in source).
8. **No edit/delete employee UI** — create + list covers assignment scope.
9. **Pagination** is implemented on backend list APIs; UI loads a practical first page size for MVP screens.

---

## Project structure

```
mini-payroll-system/
├── README.md                 ← this file
├── .gitignore
├── backend/                  ← Spring Boot API
│   ├── pom.xml
│   ├── src/main/java/...
│   ├── src/main/resources/application.yaml
│   └── src/test/java/...
└── frontend/                 ← Angular SPA
    ├── package.json
    ├── proxy.conf.json
    └── src/app/...
```

---

## Deployment

The app is packaged as **one Docker image**: Angular is built into Spring Boot `static/`, so reviewers get a **single live URL** (UI + API together). PostgreSQL is a managed database.

### Will the URL stay up for ~24 hours?

**Yes.** Free-tier apps on Render typically stay available for days/weeks for assignment review.

| What happens | Impact |
|--------------|--------|
| Service **sleeps** after ~15 min idle (free web) | First open may take 30–90s (cold start), then works |
| URL itself | Remains valid well beyond 24–48 hours |
| Free DB | Neon free DB persists; open it at least once after create |

Tip for reviewers: if the first load is slow, wait once — do not assume it’s down.

### Deploy on Render (recommended)

#### 1) Push code to GitHub

```bash
cd mini-payroll-system
git init
git add .
git commit -m "Initial commit: Mini Payroll SaaS MVP"
# create empty GitHub repo, then:
git remote add origin https://github.com/<your-user>/mini-payroll-system.git
git branch -M main
git push -u origin main
```

#### 2) Create a free Postgres database (Neon)

1. Sign up at [https://neon.tech](https://neon.tech)
2. Create a project / database
3. Copy the connection string (`postgresql://...` or `postgres://...`)

#### 3) Create Web Service on Render

1. Go to [https://render.com](https://render.com) → **New** → **Web Service**
2. Connect your GitHub repo
3. Settings:
   - **Runtime:** Docker
   - **Dockerfile path:** `./Dockerfile`
   - **Docker build context directory:** `.` (repo root)
   - **Instance type:** Free
4. Environment variables:

| Key | Value |
|-----|--------|
| `DATABASE_URL` | Paste Neon connection string |
| `SPRING_JPA_DDL_AUTO` | `update` |
| `SPRING_JPA_SHOW_SQL` | `false` |

5. Deploy and wait for the build (first build can take several minutes)
6. Open `https://<your-service>.onrender.com`

Optional: `render.yaml` is included for Blueprint deploy; if the free DB plan in the blueprint is unavailable, use Neon + the Web Service steps above.

#### 4) After deploy

1. Paste the live URL at the top of this README
2. Smoke-test: create employee → mark attendance → leave → payroll
3. Swagger: `https://<your-service>.onrender.com/swagger-ui/index.html`

### Local Docker (optional)

```bash
docker build -t mini-payroll .
docker run --rm -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/mini_payroll" \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  mini-payroll
```

---

## Submission checklist

- [x] Working REST APIs (Employee, Attendance, Leave, Payroll)
- [x] Correct payroll formulas + breakdown response
- [x] Angular pages integrated with backend
- [x] PostgreSQL with proper relationships
- [x] Extra feature: bulk attendance
- [x] README (schema, architecture, setup, APIs, assumptions)
- [x] Deployment packaging (Docker + env-based DB config)
- [ ] Live deployment URL
- [ ] GitHub repository

---