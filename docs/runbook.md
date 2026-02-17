# Runbook

Commands to run, verify, and troubleshoot EventHub locally (clean and repeatable).

---

## A) Daily (recommended): DB in Docker + API on host

### 0) Clean slate (only when you want a fresh DB)
```bash
docker compose down -v
```

### 1) Start DB
```bash
docker compose up -d db
```

### 2) Verify
```bash
docker compose ps
```

3) Run API locally (dev profile)
```bash
./gradlew bootRun --args="--spring.profiles.active=dev"
```

4) Tail DB logs
```bash
docker compose logs -f db
```

Smoke
```bash
curl -fsS http://localhost:8080/actuator/health
```

---

## B) Full Docker (API + DB)
```bash
docker compose --profile full up -d --build
docker compose logs -f api
```

---

## C) Reset clean (wipe data)
```bash
docker compose down -v
docker compose up -d db
```

---

## D) Verify migrations / tables
```bash
docker compose exec db psql -U eventhub -d eventhub -c "\dt"
docker compose exec db psql -U eventhub -d eventhub -c "\d events"
docker compose exec db psql -U eventhub -d eventhub -c "select id, title, starts_at, ends_at, created_at from events order by created_at desc limit 20;"
```

If you want the _bootJar_ to exist for inspection:
```bash
./gradlew clean bootJar
```

---

## E) Tests
```bash
./gradlew test
./gradlew integrationTest
./gradlew check
```

---

### Why _docker logs -f eventhub-api_ failed

- That command needs the exact container name.
- Compose generates names like eventhub-api-api-1.
- Prefer: docker compose logs -f api (service-based, stable).
