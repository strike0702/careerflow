 # CareerFlow Local Development Guide

This document outlines configurations, properties, and run instructions for building, running, and testing CareerFlow microservices locally.

---

## 1. Directory Structure

```
CareerFlow/
├── backend/
│   ├── shared-common/          # Correlation IDs, logging, shared exception handling
│   ├── shared-events/          # Versioned JSON domain event contracts
│   ├── api-gateway/            # Port 9000
│   ├── user-service/           # Port 8081
│   ├── application-service/    # Port 8083
│   ├── resume-service/         # Port 8082
│   ├── interview-service/      # Port 8084
│   └── notification-service/   # Port 8085
├── infrastructure/             # Docker Compose (PostgreSQL + Keycloak + Kafka)
├── frontend/                   # React SPA (Vite, port 5173)
├── bruno/                      # API test collection
└── docs/
```

---

## 2. Spring Profiles

| Profile | Purpose | Activated by |
|---------|---------|--------------|
| `dev` | Local development (default) | `SPRING_PROFILES_ACTIVE=dev` or default in `application.yml` |
| `prod` | Production-style JSON logging | `SPRING_PROFILES_ACTIVE=prod` |
| `test` | Integration tests (H2 in-memory) | `@ActiveProfiles("test")` in test classes |

Development-only settings (`show-sql`, Hibernate SQL debug logging) are confined to `application-dev.yml`.

---

## 3. Environment Variables

All services support externalized configuration via environment variables:

| Variable | Used by | Default |
|----------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | All | `dev` |
| `SERVER_PORT` | All | Service-specific (9000, 8081–8085) |
| `DATABASE_URL` | Business services | `jdbc:postgresql://localhost:5432/careerflow_*` |
| `RESUME_SERVICE_URL` | application-service | `http://localhost:8082` |
| `APPLICATION_SERVICE_URL` | interview-service | `http://localhost:8083` |
| `DATABASE_USERNAME` | user-service, application-service | `postgres` |
| `DATABASE_PASSWORD` | user-service, application-service | `password` |
| `KEYCLOAK_JWK_SET_URI` | user-service, application-service | Keycloak JWKS on `localhost:8080` |
| `USER_SERVICE_URI` | api-gateway | `http://localhost:8081` |
| `APPLICATION_SERVICE_URI` | api-gateway | `http://localhost:8083` |
| `RESUME_SERVICE_URI` | api-gateway | `http://localhost:8082` |
| `INTERVIEW_SERVICE_URI` | api-gateway | `http://localhost:8084` |
| `KAFKA_BOOTSTRAP_SERVERS` | application-service, notification-service | `localhost:9092` |

Example — run user-service against a custom database:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/careerflow_user \
DATABASE_PASSWORD=secret \
./gradlew :user-service:bootRun
```

---

## 4. Correlation IDs

Send an optional `X-Request-ID` header on any request. If omitted:

- The **gateway** generates a UUID and propagates it downstream.
- **Business services** generate one for direct calls (bypassing the gateway).

The same ID appears in response headers, access logs, and error payloads (`requestId` field).

---

## 5. Actuator Endpoints

Unauthenticated (permitted in security config for business services):

| Endpoint | Purpose |
|----------|---------|
| `GET /actuator/health/liveness` | Process is alive |
| `GET /actuator/health/readiness` | Ready to serve (includes DB check on services with datasource) |
| `GET /actuator/prometheus` | Prometheus scrape metrics |
| `GET /actuator/info` | Application info |

Bruno requests are available under **Actuator** in the collection.

---

## 6. Build & Run Workflow

```bash
# 1. Start infrastructure
cd infrastructure && docker compose up -d

# 2. Build all modules
cd backend && ./gradlew clean build

# 3. Run services (separate terminals)
./gradlew :api-gateway:bootRun
./gradlew :user-service:bootRun
./gradlew :application-service:bootRun
./gradlew :resume-service:bootRun
./gradlew :interview-service:bootRun
./gradlew :notification-service:bootRun
```

Run tests:

```bash
./gradlew :application-service:test :user-service:test :resume-service:test :interview-service:test :notification-service:test
```

---

## 8. Kafka (Phase 5)

Kafka starts with infrastructure:

```bash
cd infrastructure && docker compose up -d
```

| Component | URL |
|-----------|-----|
| Kafka broker | `localhost:9092` |
| Kafka UI | `http://localhost:8086` |

**Existing Postgres volume:** if `careerflow_notification` DB missing, create manually:

```bash
docker exec -it careerflow-postgres psql -U postgres -c "CREATE DATABASE careerflow_notification;"
```

**Verify event flow:** create application via API, check notification-service logs for `action=would_notify`. Kafka UI shows messages on `careerflow.application.events`.

---

## 7. User Service Flyway Baseline

If your local `careerflow_user` database already has tables created by Hibernate before Phase 3, Flyway uses `baseline-on-migrate: true` to adopt the existing schema without re-running `V1`/`V2` on populated databases. Fresh environments apply migrations normally.

---

## 9. Frontend Development

The React SPA lives in `frontend/` and communicates **only** with the API Gateway.

| Item | Value |
|------|-------|
| Dev server | `http://localhost:5173` |
| Vite proxy | `/api` → `http://localhost:9000` |
| Keycloak client | `careerflow-api-gateway` (Authorization Code + PKCE) |
| Demo user | `candidate@careerflow.com` / `password` |

Unauthenticated visitors land on a CareerFlow welcome screen (`AuthLanding`) with **Sign In** and **Create Account** buttons — both redirect to Keycloak's hosted pages (`keycloak.login()` / `keycloak.register()`); the SPA has no custom auth form. New accounts (via registration or Google) automatically receive the `CANDIDATE` role.

After first login, if the candidate profile has no target role or no skill yet, the dashboard shows a dismissible **Complete your profile** banner with a link to `/profile`. The rest of the app stays accessible. See [architecture.md](./architecture.md#frontend-profile-completion-nudge) and [ADR-008](./decisions/ADR-008-self-service-registration-and-onboarding.md).

```bash
cd frontend
cp .env.example .env.development
npm install
npm run dev
```

Environment variables (`frontend/.env.development`):

| Variable | Default | Purpose |
|----------|---------|---------|
| `VITE_KEYCLOAK_URL` | `http://localhost:8080` | Keycloak base URL |
| `VITE_KEYCLOAK_REALM` | `careerflow-realm` | Realm name |
| `VITE_KEYCLOAK_CLIENT_ID` | `careerflow-api-gateway` | OAuth client |
| `VITE_API_BASE_URL` | *(empty)* | Empty uses Vite `/api` proxy; set to gateway URL for direct calls |

**CORS:** The API Gateway allows `http://localhost:5173`. Keycloak redirect URIs include `http://localhost:5173/*`.

**Full stack startup order:**

1. `cd infrastructure && docker compose up -d`
2. Start backend services (gateway, user-service, application-service, notification-service)
3. `cd frontend && npm run dev`
4. Open `http://localhost:5173` and sign in via Keycloak

---

## 10. Troubleshooting

### Token / JWKS errors

Ensure Keycloak is reachable at `http://localhost:8080` and `KEYCLOAK_JWK_SET_URI` matches your realm.

### Schema validation failures

If Hibernate `ddl-auto: validate` fails after pulling Phase 3 changes, run Flyway migrations against a clean database or align your local schema with `backend/user-service/src/main/resources/db/migration/`.
