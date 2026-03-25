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