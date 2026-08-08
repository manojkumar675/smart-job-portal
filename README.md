# Smart Job Portal

A full-stack job portal with role-based access (Candidate / Recruiter / Admin), dynamic job
search with filters, a rule-based skill-matching score, and JWT-secured REST APIs.

- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA (Specifications), Spring Security, MySQL, Maven
- **Frontend:** React 18, React Router, Axios, plain CSS
- **Infra:** Docker, docker-compose (MySQL + backend + frontend)

---

## 1. Project layout

```
smart-job-portal/
├── backend/                  # Spring Boot REST API
│   ├── src/main/java/com/jobportal/
│   │   ├── config/           # Security config, CORS, data seeder
│   │   ├── security/         # JWT util, filter, user details service
│   │   ├── entity/           # JPA entities
│   │   ├── repository/       # Spring Data repositories (+ Specifications)
│   │   ├── specification/    # Dynamic JobSpecification (filtering)
│   │   ├── dto/               # Request/response records
│   │   ├── service/           # Business logic incl. skill matching
│   │   ├── controller/        # REST controllers
│   │   └── exception/         # Global exception handling
│   ├── src/main/resources/
│   │   ├── application.yml           # common config
│   │   ├── application-dev.yml       # local dev profile (MySQL on localhost)
│   │   └── application-prod.yml      # prod/docker profile
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                 # React app
│   ├── src/
│   │   ├── api/axiosConfig.js        # axios instance + in-memory JWT storage
│   │   ├── context/AuthContext.js    # auth state (token kept in memory only)
│   │   ├── components/               # Navbar, PrivateRoute
│   │   └── pages/
│   │       ├── candidate/            # Job search + apply + match %, applications, profile
│   │       ├── recruiter/            # Dashboard, post/edit job, view applicants
│   │       └── admin/                # Manage users & jobs
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml         # MySQL + backend + frontend
└── postman_collection.json    # Importable Postman collection
```

---

## 2. Running everything with Docker (recommended)

Prerequisites: Docker + Docker Compose.

```bash
cd smart-job-portal
docker compose up --build
```

This starts:
- **MySQL** on `localhost:3306` (db: `job_portal`, user: `jobportal` / `jobportal_pass`)
- **Backend** on `http://localhost:8083` (Spring profile `prod`, seed data enabled)
- **Frontend** on `http://localhost:3009` (Nginx serving the React build, proxying `/api` to the backend)

On first boot the backend seeds sample data automatically (see accounts below). Seeding only runs
once — it's skipped if the `users` table already has rows, so restarting the stack won't create
duplicates.

To stop: `docker compose down` (add `-v` to also wipe the MySQL volume).

---

## 3. Running locally without Docker

### Backend
1. Have MySQL running locally (or update `application-dev.yml`).
2. `cd backend`
3. `mvn spring-boot:run` (defaults to the `dev` profile, connects to `localhost:3306/job_portal_dev`,
   auto-creates the DB, seed data is on by default in dev).

Environment variables you can override: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`,
`JWT_SECRET`, `JWT_EXPIRATION_MS`, `SERVER_PORT`.

### Frontend
1. `cd frontend`
2. `npm install`
3. `npm start` — runs on `http://localhost:3000` and proxies `/api` calls to `http://localhost:8080`
   (see the `proxy` field in `package.json`).

---

## 4. Seeded test accounts

All seeded users share the password **`password123`**.

| Email                     | Role      | Notes                                   |
|---------------------------|-----------|------------------------------------------|
| admin@jobportal.com       | ADMIN     | Full user/job management                 |
| recruiter1@acme.com       | RECRUITER | Acme Corp — posted 2 jobs                |
| recruiter2@globex.com     | RECRUITER | Globex Inc — posted 2 jobs               |
| candidate1@example.com    | CANDIDATE | Skills: Java, Spring Boot, MySQL, Docker |
| candidate2@example.com    | CANDIDATE | Skills: React, JavaScript, CSS           |

---

## 5. Authentication & roles

- Passwords are hashed with **BCrypt**.
- Login returns a **JWT** (`Authorization: Bearer <token>`), valid for 24h by default.
- Endpoints are protected by role via Spring Security (`SecurityConfig`) and method-level
  `@PreAuthorize` on sensitive actions:
  - **CANDIDATE**: search/apply to jobs, manage own profile, view own applications
  - **RECRUITER**: create/edit/delete/activate own jobs, view/manage applicants for own jobs
  - **ADMIN**: manage all users (enable/disable/delete) and moderate any job listing
- The React frontend stores the JWT **only in memory** (a JS variable + React state) — never in
  `localStorage`/`sessionStorage` — so refreshing the page logs the user out by design.

---

## 6. Skill-matching algorithm

Rule-based and fully deterministic (`SkillMatchService`):

```
match % = (number of job-required skills the candidate has) / (total required skills) × 100
```

The score is computed whenever a candidate searches/views jobs and is stored on each `Application`
record at the moment they apply, so recruiters see the score as it was when the candidate applied.

---

## 7. Dynamic job search (Specifications, not hardcoded queries)

`GET /api/jobs` builds a `JobSpecification` on the fly from whichever query params are present —
`location`, `skills` (comma-separated / repeated param), `experienceLevel`, `jobType`, `keyword` —
so any combination of filters works without a matching hardcoded repository method. Results are
paginated and sortable via standard Spring Data `Pageable` params (`page`, `size`, `sort`).

Example:
```
GET /api/jobs?location=bangalore&skills=java,spring&experienceLevel=MID&jobType=FULL_TIME&page=0&size=10&sort=createdAt,desc
```

---

## 8. API overview

Import `postman_collection.json` into Postman for ready-to-run requests. Summary:

| Method | Endpoint                                  | Access             | Purpose |
|--------|--------------------------------------------|--------------------|---------|
| POST   | `/api/auth/register`                       | Public             | Register as CANDIDATE or RECRUITER |
| POST   | `/api/auth/login`                          | Public             | Login, returns JWT |
| GET    | `/api/jobs`                                 | Public             | Search jobs (filters + pagination) |
| GET    | `/api/jobs/{id}`                            | Public             | Job detail |
| POST   | `/api/jobs`                                 | RECRUITER          | Create job |
| PUT    | `/api/jobs/{id}`                            | RECRUITER (owner)  | Edit job |
| DELETE | `/api/jobs/{id}`                            | RECRUITER (owner)  | Delete job |
| PATCH  | `/api/jobs/{id}/status?active=`             | RECRUITER (owner)  | Activate/deactivate job |
| GET    | `/api/candidate/profile`                    | CANDIDATE          | Get own profile |
| PUT    | `/api/candidate/profile`                    | CANDIDATE          | Update own profile & skills |
| GET    | `/api/recruiter/profile`                    | RECRUITER          | Get own recruiter profile |
| PUT    | `/api/recruiter/profile`                    | RECRUITER          | Update own recruiter profile |
| GET    | `/api/recruiter/jobs`                       | RECRUITER          | List own posted jobs |
| POST   | `/api/applications/apply/{jobId}`           | CANDIDATE          | Apply to a job (computes match %) |
| GET    | `/api/applications/mine`                    | CANDIDATE          | List own applications |
| GET    | `/api/applications/job/{jobId}`             | RECRUITER (owner)  | List applicants for a job |
| GET    | `/api/applications/recruiter/all`           | RECRUITER          | List applicants across all own jobs |
| PATCH  | `/api/applications/{id}/status`             | RECRUITER (owner)  | Update applicant status |
| GET    | `/api/admin/users`                          | ADMIN              | List all users |
| PATCH  | `/api/admin/users/{id}/status?enabled=`     | ADMIN              | Enable/disable a user |
| DELETE | `/api/admin/users/{id}`                     | ADMIN              | Delete a user |
| DELETE | `/api/admin/jobs/{id}`                      | ADMIN              | Delete any job |
| PATCH  | `/api/admin/jobs/{id}/status?active=`       | ADMIN              | Activate/deactivate any job |
| GET    | `/api/skills`                               | Public             | List all known skills (for autocomplete) |

---

## 9. Notes & assumptions

- Schema management uses Hibernate `ddl-auto` (`update` in both dev and prod) for simplicity. In a
  real production system, swap this for a migration tool like Flyway or Liquibase.
- `SEED_DATA` seeds demo data only when the `users` table is empty — safe to leave on.
- The skill-matching algorithm is intentionally simple/explainable rather than ML-based, per the
  "rule-based" requirement; it's isolated in `SkillMatchService` so it can be swapped out later.
- CORS is wide-open (`*`) for local development; restrict `corsConfigurationSource()` in
  `SecurityConfig` before deploying publicly.
