# Architecture

## Overview
`eventhub-api` is a Spring Boot REST API built with a backend-first roadmap and production-minded defaults.

Key principles:
- **Clean boundaries:** domain/application are not coupled to web or JPA specifics.
- **Consistency:** stable API contracts (including error payloads).
- **Testability:** fast unit tests + integration tests against real PostgreSQL.

## High-level Layers
The project follows a layered approach that keeps dependencies directional:

- **Domain**
	- Domain model, rules, ports (interfaces), domain exceptions.
- **Application**
	- Use cases orchestrating domain behavior and coordinating ports.
- **Infrastructure**
	- Adapters for persistence (JPA), configuration, and external integrations.
- **Presentation**
	- REST controllers, request/response DTOs, exception handling.

## Package Structure (suggested)
- `dev.codedbydavid.eventhub.domain`
- `dev.codedbydavid.eventhub.application`
- `dev.codedbydavid.eventhub.infrastructure`
- `dev.codedbydavid.eventhub.presentation`

## API Conventions
- **Base path:** `/api/v1`
- **Versioning:** all endpoints are versioned under the base path (v1).

## Error Handling
A single error contract is used for all endpoints:

- Fields: `code`, `message`, `details`, `timestamp`, `path`
- Common mappings:
	- **400** validation errors (request validation or domain validation)
	- **404** resource not found
	- **409** conflicts (e.g., unique constraints) when applicable

## Persistence Strategy
- **Runtime DB target:** PostgreSQL
- **Unit tests:** H2 in-memory using a dedicated test profile/config
- **Integration tests:** PostgreSQL via Testcontainers

## Testing Strategy
- **Unit tests**
	- Domain rules and application use cases
	- Fast, no Spring context required
- **Integration tests**
	- Controller + repository + database (Testcontainers)
	- Validate critical flows and the standardized error contract

## Evolution (Roadmap-friendly)
The architecture is designed to evolve without rewrites:
- Auth (JWT/OAuth) can be added at the edges
- Observability can expand from correlation ID + Actuator into structured logs/traces
- Event-driven patterns (e.g., outbox) can be introduced within infrastructure/application boundaries