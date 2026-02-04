# ADR-0001: Observability baseline

We standardize on Spring Boot Actuator for health checks and a request-scoped Correlation ID header.
All requests accept `X-Correlation-Id`; when absent, the API generates one and echoes it back.
This provides minimal, production-minded traceability across logs and clients.
Actuator endpoints remain minimal and non-sensitive by default.