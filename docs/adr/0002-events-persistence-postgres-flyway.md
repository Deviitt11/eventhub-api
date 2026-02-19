# ADR 0002 — Events persistence: Postgres (dev/docker profiles) + Flyway

## Status
Accepted

## Context
Week 2 requires persisting Events in PostgreSQL with schema managed via Flyway.

We want a production-minded setup:
- Runtime uses PostgreSQL (via `dev`/`docker` profiles).
- Schema is versioned and reproducible via Flyway.
- Unit tests stay fast and isolated (in-memory DB for tests only).
- The base profile is not intended to run without an explicit datasource (runtime is profile-driven).

## Decision
- Keep the current Ports/Adapters structure:
  - `domain.event.EventRepository` as the domain port.
  - `infrastructure.persistence.event.*` as the JPA adapter (`EventJpaEntity`, `EventJpaRepository`, `EventRepositoryAdapter`).
- Use PostgreSQL + Flyway for runtime via profiles:
  - `application-dev.yaml` configures Postgres + Flyway + `spring.jpa.hibernate.ddl-auto=validate`.
  - `application-docker.yaml` configures Postgres + Flyway for Compose/container runs.
  - `application.yaml` defines global JPA/Flyway defaults but no datasource; runtime datasource is provided by profiles.
- Use dedicated configs for tests:
  - `application-test.yml` uses H2 for unit tests only.
  - `application-it.yml` runs against PostgreSQL Testcontainers (datasource overridden via `@DynamicPropertySource`).
- Flyway is the **schema source of truth** (versioned migrations).
- Hibernate DDL is set to `validate` to detect drift early (no schema management at runtime).
- Flyway `clean` is disabled (`spring.flyway.clean-disabled=true`) to avoid accidental wipes.

## Consequences
- Pros:
  - Runtime is production-like: real Postgres + versioned schema + validation.
  - Drift between entities and migrations is caught early via `ddl-auto=validate`.
  - Unit tests do not require Docker/DB and remain fast.
- Cons:
  - Developers must run Postgres (Compose) and start the API with `--spring.profiles.active=dev` for local runtime.
  - Full Docker runtime uses the `docker` profile (Compose/container).
  - Integration tests require Docker (Testcontainers).