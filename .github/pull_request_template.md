# Summary
<!-- 2–5 lines: what and why. -->

# Scope
- **Area:** <!-- e.g., Events / Auth / CI -->
- **Type:** <!-- feat / fix / refactor / docs / chore -->
- **Related issue(s):** <!-- optional: #123 -->

# Changes
## Architecture / Design
- <!-- bullet points -->

## API Contract (if applicable)
- **Endpoint(s):**
	- `METHOD /path` → `status`
- **Behavior changes:**
	- <!-- bullet points -->

## Validation & Error Handling (if applicable)
- **400** when:
	- <!-- bullet points -->
- **404** when:
	- <!-- bullet points -->
- **409** when:
	- <!-- bullet points (only if real conflicts) -->

## Persistence / Data (if applicable)
- <!-- migrations, constraints, mapping notes -->

# Root Cause (if bugfix)
- **Observed:** <!-- what failed -->
- **Cause:** <!-- why it failed -->
- **Fix:** <!-- smallest production-minded fix -->

# How to Test
## Local
1. `./gradlew bootRun`
2. Open: `http://localhost:8080/swagger-ui/index.html`
3. Steps:
	- <!-- bullet list -->

## Automated Tests
- [ ] Unit:
	- <!-- list -->
- [ ] Integration:
	- <!-- list -->

# Screenshots (attach)
- [ ] <!-- POST 201 -->
- [ ] <!-- POST 400 (validation/domain) -->
- [ ] <!-- GET 200 -->
- [ ] <!-- PUT 200 -->
- [ ] <!-- DELETE 204 -->
- [ ] <!-- GET 404 after delete -->

# Definition of Done
- [ ] Endpoints respond with correct statuses (200/201/204 + 400/404; 409 only for real conflicts)
- [ ] Validation messages are clear and consistent
- [ ] Error payload format is consistent across the API
- [ ] Swagger/OpenAPI is accessible locally
- [ ] Clean layers: domain/application/infrastructure/presentation remain separated

# Notes / Follow-ups
- <!-- tech debt, next steps, risks -->
