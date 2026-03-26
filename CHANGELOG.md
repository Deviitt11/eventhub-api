# Changelog

## Week 3

### Wednesday — DevOps / CI-CD / Docker

#### Shipped
- Added Docker image build validation to CI
- Added a minimal docker-compose startup smoke check
- Removed unsafe hardcoded password handling from the CI workflow
- Improved compose credential handling for safer reproducibility
- Aligned README and runbook with the actual supported Docker/Compose/CI flow

#### Impact
- Earlier detection of Docker/runtime regressions
- Better local/CI reproducibility
- Safer workflow configuration handling

#### No change
- No API contract changes
- No persistence changes
- No Flyway migrations

### Thursday — Backend Advanced / Events Query Hardening

#### Shipped
- Added explicit default sorting to `GET /api/v1/events` by `startsAt`
- Added optional `startsAt`-based filters to the events list endpoint
- Hardened invalid query parameter handling for `GET /api/v1/events`
- Updated API documentation to reflect the supported list query parameters
- Added/updated tests to cover list ordering, filtering, and invalid query scenarios

#### Impact
- More predictable and interview-grade list endpoint behavior
- Better support for real client use cases when querying events by time
- Stronger and more consistent error handling for malformed list queries
- Improved API discoverability through updated Swagger/OpenAPI docs

#### No change
- No new endpoints introduced
- No persistence schema changes
- No Flyway migrations
- No large refactor across layers