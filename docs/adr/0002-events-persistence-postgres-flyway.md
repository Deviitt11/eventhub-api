# ADR 0002 — Events persistence: Postgres (dev profile) + Flyway

## Status
Accepted

## Context
Week 2 requires persisting Events in PostgreSQL with schema managed via Flyway, while keeping a safe default developer experience (app should boot without requiring a local DB).

## Decision
- Keep the current Ports/Adapters structure:
    - `domain.event.EventRepository` as the domain port.
    - `infrastructure.persistence.event.*` as the JPA adapter (`EventJpaEntity`, `EventJpaRepository`, `EventRepositoryAdapter`).
- Use PostgreSQL + Flyway under `dev` profile:
    - `application.yaml` remains a safe default.
    - `application-dev.yaml` configures Postgres + Flyway + `spring.jpa.hibernate.ddl-auto=validate`.

## Consequences
- Pros:
    - No DB required to boot the app by default.
    - Dev profile behaves production-like: real Postgres + versioned schema + validation.
- Cons:
    - Developers must remember to use `--spring.profiles.active=dev` when they want Postgres locally.
