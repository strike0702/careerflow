 # CareerFlow Local Development Guide

This document outlines configurations, properties, and run instructions for building, running, and testing CareerFlow microservices locally.

---

## 1. Directory Structure

```
CareerFlow/
├── backend/
│   ├── shared-common/          # Correlation IDs, logging, shared exception handling
│   ├── api-gateway/            # Port 9000
│   ├── user-service/           # Port 8081
│   └── application-service/    # Port 8083
├── infrastructure/             # Docker Compose (PostgreSQL + Keycloak)
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
| `SERVER_PORT` | All | Service-specific (9000, 8081, 8083) |
| `DATABASE_URL` | user-service, application-service | `jdbc:postgresql://localhost:5432/careerflow_*` |
| `DATABASE_USERNAME` | user-service, application-service | `postgres` |
| `DATABASE_PASSWORD` | user-service, application-service | `password` |
| `KEYCLOAK_JWK_SET_URI` | user-service, application-service | Keycloak JWKS on `localhost:8080` |
| `USER_SERVICE_URI` | api-gateway | `http://localhost:8081` |
| `APPLICATION_SERVICE_URI` | api-gateway | `http://localhost:8083` |
| `RESUME_SERVICE_URI` | api-gateway | `http://localhost:8082` |
| `INTERVIEW_SERVICE_URI` | api-gateway | `http://localhost:8084` |

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
```

Run tests:

```bash
./gradlew :application-service:test :user-service:test
```

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
2. Start backend services (gateway, user-service, application-service)
3. `cd frontend && npm run dev`
4. Open `http://localhost:5173` and sign in via Keycloak

---

## 10. Troubleshooting

### Token / JWKS errors

Ensure Keycloak is reachable at `http://localhost:8080` and `KEYCLOAK_JWK_SET_URI` matches your realm.

### Schema validation failures

If Hibernate `ddl-auto: validate` fails after pulling Phase 3 changes, run Flyway migrations against a clean database or align your local schema with `backend/user-service/src/main/resources/db/migration/`.
