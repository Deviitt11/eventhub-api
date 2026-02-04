# PRD

## Project Title
EventHub API (Spring Boot)

## Vision
Build a production-minded backend API that demonstrates clean architecture, robust validation/error handling, and reliable automated testing/CI.

## Weekly Deliverable (Week 1)
A minimal "Events" vertical slice (CRUD) with:
- Validation
- Consistent error handling
- OpenAPI published (Swagger UI)
- CI running unit + integration tests

## Goals
- Backend-first (70/30): prioritize domain, persistence, testing, and API contracts.
- Ship vertical slices weekly with increasing depth.
- Maintain a clean structure that scales to new modules.

## Non-Goals (for Week 1)
- Authentication/authorization
- Complex filtering/search
- Event sourcing or microservices
- Observability stack (metrics/tracing) beyond basic logging

## Success Criteria (Week 1)
- Events endpoints work locally (HTTP 200/201/204 + 400/404).
- Swagger UI shows Events endpoints and schemas.
- CI runs unit tests on PR.
- Integration tests exist (Testcontainers) for the Events slice.