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
.\mvnw.cmd clean spring-boot:run
```

- Alternative (skip tests):

```powershell
.\mvnw.cmd -DskipTests spring-boot:run
```

## Docker

Docker Compose (PostgreSQL + backend): see [DOCKER.md](DOCKER.md).

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

## Thông tin thẻ test vnpay

| Thông tin | Chi tiết |
| :--- | :--- |
| **Ngân hàng** | NCB |
| **Số thẻ** | 9704198526191432198 |
| **Tên chủ thẻ** | NGUYEN VAN A |
| **Ngày phát hành** | 07/15 |
| **Mật khẩu OTP** | 123456 |