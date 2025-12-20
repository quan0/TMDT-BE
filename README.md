# TMDTBackend (Spring Boot)

## Requirements

- Windows + PowerShell
- Java 21
- PostgreSQL (local or Docker)

## First-time setup (after cloning)

1) Create your local env file:

- Copy `.env.example` to `.env`
- Edit values in `.env` (especially `DB_*` and `JWT_SECRET`)

2) Ensure PostgreSQL is running and the database exists:

- Default in `.env.example` is `DB_URL=jdbc:postgresql://localhost:5432/tmdt`
- Make sure database `tmdt` exists and the user/password match.

## Run the app (loads .env automatically)

This project loads `.env` via Spring Boot config import (`spring.config.import`).

- Start:

```powershell
cd F:\Study\TMDT\TMDTBackend
.\mvnw.cmd clean spring-boot:run
```

- Alternative (skip tests):

```powershell
.\mvnw.cmd -DskipTests spring-boot:run
```

## Swagger / OpenAPI

After the app starts (default port `8000`):

- Swagger UI: `http://localhost:8000/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8000/v3/api-docs`

If you disable swagger:

- Set `SWAGGER_ENABLED=false` in `.env`

## SQL init / seed data (`SQL_INIT_MODE`)

In `src/main/resources/application.yaml`, SQL init is controlled by:

- `spring.sql.init.mode=${SQL_INIT_MODE:never}`
- `spring.sql.init.data-locations=${SQL_INIT_DATALOCATION:classpath:db/seed.sql}`

Typical values:

- `SQL_INIT_MODE=never`
  - Does not run `seed.sql`.
  - Use this for normal development once your DB already has data.

- `SQL_INIT_MODE=always`
  - Runs `seed.sql` on every app start.
  - WARNING: the seed script usually truncates tables / resets data. Do not use if you need to keep existing data.

## Common problems

### `FATAL: password authentication failed for user "postgres"`

- Verify your `.env` values: `DB_USERNAME`, `DB_PASSWORD`, `DB_URL`.
- Confirm the password by connecting with psql:

```powershell
$env:PGPASSWORD = "<your_password>"
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -U postgres -d tmdt -c "select 1;"
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
```

### `.env` not being applied

- Ensure `.env` is at the project root (same folder as `pom.xml`).
- This repo uses `spring.config.import: optional:file:.env[.properties]` to load it.
