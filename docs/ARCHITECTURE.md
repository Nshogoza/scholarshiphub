# Architecture

## Overview

ScholarshipHub is a monolithic Spring Boot API backing a React SPA. The
backend is the strongest part of the system, per the project brief: it
owns all business rules, security, and data integrity; the frontend is a
thin, well-typed consumer of the API.

```
┌─────────────┐        ┌────────────────────────────────────────────┐
│   React SPA │ ─────▶ │  Spring Boot API (com.scholarshiphub)       │
│ (Vite, TS)  │  HTTPS │  Controller → Service → Repository → DB     │
└─────────────┘        └───────────────────┬──────────────────────────┘
                                            │
                                   ┌────────┴────────┐
                                   │   PostgreSQL     │
                                   │ (Flyway-managed) │
                                   └──────────────────┘
```

In production (Docker Compose), Nginx serves the built SPA and reverse
proxies `/api/v1/*` to the backend, so the browser only ever talks to one
origin -- no CORS is needed there, and the refresh-token cookie is same-site
by construction. In local dev, Vite's dev server and the backend run on
different ports, so CORS is configured for `http://localhost:5173`.

## Backend layering

The codebase follows a **layered (package-by-layer)** architecture, exactly
as specified in the project brief, rather than package-by-feature:

```
com.scholarshiphub
├── controller/       REST endpoints only -- no business logic. Validates
│                     input via @Valid, delegates to a service, wraps the
│                     result in ApiResponse<T> / PageResponse<T>.
├── service/          Interfaces describing business operations.
│   └── impl/         Implementations. All business rules, transactions,
│                     and orchestration across repositories live here.
├── repository/       Spring Data JPA interfaces (+ specification/ for
│                     dynamic filter/sort/paginate queries).
├── entity/           JPA entities (+ enums/ for status/role enums).
├── dto/               Wire types.
│   ├── request/      Input, validated with jakarta.validation annotations.
│   └── response/     Output, decoupled from entities and from Spring Data's
│                     Page<T> (see PageResponse<T>).
├── mapper/           MapStruct interfaces: entity <-> DTO, zero hand-written
│                     boilerplate, compile-time generated and null-safe.
├── security/          JWT issuing/validation, the auth filter, UserDetails
│                     adapter, cookie handling.
├── config/            Spring @Configuration classes (security, CORS,
│                     OpenAPI, async executor, JPA auditing, typed
│                     @ConfigurationProperties).
├── exception/         BusinessException hierarchy + a single
│                     @RestControllerAdvice that turns every exception into
│                     a uniform ErrorResponse.
└── validation/        Custom bean-validation constraints (@StrongPassword).
```

Controllers never contain business logic; a controller method is a thin
translation of "HTTP request -> service call -> HTTP response." Every
mutating service method is `@Transactional`; read paths are
`@Transactional(readOnly = true)`.

**Dependency direction** is strictly controller → service → repository →
entity. Services depend on other services and repositories only, never on
controllers; entities have no framework dependency beyond JPA annotations
(Spring Security's `UserDetails` is implemented by a separate
`security.UserPrincipal` adapter, not by the `User` entity itself).

## Data model

Twelve tables, one Flyway migration per logical group
(`backend/src/main/resources/db/migration`):

| Table                              | Purpose |
|-------------------------------------|---------|
| `roles`                             | Reference table: STUDENT, REVIEWER, ADMIN |
| `users`                             | Identity, auth state, lockout counters |
| `student_profiles`                  | 1:1 with `users`; education/GPA/personal statement -- kept off `users` so reviewer/admin rows don't carry unused nullable columns |
| `refresh_tokens`                    | Hashed, rotating refresh tokens (see below) |
| `email_verification_tokens` / `password_reset_tokens` | Single-use, hashed, expiring tokens |
| `scholarships`                      | Scholarship definitions and lifecycle status |
| `scholarship_required_documents`    | Normalized child table (not a JSON column), so it can be foreign-keyed and queried relationally |
| `applications`                      | One row per (scholarship, student); status machine |
| `application_documents`             | Uploaded file metadata; the file itself lives on disk |
| `reviews`                           | Append-only: every reviewer decision is a new row, giving a full audit trail without a separate history table |
| `audit_logs`                        | Generic security/business event log (logins, submissions, admin actions) |

All foreign keys, `NOT NULL`, and `CHECK` constraints (e.g. status enums,
`amount > 0`, file size bounds) are enforced at the database level, not just
in application code.

### Status machines

**Scholarship:** `DRAFT → PUBLISHED → CLOSED ⇄ PUBLISHED → ARCHIVED` (terminal).
Enforced in `ScholarshipServiceImpl` via an explicit transition table --
invalid transitions (e.g. skipping straight from DRAFT to CLOSED) are
rejected with a 409.

**Application:**
`DRAFT/ADDITIONAL_INFO_REQUIRED --submit--> SUBMITTED/UNDER_REVIEW`
(skips straight to UNDER_REVIEW if a reviewer is already assigned)
`--assign reviewer--> UNDER_REVIEW --review--> APPROVED | REJECTED | ADDITIONAL_INFO_REQUIRED`.
A student can only edit/upload documents while in `DRAFT` or
`ADDITIONAL_INFO_REQUIRED`; submission re-validates every mandatory
required document is present and the deadline hasn't passed.

## Authentication & authorization

- **Access tokens**: short-lived (15 min default) JWTs (HS256), returned in
  the JSON response body, sent by the client via `Authorization: Bearer`.
  Never persisted client-side beyond an in-memory JS variable (not
  `localStorage`), to limit exposure if an XSS bug ever slipped through.
- **Refresh tokens**: opaque random values (not JWTs), stored **hashed**
  (SHA-256) server-side in `refresh_tokens`, delivered only via an
  `httpOnly`, `Secure` (in prod), `SameSite=Strict` cookie scoped to
  `/api/v1/auth`. Every refresh **rotates** the token (issues a new one,
  revokes the old) and tags all tokens from one login with a shared
  `family_id`. If an already-revoked token is ever presented again, the
  entire family is revoked -- this is the standard mitigation for a stolen
  refresh token being replayed after the legitimate client has already
  rotated past it.
- **RBAC**: enforced primarily via `@PreAuthorize` at the controller layer
  (`hasRole('ADMIN')`, etc.), colocated with each endpoint. Row-level
  ownership checks (a student may only see their own application; a
  reviewer only their assigned ones) are enforced in the service layer,
  since they require loading the entity first.
- **Account lockout**: 5 failed attempts (configurable) locks the account
  for 15 minutes (configurable); the lock auto-clears once the window
  elapses.
- **Email verification**: required to log in by default
  (`REQUIRE_EMAIL_VERIFICATION=true`). Local/dev uses MailHog as an SMTP
  sink so the real `JavaMailSender` code path is exercised without a real
  mailbox.
- **Password hashing**: BCrypt, strength 10.

## File uploads

Uploaded documents (PDF/DOCX, max 10MB, validated by both content-type and
size) are stored on local disk behind a `StorageService` interface --
`LocalFileStorageService` is the only implementation today, but a future
S3-backed implementation would not require touching the application or
controller layers. Every stored filename is a generated UUID (never the
client-supplied name), and the resolved path is checked to stay within the
configured upload root, structurally preventing path traversal. A SHA-256
checksum is computed and stored alongside each file's metadata.

## Cross-cutting concerns

- **Response envelope**: every endpoint returns `ApiResponse<T>`
  (`{ success, message, data, timestamp }`); paginated endpoints wrap their
  page in `PageResponse<T>`, decoupled from Spring Data's `Page<T>` so the
  wire format never leaks Spring Data internals.
- **Error handling**: a single `GlobalExceptionHandler`
  (`@RestControllerAdvice`) maps a small hierarchy of `BusinessException`
  subtypes (each carrying an `ErrorCode` with its own HTTP status), plus
  Spring's validation/security exceptions, to a uniform `ErrorResponse`.
  No controller or service ever formats an HTTP error response itself.
- **Auditing**: `AuditLogService` writes to both the durable `audit_logs`
  table (queried by the admin dashboard) and a dedicated `AUDIT` log
  stream (see `logback-spring.xml`), so the same event is available for
  both live querying and offline log analysis. It runs in
  `REQUIRES_NEW` so an audit entry for a failed operation still commits
  even when the surrounding transaction rolls back.
- **Structured logging**: every log line carries a request-correlation id
  and (once authenticated) the acting user id via SLF4J MDC
  (`RequestContextLoggingFilter`), so a single request's logs can be
  traced across filters, services, and the audit log.

## Frontend

React + TypeScript + Vite + Tailwind CSS, using:

- **React Router** for client-side routing, with a `ProtectedRoute`
  component gating both authentication and role.
- **TanStack Query** for all server data -- caching, refetch-on-mutation,
  and loading/error states, instead of hand-rolled `useEffect` fetching.
- **Axios** with a single interceptor pair: requests attach the in-memory
  access token; a 401 response triggers exactly one in-flight silent
  refresh (via the httpOnly cookie), then retries the original request --
  or, if the refresh itself fails, clears the session and redirects to
  `/login`.
- A small `types/` module mirroring the backend's DTOs, so the two layers
  stay in sync by inspection.

## Testing strategy

- **Unit tests** (`backend/src/test/java/.../unit`) mock every collaborator
  and target the highest-risk business logic in isolation (the
  login/lockout state machine).
- **Integration tests** (`.../integration`) boot the full Spring context
  against a real, throwaway PostgreSQL container via **Testcontainers**
  (Flyway migrations run exactly as in production) and drive the API
  through MockMvc, covering: the full register → verify → apply → assign →
  review → approve lifecycle across all three roles, and the RBAC
  boundaries (401 unauthenticated, 403 wrong role) that the rest of the
  system depends on.
