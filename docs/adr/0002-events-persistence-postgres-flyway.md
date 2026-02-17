# ADR 0002 — Events persistence: Postgres (dev profile) + Flyway

## Status
Accepted

## Context
Week 2 requires persisting Events in PostgreSQL with schema managed via Flyway, while keeping a safe default developer experience (the app can boot without requiring a local DB by default).

## Decision
- Keep the current Ports/Adapters structure:
  - `domain.event.EventRepository` as the domain port.
  - `infrastructure.persistence.event.*` as the JPA adapter (`EventJpaEntity`, `EventJpaRepository`, `EventRepositoryAdapter`).
- Use PostgreSQL + Flyway under `dev` profile:
  - `application.yaml` remains a safe default.
  - `application-dev.yaml` configures Postgres + Flyway + `spring.jpa.hibernate.ddl-auto=validate`.
- Flyway is the **schema source of truth** (versioned migrations).
- Hibernate DDL is set to `validate` to detect drift early (no schema management at runtime).
- Flyway `clean` is disabled (`spring.flyway.clean-disabled=true`) to avoid accidental wipes.

## Consequences
- Pros:
  - No DB required to boot the app by default.
  - Dev profile behaves production-like: real Postgres + versioned schema + validation.
  - Drift between entities and migrations is caught early via `ddl-auto=validate`.
- Cons:
  - Developers must run the DB (Compose) and start the API with `--spring.profiles.active=dev` when they want Postgres locally.
