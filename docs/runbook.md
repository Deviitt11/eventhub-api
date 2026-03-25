# Runbook (host + compose)

**Windows PowerShell note:** `curl` is an alias for `Invoke-WebRequest`. Use `curl.exe` or `Invoke-RestMethod`.

Goal: validate the two supported local flows and confirm Flyway-managed persistence without deviating from CI-supported behavior.

## 0) Prerequisites

- Copy `.env.example` to `.env`.
- Set a non-empty `POSTGRES_PASSWORD`.
- Keep Docker running before using Compose or Gradle integration checks.

## A) Recommended daily flow: API on host + DB via Compose

### 1) Start only PostgreSQL
```bash
docker compose up -d db
```

### 2) Run the API locally with the dev profile
```bash
./gradlew bootRun --args="--spring.profiles.active=dev"
```

### 3) Smoke the running app
```bash
curl.exe -fsS http://localhost:8080/actuator/health
curl.exe -fsS http://localhost:8080/api-docs
```

### 4) Optional CRUD smoke
```bash
curl.exe -s -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-123" \
  -d '{
    "title": "My Event",
    "startsAt": "2030-01-01T10:00:00Z",
    "endsAt": "2030-01-01T11:00:00Z"
  }'
```

---

## B) Full Compose stack: API + DB

### 1) Build and start the stack
```bash
docker compose up -d --build
```

### 2) Smoke the containerized app
```bash
curl.exe -fsS http://localhost:8080/actuator/health
curl.exe -fsS http://localhost:8080/api-docs
```

### 3) Inspect service state if needed
```bash
docker compose ps
docker compose logs --no-color api
```

---

## C) Flyway / persistence sanity checks

These checks are useful after either flow A or B.

### List tables
```bash
docker compose exec db psql -U eventhub -d eventhub -c "\dt"
```

### Inspect Flyway history
```bash
docker compose exec db psql -U eventhub -d eventhub -c "select installed_rank, version, description, type, script, checksum, installed_on, success from flyway_schema_history order by installed_rank;"
```

### Inspect the `events` table
```bash
docker compose exec db psql -U eventhub -d eventhub -c "\d events"
```

### Check inserted rows (top 20)
```bash
docker compose exec db psql -U eventhub -d eventhub -c "select id, title, starts_at, ends_at, created_at from events order by created_at desc limit 20;"
```

Expected:

- table _flyway_schema_history_ exists
- at least one row with _version = '1'_ (or similar) for `V1__create_events_table.sql`
- _success = true_

---

## D) CI-aligned validation commands

Run the same command families that the repository expects locally:

```bash
docker compose up -d db
./gradlew bootRun --args="--spring.profiles.active=dev"
docker compose up -d --build
./gradlew check
```

Additional CI-specific sanity check:

```bash
docker compose config
```

What to confirm manually:

- `bootRun` with `dev` connects to `localhost:5432` and serves `/actuator/health`.
- `docker compose up -d --build` brings up both `db` and `api`, and the API becomes healthy on port `8080`.
- `./gradlew check` completes with Docker available for Testcontainers.

---

## E) Cleanup

```bash
docker compose down
docker compose down -v
```

Use `down -v` only when you intentionally want to wipe database data.

---

## Notes

- Prefer `docker compose logs ...` over container-name-based `docker logs ...`; service names are stable.
- The canonical Swagger UI path from the current Springdoc config is `/swagger-ui.html`.
