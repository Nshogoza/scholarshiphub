# Deployment Guide

## 1. Docker Compose (local / single-host)

The simplest path, and the one exercised in CI-equivalent local testing.

```bash
cp .env.example .env   # edit values, especially JWT_SECRET, for anything beyond a laptop demo
docker compose up --build -d
docker compose logs -f backend   # watch Flyway migrate + the app come up
```

Services: `postgres`, `mailhog` (dev SMTP sink), `backend`, `frontend`
(Nginx, reverse-proxying `/api/v1/*` to the backend and serving the SPA).
Data persists in the named volumes `postgres-data`, `backend-uploads`,
`backend-logs` -- `docker compose down -v` removes them.

For anything beyond a local demo:

- Set `JWT_SECRET` to a strong random value (`openssl rand -base64 48`).
- Set `COOKIE_SECURE=true` once you're serving over HTTPS (required for the
  refresh-token cookie to be sent).
- Point `MAIL_HOST`/`MAIL_PORT`/`MAIL_USERNAME`/`MAIL_PASSWORD` at a real
  SMTP provider instead of MailHog.
- Put a reverse proxy / TLS terminator (Caddy, Traefik, or a cloud load
  balancer) in front of the `frontend` container.

## 2. Railway

Railway can run this as three services from one repo (Postgres is a Railway
plugin, not a container you manage):

1. **Create a project**, add a **PostgreSQL** plugin -- Railway provisions
   `DATABASE_URL` and individual `PGHOST`/`PGPORT`/`PGDATABASE`/`PGUSER`/`PGPASSWORD`
   variables automatically.
2. **Backend service**: "Deploy from repo", set the root directory to
   `backend/`. Railway detects the `Dockerfile` and builds it directly.
   Set environment variables (Settings → Variables):
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` -- map
     these to the Postgres plugin's `PGHOST`/`PGPORT`/`PGDATABASE`/
     `PGUSER`/`PGPASSWORD` via Railway's variable references
     (`${{Postgres.PGHOST}}` etc.).
   - `JWT_SECRET` (generate a strong one), `COOKIE_SECURE=true`,
     `FRONTEND_URL` and `CORS_ALLOWED_ORIGINS` set to the frontend
     service's public Railway URL.
   - `MAIL_HOST` / `MAIL_PORT` / credentials for a real SMTP provider
     (Railway has no built-in mail catcher for production use).
   - Expose the service publicly; Railway assigns a `*.up.railway.app`
     domain and terminates TLS for you (satisfying `COOKIE_SECURE=true`).
3. **Frontend service**: a second service from the same repo with root
   directory `frontend/`. Since the frontend's Dockerfile bakes in
   `VITE_API_BASE_URL=/api/v1` and expects Nginx to proxy `/api/v1/*`,
   either (a) edit `frontend/nginx.conf`'s `proxy_pass` to point at the
   backend service's Railway-internal hostname before building, or (b)
   simpler on Railway: set a build arg / env var so Vite calls the
   backend's public URL directly and relax CORS on the backend to allow
   the frontend's Railway URL. Option (b) needs no Nginx proxy config
   changes -- just rebuild the frontend with
   `VITE_API_BASE_URL=https://<backend-service>.up.railway.app/api/v1`.
4. Attach persistent storage to the backend service (Railway Volumes) at
   `/app/uploads` so uploaded documents survive redeploys.

## 3. Render

1. **New PostgreSQL** instance (Render Dashboard → New → PostgreSQL). Copy
   the internal connection details.
2. **New Web Service** for the backend:
   - Root directory `backend/`, environment "Docker" (Render builds the
     `Dockerfile` directly).
   - Environment variables: same set as Railway above, with `DB_HOST` etc.
     from the Render Postgres instance's internal hostname/credentials.
   - Add a **Disk** (Render's persistent volume) mounted at `/app/uploads`.
   - Health check path: `/actuator/health`.
3. **New Static Site or Web Service** for the frontend:
   - If using Render's **Static Site** type: build command `npm ci && npm run build`,
     publish directory `dist`, and set `VITE_API_BASE_URL` to the
     backend web service's public `.onrender.com` URL + `/api/v1` (a
     static site can't run the Nginx reverse proxy, so the SPA calls the
     backend directly -- ensure `CORS_ALLOWED_ORIGINS` on the backend
     includes the static site's URL, and `COOKIE_SECURE=true` +
     `COOKIE_DOMAIN` set appropriately so the refresh cookie round-trips).
   - If using the **Docker Web Service** type instead (keeping Nginx as the
     proxy): same approach as Railway option (a) above -- update
     `nginx.conf`'s `proxy_pass` target to the backend's Render internal
     hostname before building.

## 4. AWS (reference architecture)

A minimal, still-production-reasonable layout using managed services:

- **RDS for PostgreSQL** (single-AZ is fine for a portfolio deployment;
  enable automated backups). Place it in a private subnet.
- **ECR**: push both the `backend` and `frontend` images
  (`docker build`, `docker tag`, `docker push` per Dockerfile in this repo).
- **ECS Fargate**: one service per image.
  - Backend task definition: container port 8080, environment variables
    from **Secrets Manager** (`JWT_SECRET`, `DB_PASSWORD`, mail
    credentials) rather than plaintext task-def env vars. Mount an **EFS**
    access point at `/app/uploads` so uploaded files persist across task
    restarts/scale-out (a single Fargate task's local disk does not).
  - Frontend task definition: container port 80. Either bake the real
    backend URL into `VITE_API_BASE_URL` at build time, or update
    `nginx.conf`'s `proxy_pass` to the backend's internal Service Discovery
    / ALB DNS name before building the image.
- **Application Load Balancer**: one ALB (or two target groups behind one,
  path-routed) in front of both services; terminate TLS here with an ACM
  certificate. Point the backend's `CORS_ALLOWED_ORIGINS` /
  `FRONTEND_URL` / `COOKIE_DOMAIN` at the ALB's public domain.
- **SES** for outbound mail instead of MailHog; verify the sending domain.
- **CloudWatch Logs**: ECS task definitions ship container stdout (this
  app's console appender) to CloudWatch by default with the `awslogs`
  driver -- no code changes needed.
- **Route 53 + ACM** for the public domain and certificate.

This is a reference shape, not a Terraform/CDK stack included in this repo
-- translate it to your team's existing IaC tooling.

## Environment variable reference

All variables and their defaults are documented inline in
[`.env.example`](../.env.example) (compose-level),
[`backend/.env.example`](../backend/.env.example), and
[`frontend/.env.example`](../frontend/.env.example). The backend reads them
via Spring's standard `${VAR:default}` placeholders in
`application.yml` -- nothing is hardcoded.
