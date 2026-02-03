# EventHub API

[![CI](https://github.com/Deviitt11/eventhub-api/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Deviitt11/eventhub-api/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-informational)
![Gradle](https://img.shields.io/badge/Gradle-9.3.0-informational)
![License](https://img.shields.io/badge/License-MIT-success)

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
- PostgreSQL (runtime + Testcontainers for integration tests)
- Springdoc OpenAPI (Swagger UI)
- Gradle

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
- Docker + Docker Compose (recommended for running the app with PostgreSQL via Compose)
- Java 21 (optional, only needed if you want to run without Docker)

- Docker Compose runs the API with PostgreSQL.
- Running without Docker defaults to H2 (see application.yml).

### Run with Docker (recommended)
```bash
docker compose up --build
```

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

## Running Tests

### Unit tests
```bash
./gradlew test
```

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

## CI

GitHub Actions runs:
- `./gradlew check`

on:
- pull requests to `develop` and `main`
- pushes to `develop` and `main`

## Project Structure (high level)
- src/main/java — application code 
- src/test/java — unit tests 
- src/integrationTest/java — integration tests 
- src/integrationTest/resources — integration test config

## Roadmap (high-level)

Planned next steps (as the project evolves):

- Authentication + authorization (JWT / OAuth)
- Observability (structured logs, metrics, traces)
- Event-driven patterns (outbox, messaging)
- Hardening (rate limiting, idempotency, retries)

## License

This project is currently shared for learning and portfolio purposes.
(Choose a license later if you plan to open-source it publicly.)

