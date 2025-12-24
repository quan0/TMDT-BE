# Docker (TMDTBackend)

This backend can run with PostgreSQL using Docker Compose.

## Prerequisites

- Docker Desktop (with Compose v2)
- Windows PowerShell

## Environment files

This repo uses two env files:

- `.env`: used for running locally with `mvnw` (loaded by Spring via `spring.config.import`)
- `.env.docker`: used by Docker Compose (see `compose.yaml`)

### Create `.env.docker`

1) Copy `.env.example` to `.env.docker`
2) Edit at least:
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` (if you test VNPAY)

Notes:
- In Docker, the database host is the Compose service name `postgres`.
- `compose.yaml` already overrides `DB_URL` to `jdbc:postgresql://postgres:5432/<db>`.

## Start (build + run)

From the project folder:

```powershell
docker compose -f compose.yaml up -d --build
```

Endpoints:

- API: http://localhost:8000
- Swagger UI: http://localhost:8000/swagger-ui.html

## View logs

```powershell
docker compose -f compose.yaml logs -f --tail 200 backend
```

## Stop

```powershell
docker compose -f compose.yaml down
```

## Reset database (delete volume)

WARNING: this deletes all DB data.

```powershell
docker compose -f compose.yaml down -v
```

## Common troubleshooting

- Port already in use: change `PORT` in `.env.docker` (Compose publishes `${PORT}:8000`).
- DB connection issues: check health + logs:

```powershell
docker compose -f compose.yaml ps
docker compose -f compose.yaml logs --tail 200 postgres
```
