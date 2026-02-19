# EventHub API

[![CI](https://github.com/Deviitt11/eventhub-api/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Deviitt11/eventhub-api/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-informational)
![Gradle](https://img.shields.io/badge/Gradle-9.3.0-informational)

EventHub is a backend-first REST API for creating and managing events with production-minded defaults: validation, consistent error contracts, OpenAPI docs, and a clean path to real integration testing.

This project is built as a solid backend slice you can ship, extend, and trust:
- **Consistent API contracts** (request/response + error payloads)
- **Validation that protects data quality** (and your future self)
- **Integration tests with real PostgreSQL** (Testcontainers) to reduce “works on my machine”
- **CI-ready** (GitHub Actions running unit + integration tests)

---

## What value does this project offer?

- **Reliability:** integration tests run against a real database, catching issues H2 won’t.
- **Maintainability:** clear boundaries (controller/service/repository) and predictable error responses.
- **Speed to iterate:** OpenAPI/Swagger UI makes it easy to test and evolve endpoints.
- **Real-world readiness:** designed to be extended (auth, event-driven flows, observability) without rewrites.

---

## Docs

High-level documentation for reviewers and contributors:

- Architecture: `docs/architecture.md`
- Security: `docs/security.md`
- Requirements: `docs/requirements.md`
- PRD / Scope: `docs/prd.md`
- ADRs: `docs/adr/` (e.g., `docs/adr/0001-observability-baseline.md`)

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Web + Validation + Data JPA
- PostgreSQL (runtime; Testcontainers for integration tests)
- H2 (unit tests only)
- Springdoc OpenAPI (Swagger UI)
- Gradle
- Flyway (schema migrations)

---

## API Overview

Base path: `/api/v1`

### Events
- `POST /events` — create event
- `GET /events` — list events
- `GET /events/{id}` — get event by id
- `PUT /events/{id}` — update event
- `DELETE /events/{id}` — delete event

### Error Contract (standard payload)
Errors are returned using a consistent payload shape, for example:
- `VALIDATION_ERROR` (400)
- `DOMAIN_VALIDATION_ERROR` (400)
- `NOT_FOUND` (404)

---

## Swagger / OpenAPI

Once the app is running, Swagger UI is available at:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`

---

## Observability

This project includes minimal observability defaults to support safe debugging and consistent request tracing.

- **Actuator base:** `/actuator` (e.g., `GET /actuator/health`)
- **Correlation ID:** requests accept `X-Correlation-Id`. If missing, the API generates one and echoes it back in the response.

For design decisions, see: `docs/adr/0001-observability-baseline.md`.

---

## Demo (curl)

**Windows PowerShell note:** `curl` is an alias for `Invoke-WebRequest`. Use `curl.exe` or `Invoke-RestMethod` instead.

### 1) Healthcheck
```bash
curl -i http://localhost:8080/actuator/health
```

### 2) Correlation ID (echo when provided)
```bash
curl -i -H "X-Correlation-Id: demo-123" http://localhost:8080/api/v1/events
```

### 3) Correlation ID (generated when missing)
```bash
curl -i http://localhost:8080/api/v1/events
```

### 4) Minimal E2E (create → fetch → delete)
```bash
# create
curl -s -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-123" \
  -d '{
    "title": "My Event",
    "startsAt": "2030-01-01T10:00:00Z",
    "endsAt": "2030-01-01T11:00:00Z"
  }'

# replace <id> from the response
curl -i -H "X-Correlation-Id: demo-123" http://localhost:8080/api/v1/events/<id>

curl -i -X DELETE -H "X-Correlation-Id: demo-123" http://localhost:8080/api/v1/events/<id>
```
---

## Getting Started

### Prerequisites
- Docker + Docker Compose (recommended for local development with PostgreSQL)
- Java 21 (only needed if you want to run the API locally outside Docker)

### Environment
Copy `.env.example` to `.env` and adjust values if needed:

```bash
cp .env.example .env
```

On Windows PowerShell, `cp` works as well.

---

## DB & migrations (Flyway)

EventHub uses **Flyway** for schema migrations.

### How migrations run
- On application startup (profiles `docker` and `dev`), Flyway validates and applies pending migrations from:
  - `src/main/resources/db/migration/`
- Migration history is tracked in `flyway_schema_history`.

### Reset DB (start from scratch)
This will remove containers **and** delete the Postgres volume (all data):

```bash
docker compose down -v
docker compose up --build
```

### Verify migrations / tables

```bash
docker compose exec db psql -U eventhub -d eventhub -c "select * from flyway_schema_history order by installed_rank;"
docker compose exec db psql -U eventhub -d eventhub -c "\dt"
```

---

### Local dev (recommended): PostgreSQL in Docker + API on host (dev profile)

```bash
docker compose up -d db
./gradlew bootRun --args="--spring.profiles.active=dev"
```
**Note**: The default runtime is PostgreSQL via profiles (`dev` / `docker`). H2 is used only for unit tests.

Verify migrations / tables:

```bash
docker compose exec db psql -U eventhub -d eventhub -c "\dt"
docker compose exec db psql -U eventhub -d eventhub -c "\d events"
```

Check inserted rows:

```bash
docker compose exec db psql -U eventhub -d eventhub -c "select id, title, starts_at, ends_at, created_at from events order by created_at desc limit 20;"
```

---

### Full Docker (API + DB)
```bash
docker compose up -d --build
docker compose logs -f api
```

---

### Useful URLs

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/api-docs
- Health: http://localhost:8080/actuator/health

### Stop and cleanup
```bash
docker compose down
```

If you also want to remove volumes (database data):
```bash
docker compose down -v
```

---

## Running Tests

### Unit tests
```bash
./gradlew test
```
**Notes**:
- Unit tests use an in-memory H2 datasource (test scope only).
- Integration tests run against PostgreSQL via Testcontainers.

### Integration tests (PostgreSQL via Testcontainers)
```bash
./gradlew integrationTest
```

### Verify all (unit + integration)
```bash
./gradlew check
```

Notes:
- Docker must be running for integrationTest / check. 
- On Windows, Testcontainers should auto-detect Docker Desktop. If you ever need to force it:
  - DOCKER_HOST=npipe:////./pipe/docker_engine

---

## CI

GitHub Actions runs:
- `./gradlew check`

on:
- pull requests to `develop` and `main`
- pushes to `develop` and `main`

---

## Project Structure (high level)
- src/main/java — application code 
- src/test/java — unit tests 
- src/integrationTest/java — integration tests 
- src/integrationTest/resources — integration test config

---

## Roadmap (high-level)

Planned next steps (as the project evolves):

- Authentication + authorization (JWT / OAuth)
- Observability (structured logs, metrics, traces)
- Event-driven patterns (outbox, messaging)
- Hardening (rate limiting, idempotency, retries)

---

## License

See `LICENSE`.
