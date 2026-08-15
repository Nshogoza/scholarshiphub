# API Reference

Full, always-up-to-date request/response schemas are generated at runtime:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Raw OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

This document covers the flows and endpoint map that aren't obvious from
schemas alone.

## Conventions

- All endpoints are under `/api/v1`.
- Every response is wrapped: `{ "success": bool, "message"?: string, "data": T, "timestamp": string }`.
- Every error is: `{ "success": false, "errorCode": string, "message": string, "path": string, "timestamp": string, "fieldErrors"?: [...] }`.
- Paginated list endpoints accept `page` (0-indexed), `size`, and `sort`
  (e.g. `sort=createdAt,desc`) and return
  `{ "content": T[], "page", "size", "totalElements", "totalPages", "first", "last" }`.
- Authenticated requests send `Authorization: Bearer <accessToken>`.

## Authentication flow

1. `POST /auth/register` -- creates a STUDENT account (public registration
   never creates REVIEWER/ADMIN accounts) and emails a verification link.
2. `GET /auth/verify-email?token=...` -- verifies the email. Login is
   rejected with `EMAIL_NOT_VERIFIED` until this step completes (unless
   `REQUIRE_EMAIL_VERIFICATION=false`).
3. `POST /auth/login` -- returns `{ accessToken, expiresInSeconds, user }`
   in the body and sets a `refreshToken` **httpOnly cookie** (never
   readable by JS). Repeated failed attempts lock the account
   (`ACCOUNT_LOCKED`, HTTP 423) after 5 tries for 15 minutes by default.
4. `POST /auth/refresh` -- reads the refresh cookie, rotates it (revokes
   the old one, issues a new one), and returns a fresh access token. The
   frontend's Axios interceptor calls this automatically on a 401.
5. `POST /auth/logout` -- revokes the current refresh-token session family
   and clears the cookie.
6. `POST /auth/forgot-password` / `POST /auth/reset-password` -- always
   responds success-shaped even for an unknown email, to avoid leaking
   which addresses have accounts.
7. `PUT /auth/change-password` -- authenticated; revokes all other
   sessions on success.

## Endpoint map

| Area | Method & path | Role |
|---|---|---|
| Auth | `POST /auth/register` | public |
| Auth | `POST /auth/login` | public |
| Auth | `POST /auth/refresh` | public (cookie) |
| Auth | `POST /auth/logout` | authenticated |
| Auth | `GET /auth/verify-email` | public |
| Auth | `POST /auth/resend-verification` | public |
| Auth | `POST /auth/forgot-password` | public |
| Auth | `POST /auth/reset-password` | public |
| Auth | `PUT /auth/change-password` | authenticated |
| Profile | `GET/PUT /users/me` | authenticated |
| Profile | `GET/PUT /users/me/student-profile` | STUDENT |
| Scholarships | `GET /scholarships`, `GET /scholarships/{id}` | authenticated |
| Scholarships (admin) | `GET/POST /admin/scholarships`, `PUT/DELETE /admin/scholarships/{id}`, `PATCH /admin/scholarships/{id}/status` | ADMIN |
| Applications | `POST /applications`, `GET /applications/me`, `GET /applications/{id}` | STUDENT (own) |
| Applications | `POST/DELETE /applications/{id}/documents...`, `GET .../download`, `POST /applications/{id}/submit` | STUDENT (own) |
| Reviewer | `GET /reviewer/applications`, `GET /reviewer/applications/{id}`, `POST /reviewer/applications/{id}/reviews` | REVIEWER (assigned) |
| Applications (admin) | `GET /admin/applications`, `GET /admin/applications/{id}`, `PATCH /admin/applications/{id}/assign-reviewer` | ADMIN |
| Users (admin) | `GET/POST /admin/users`, `PATCH /admin/users/{id}/status` | ADMIN |
| Dashboard (admin) | `GET /admin/analytics`, `GET /admin/audit-logs` | ADMIN |

## Error codes

| `errorCode` | HTTP status | Meaning |
|---|---|---|
| `VALIDATION_FAILED` | 400 | Request body failed bean validation; see `fieldErrors` |
| `INVALID_CREDENTIALS` | 401 | Bad email/password, or unauthenticated |
| `EMAIL_NOT_VERIFIED` | 403 | Login blocked pending email verification |
| `ACCESS_DENIED` | 403 | Authenticated but lacks permission (wrong role or not the owner) |
| `ACCOUNT_DISABLED` | 403 | Account disabled by an administrator |
| `RESOURCE_NOT_FOUND` | 404 | Entity id doesn't exist |
| `DUPLICATE_RESOURCE` | 409 | e.g. email already registered, duplicate application |
| `INVALID_STATE` | 409 | Action not valid for the entity's current status |
| `ACCOUNT_LOCKED` | 423 | Too many failed logins; retry after the lockout window |
| `FILE_VALIDATION_FAILED` | 400 | Wrong file type or over the 10MB limit |
| `INVALID_TOKEN` | 400 | Expired/invalid/already-used verification, reset, or refresh token |
| `INTERNAL_ERROR` | 500 | Unexpected server error |
