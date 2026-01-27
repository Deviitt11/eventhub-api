# EventHub API

[![CI](https://github.com/<Deviitt11>/<eventhub-api>/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/<Deviitt11>/<eventhub-api>/actions/workflows/ci.yml)
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

## Getting Started

### Prerequisites
- Java 21 installed (or a compatible toolchain setup)
- Docker Desktop (required **only** for integration tests with Testcontainers)

### Run locally
```bash
./gradlew bootRun
```

App will start on:

http://localhost:8080

## Running Tests

### Unit tests (fast)
```bash
./gradlew test
```

### Integration tests (PostgreSQL via Testcontainers)
```bash
./gradlew test
```

On Windows, make sure Docker Desktop is running.
If you ever need it, you can set:
- DOCKER_HOST=npipe:////./pipe/docker_engine

## CI

GitHub Actions runs:
- ./gradlew test
- ./gradlew integrationTest

on:

- pull requests to develop and main 
- pushes to develop

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

