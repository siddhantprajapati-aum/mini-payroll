# PayFlow — Frontend

Angular SPA for the Mini Payroll & Attendance System.

For full system design, database schema, API contracts, and project assumptions, see the [root README](../README.md).

## Tech stack

- Angular 19 (standalone components)
- TypeScript (strict)
- Reactive Forms + Signals
- RxJS / HttpClient
- SCSS

## Prerequisites

- Node.js 18+ and npm
- Backend running on `http://localhost:8080` (see [`../backend/README.md`](../backend/README.md))

## Run locally

```bash
cd frontend
npm install
npm start
```

- App: http://localhost:4200  
- Dev proxy: `/api` → `http://localhost:8080` (`proxy.conf.json`)

### Production build

```bash
npm run build
```

Output: `dist/mini-payroll-ui/`

## Project structure

```
frontend/src/app/
├── layout/shell/           # Sidebar / top nav shell
├── features/
│   ├── employees/          # Create + list employees
│   ├── attendance/         # Single + bulk mark, history
│   ├── leave/              # Apply + approve/reject
│   └── payroll/            # Generate salary breakdown
└── core/
    ├── models/             # TypeScript interfaces / enums
    ├── services/           # HTTP API clients
    └── utils/              # Date rules, API error helpers
```

## Pages & routes

| Route | Page | What it does |
|-------|------|----------------|
| `/employees` | Employees | Create employee, list workforce |
| `/attendance` | Attendance | Mark day, bulk mark range, view history |
| `/leave` | Leave | Apply leave, approve/reject pending |
| `/payroll` | Payroll | Generate monthly salary breakdown |

Default route redirects to `/employees`.

## API integration

- Base path: `/api/v1` (`src/environments/environment.ts`)
- Services map 1:1 to backend controllers (`Employee`, `Attendance`, `Leave`, `Payroll`)
- Local development uses Angular proxy (no CORS issues in the browser)
- API errors are parsed and shown as user-facing alerts

## UI rules (client-side)

| Rule | Behavior |
|------|----------|
| Required fields | Forms block empty submits |
| Attendance dates | No future dates |
| Leave dates | No past dates |
| Weekends (monthly) | Blocked for attendance/leave; bulk skips weekends |
| Weekends (daily) | Allowed |
| Empty workforce | Attendance / Leave / Payroll prompt to create employees first |

## Scripts

| Command | Description |
|---------|-------------|
| `npm start` | Dev server (with API proxy) |
| `npm run build` | Production build |
| `npm test` | Unit tests (Karma) |
