# Runbook (host + compose)

Goal: verify end-to-end (Swagger/curl) and prove Flyway-managed persistence.
DoD: step-by-step commands documented + DB verification queries.

## A) Daily (API on host + DB via Docker Compose) — recommended

### 0) Clean slate (only when you want a fresh DB)
```bash
docker compose down -v
```

### 1) Start DB
```bash
docker compose up -d db
```

### 2) Verify DB
```bash
docker compose ps
docker compose logs -f db
```

### 3) Run API locally (dev profile)
```bash
./gradlew bootRun --args="--spring.profiles.active=dev"
```

### 4) Smoke (Actuator)
```bash
curl -fsS http://localhost:8080/actuator/health
```

### 5) CRUD smoke (curl) — create → list → get → update → delete
```bash
# Create
curl -s -X POST http://localhost:8080/api/v1/events \
	-H "Content-Type: application/json" \
	-H "X-Correlation-Id: demo-123" \
	-d '{
		"title": "My Event",
		"startsAt": "2030-01-01T10:00:00Z",
		"endsAt": "2030-01-01T11:00:00Z"
	}'

# Copy the "id" from the JSON response and set it here
export EVENT_ID="<id>"

# List
curl -fsS -H "X-Correlation-Id: demo-123" http://localhost:8080/api/v1/events

# Get by id
curl -fsS -H "X-Correlation-Id: demo-123" http://localhost:8080/api/v1/events/$EVENT_ID

# Update
curl -s -X PUT http://localhost:8080/api/v1/events/$EVENT_ID \
	-H "Content-Type: application/json" \
	-H "X-Correlation-Id: demo-123" \
	-d '{
		"title": "My Event Updated",
		"startsAt": "2030-01-01T10:00:00Z",
		"endsAt": "2030-01-01T12:00:00Z"
	}'

# Delete
curl -i -X DELETE -H "X-Correlation-Id: demo-123" http://localhost:8080/api/v1/events/$EVENT_ID
```

---

## B) All Docker (API + DB via Compose)

The _api_ service is under _profiles: ["full"]_.

```bash
docker compose --profile full up -d --build
docker compose logs -f api
```

Smoke:

```bash
curl -fsS http://localhost:8080/actuator/health
```

---

## C) Reset clean (wipe DB data)
```bash
docker compose down -v
```

---

## D) Verify migrations / tables (Flyway)

### List tables
```bash
docker compose exec db psql -U eventhub -d eventhub -c "\dt"
```

### Describe events table
```bash
docker compose exec db psql -U eventhub -d eventhub -c "\d events"
```

### Flyway history (prove Flyway applied migrations)
```bash
docker compose exec db psql -U eventhub -d eventhub -c "select installed_rank, version, description, type, script, checksum, installed_on, success from flyway_schema_history order by installed_rank;"
```

Expected:

- table _flyway_schema_history_ exists
- at least one row with _version = '1'_ (or similar) for `V1__create_events_table.sql`
- _success = true_

### Check inserted rows (top 20)

```bash
docker compose exec db psql -U eventhub -d eventhub -c "select id, title, starts_at, ends_at, created_at from events order by created_at desc limit 20;"
```

---

## E) Tests
```bash
./gradlew test
./gradlew integrationTest
./gradlew check
```

If Gradle says tasks are UP-TO-DATE and you want a fresh run:
```bash
./gradlew integrationTest --rerun-tasks
```

If daemons get weird (e.g., JVM mismatch):
```bash
./gradlew --stop
```

---

## Notes

### Why _docker logs -f eventhub-api_ failed

- That command needs the exact container name.
- Compose generates names like _eventhub-api-api-1_.
- Prefer: _docker compose logs -f api_ (service-based, stable).
