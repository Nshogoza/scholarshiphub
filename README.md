# ScholarshipHub

A production-grade, full-stack scholarship application management platform:
students apply for scholarships, reviewers evaluate applications, and
administrators manage the platform end to end.

**Stack:** Java 21 · Spring Boot 3 · Spring Security · PostgreSQL · Flyway ·
React 19 · TypeScript · Vite · Tailwind CSS · Docker.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the system design,
[`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md) for deploying to Docker Compose,
Railway, Render, or AWS, and [`docs/API.md`](docs/API.md) for the API
reference (Swagger UI is also generated at runtime).

## Quickstart (Docker Compose)

Requires Docker and Docker Compose.

```bash
cp .env.example .env
# Edit .env if you want to change ports/secrets; the defaults work out of the box.
docker compose up --build
```

This starts four containers:

| Service    | URL                              | Purpose                              |
|------------|-----------------------------------|---------------------------------------|
| frontend   | http://localhost:8081             | React SPA, served by Nginx            |
| backend    | http://localhost:8080             | Spring Boot API                       |
| postgres   | localhost:5432                    | Database                              |
| mailhog    | http://localhost:8025             | Catches every email the app sends     |

Swagger UI: http://localhost:8080/swagger-ui.html

### Seeded demo accounts

Flyway seeds one account per role so you can exercise every workflow
immediately (see `backend/src/main/resources/db/migration/V10__...sql`).
**Rotate or remove these before any real deployment.**

| Role     | Email                         | Password        |
|----------|--------------------------------|------------------|
| Admin    | admin@scholarshiphub.com       | Admin@12345      |
| Reviewer | reviewer@scholarshiphub.com    | Reviewer@12345   |
| Student  | student@scholarshiphub.com     | Student@12345    |

### Try the full flow

1. Log in as the seeded **admin**, go to *Scholarships → New scholarship*,
   fill it in, then click *Publish*.
2. Register a new **student** account (or use the seeded one), verify the
   email via the MailHog UI at http://localhost:8025, then log in.
3. Browse scholarships, start an application, upload a document, and submit.
4. Log back in as **admin** → *Applications* → assign the seeded reviewer.
5. Log in as **reviewer**, open the application, and approve/reject/request
   more info.
6. Log back in as the student to see the status change, or as admin to see
   it reflected in the dashboard analytics and audit log.

## Local development (without Docker)

**Backend** (needs a local PostgreSQL 16 running with the database from
`backend/.env.example`):

```bash
cd backend
cp .env.example .env   # then export its variables, or configure them in your IDE run config
./mvnw spring-boot:run
```

**Frontend:**

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

## Running the tests

```bash
cd backend
./mvnw test
```

Unit tests run with no external dependencies. Integration tests use
[Testcontainers](https://testcontainers.com/) to boot a real, throwaway
PostgreSQL container and drive the API through Spring's MockMvc against the
real Flyway-migrated schema -- Docker must be running.

## Project layout

```
backend/    Spring Boot 3 API -- see docs/ARCHITECTURE.md for the package layout
frontend/   React + TypeScript + Vite SPA
docs/       Architecture, deployment, and API documentation
docker-compose.yml
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md) -- layering, data model, security design, key decisions
- [Deployment guide](docs/DEPLOYMENT.md) -- Docker Compose, Railway, Render, AWS
- [API reference](docs/API.md) -- authentication flow and endpoint summary (full detail in Swagger UI)
