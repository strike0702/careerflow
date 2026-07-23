# CareerFlow Project Status

Last updated: July 2026 (post Phase 6)

---

## Project Vision

CareerFlow is an **Identity-Aware Job Application Tracker** built to demonstrate production-style backend engineering: OAuth2/OIDC authentication, microservice boundaries, database-per-service isolation, and secure multi-tenant data access.

The platform helps software engineers track job applications, referrals, offers, and career activity in one place—without storing identity data redundantly across services.

---

## High-Level Architecture

```mermaid
graph LR
    Client[Client / Bruno] -->|Bearer JWT| Gateway[API Gateway :9000]
    Gateway --> UserService[User Service :8081]
    Gateway --> AppService[Application Service :8083]
    Gateway --> ResumeService[Resume Service :8082]
    Gateway --> InterviewService[Interview Service :8084]

    Keycloak[Keycloak :8080] -->|Issues JWT| Client
    UserService --> UserDB[(careerflow_user)]
    AppService --> AppDB[(careerflow_application)]
    ResumeService --> ResumeDB[(careerflow_resume)]
    InterviewService --> InterviewDB[(careerflow_interview)]
    Keycloak --> KCDB[(keycloak_db)]
```

Clients authenticate with Keycloak, call the API Gateway with a JWT, and each downstream service validates the token locally before handling the request.

---

## Implementation Status Summary

| Area | Status |
|------|--------|
| Infrastructure (Docker Compose, PostgreSQL, Keycloak, Kafka) | **Completed** |
| API Gateway (path routing) | **Completed** |
| User Service (profile + JWT sync) | **Completed** (with known gaps — see limitations) |
| Application Service (applications, offers, activities, dashboard) | **Completed** |
| Resume Service | **Completed** |
| Interview Service | **Completed** |
| Shared observability module | **Completed** |
| Frontend (React SPA) | **Completed** |
| Notification Service (event consumer stub) | **Completed** |
| CI/CD | **Planned** |
| Event-driven architecture (Kafka) | **Completed** |

---

## Completed Phases

### Phase 1 — Identity & User Service ✅

- Keycloak realm import (`careerflow-realm`)
- OAuth2 resource server configuration in user-service
- JWT validation via Keycloak JWKS endpoint
- Lazy user sync from JWT claims on first authenticated request
- Candidate profile CRUD scoped to authenticated user
- API Gateway route for `/api/v1/users/**`

### Phase 2 — Application Service ✅

- Gradle module with Flyway migrations (`V1`–`V3`)
- Feature-based package structure (`application`, `offer`, `activity`, `shared`)
- Full application lifecycle APIs (create, list, detail, status update)
- Offer upsert (`PUT /offer`)
- Activity timeline and automatic audit logging
- Dashboard with DB aggregation queries
- Ownership enforcement via JWT `sub` (404 on cross-user access)
- Integration tests (repository, service, security)
- Bruno API collection for application-service endpoints

### Phase 3 — Observability & Production Readiness ✅

- `shared-common` module: correlation IDs, access logging, shared exception handling
- `X-Request-ID` propagation through gateway and business services
- Profile-aware logging (plain text in `dev`, JSON in `prod`)
- RFC 7807 `ProblemDetail` errors with `requestId` across all services
- Liveness/readiness probes and Prometheus metrics
- Externalized configuration via Spring profiles and environment variables
- User Service Flyway migrations (`V1`–`V2`); `ddl-auto: validate`
- User Service tests (7 passing)
- Bruno collection updates (readiness, prometheus, inherited auth)
- ADR-007: Correlation ID propagation

**Deferred:** Shared security module extraction and `@PreAuthorize` (intentionally postponed).

### Phase 4 — Frontend MVP ✅

- React + TypeScript + Vite SPA in `frontend/`
- Keycloak Authorization Code + PKCE via `keycloak-js`
- All API calls through API Gateway (`:9000`) with Axios interceptors
- Dashboard with metrics, status chart, and activity feed
- Applications list (filters, pagination), create, detail, status update, offer management
- Candidate profile view and edit
- Gateway CORS for `http://localhost:5173`; Vite `/api` dev proxy
- TanStack Query, React Hook Form + Zod, shadcn/ui, Recharts, next-themes

### Phase 4.1 — Self-Service Registration & Onboarding ✅

- Keycloak self-registration enabled (`registrationAllowed`, email-as-username, password policy, self-service password reset)
- Google Identity Provider configured via Keycloak brokering (placeholder credentials — see [setup-guide.md](./setup-guide.md))
- New users auto-assigned `CANDIDATE` via a Keycloak default group (`/candidates`), regardless of registration path
- Frontend landing screen (`AuthLanding`) with **Sign In** / **Create Account**, replacing the previous unconditional auto-redirect to Keycloak login
- Frontend profile completion banner on the dashboard (`ProfileCompletionBanner`): dismissible nudge when profile lacks target role or skill; links to `/profile` without blocking other routes
- No backend or database changes — lazy `User`/`CandidateProfile` sync (`UserService.getOrSyncUser`) is unchanged; see [ADR-008](./decisions/ADR-008-self-service-registration-and-onboarding.md)

### Phase 5 — Event-Driven Architecture ✅

- Apache Kafka (KRaft) + Kafka UI in Docker Compose
- `shared-events` module: versioned JSON envelope + payload contracts
- Application Service transactional outbox (`V4` migration) + scheduled poller
- Domain events: `ApplicationCreated`, `ApplicationStatusChanged`, `OfferAdded`, `OfferUpdated`
- Notification Service (`:8085`) consumes events with idempotent `processed_events` table
- Consumer retries + DLT topic; correlation ID propagation via Kafka headers
- ADR-009: Kafka + outbox + JSON envelope

**Deferred:** Avro/Schema Registry, SMTP/email, in-app notification inbox API.

### Phase 6 — Resume & Interview Management ✅

- Resume Service (`:8082`): CRUD for resume versions, mock storage URLs, parse status metadata, transactional outbox
- Interview Service (`:8084`): interview rounds, status/outcome, retrospectives, dashboard stats endpoint
- OpenFeign sync validation: Interview→Application, Application→Resume (ADR-011)
- Optional `resume_id` on applications (Flyway V5)
- Domain events: `ResumeUploaded`, `ResumeDeleted`, `InterviewScheduled`, `InterviewCompleted`
- Frontend: Resumes page, Interviews tab on application detail, dashboard merges interview stats
- ADR-010, ADR-011

---

## Service Responsibilities

| Service | Port | Database | Owns |
|---------|------|----------|------|
| **API Gateway** | 9000 | None | Routing only; forwards JWT unchanged |
| **User Service** | 8081 | `careerflow_user` | Users, candidate profiles |
| **Application Service** | 8083 | `careerflow_application` | Applications, offers, activities, outbox events |
| **Notification Service** | 8085 | `careerflow_notification` | Event consumption, idempotency, stub notifications |
| **Resume Service** | 8082 | `careerflow_resume` | Resume versions, storage metadata, parse status |
| **Interview Service** | 8084 | `careerflow_interview` | Interview rounds, retrospectives |

**Ownership rule:** Application Service stores only `userId` (JWT subject). It does not store email, username, or name.

---

## Current API Summary

All public APIs use the prefix `/api/v1`. Gateway entry point: `http://localhost:9000`.

### User Service — Implemented

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/users/me` | Current user (synced from JWT) |
| GET | `/api/v1/users/me/profile` | Candidate profile |
| PUT | `/api/v1/users/me/profile` | Update candidate profile |

### Application Service — Implemented

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/applications` | Create application |
| GET | `/api/v1/applications` | List applications (paginated, filterable) |
| GET | `/api/v1/applications/{id}` | Application detail + offer + recent activities |
| PATCH | `/api/v1/applications/{id}/status` | Update status + activity log |
| PUT | `/api/v1/applications/{id}/offer` | Create or replace offer |
| GET | `/api/v1/applications/activities` | User activity timeline |
| GET | `/api/v1/applications/dashboard` | Aggregated metrics |

See [api-overview.md](./api-overview.md) for request/response schemas.

### Resume Service — Implemented

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/resumes` | Create resume version |
| GET | `/api/v1/resumes` | List user's resumes |
| GET | `/api/v1/resumes/{id}` | Get resume |
| PUT | `/api/v1/resumes/{id}` | Update metadata |
| PUT | `/api/v1/resumes/{id}/primary` | Set as primary |
| DELETE | `/api/v1/resumes/{id}` | Delete resume |

### Interview Service — Implemented

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/interviews` | Schedule interview round |
| GET | `/api/v1/interviews` | List/filter interviews |
| GET | `/api/v1/interviews/stats` | Dashboard aggregation |
| GET | `/api/v1/interviews/{id}` | Get interview |
| PUT | `/api/v1/interviews/{id}` | Update details |
| PATCH | `/api/v1/interviews/{id}/status` | Update status |
| PATCH | `/api/v1/interviews/{id}/outcome` | Update outcome |
| PUT | `/api/v1/interviews/{id}/retrospective` | Upsert retrospective |
| GET | `/api/v1/interviews/{id}/retrospective` | Get retrospective |
| DELETE | `/api/v1/interviews/{id}` | Delete interview |

Application create accepts optional `resumeId` (validated via Feign).

---

## Infrastructure Components

| Component | Location | Notes |
|-----------|----------|-------|
| Docker Compose | `infrastructure/docker-compose.yml` | PostgreSQL 16 + Keycloak 24.0.5 + Kafka KRaft + Kafka UI |
| DB init script | `infrastructure/postgres/init.sql` | Creates 6 logical databases |
| Keycloak realm | `infrastructure/keycloak/realm-export.json` | Realm, roles, demo users, OAuth client |
| Gradle monorepo | `backend/` | Spring Boot 3.3, Java 21 |
| Bruno collection | `bruno/` | OpenCollection YAML requests |

**Databases created at startup:**

- `careerflow_user`
- `careerflow_resume`
- `careerflow_application`
- `careerflow_interview`
- `careerflow_notification`
- `keycloak_db`

---

## Security Architecture

1. **Authentication:** Keycloak issues JWT access tokens (OAuth2/OIDC).
2. **Gateway:** Stateless path-based routing; does **not** validate JWTs.
3. **Services:** Each service is an OAuth2 resource server validating JWTs via Keycloak JWKS.
4. **Authorization:** All endpoints require authentication except actuator health/info.
5. **Ownership:** Business logic scopes queries to `jwt.getSubject()`. Cross-user resource access returns **404 Not Found** (application-service).
6. **Roles:** Keycloak `realm_access.roles` mapped to Spring authorities (`ROLE_CANDIDATE`, etc.). Method-level `@PreAuthorize` is enabled but **not yet applied** to controllers.

Demo credentials (Keycloak):

- Candidate: `candidate@careerflow.com` / `password`
- Admin: `admin@careerflow.com` / `password`
- OAuth client: `careerflow-api-gateway`

**Self-registration:** enabled in Keycloak. New users (via the hosted registration page or Google) are placed in the `/candidates` default group → realm role `CANDIDATE`. `ADMIN` is never assigned by default. Google login requires real OAuth credentials to be configured (see [setup-guide.md](./setup-guide.md)); the committed realm export ships with placeholders only.

---

## Database Ownership

| Database | Schema management | Tables (current) |
|----------|-------------------|-------------------|
| `careerflow_user` | Flyway + Hibernate `validate` | `users`, `candidate_profiles`, `candidate_skills` |
| `careerflow_application` | Flyway + Hibernate `validate` | `applications`, `offers`, `activities` |
| `careerflow_resume` | Flyway + Hibernate `validate` | `resumes`, `outbox_events` |
| `careerflow_interview` | Flyway + Hibernate `validate` | `interviews`, `interview_retrospectives`, `outbox_events` |

Application Service Flyway migrations:

- `V1__create_applications.sql`
- `V2__create_offers.sql`
- `V3__create_activities.sql`
- `V4__create_outbox_events.sql`
- `V5__add_resume_id_to_applications.sql`

User Service Flyway migrations:

- `V1__create_users.sql`
- `V2__create_candidate_profiles.sql`

Referral data is embedded in the `applications` table (not a separate `referrals` table).

---

## Testing Status

| Module | Tests | Status |
|--------|-------|--------|
| application-service | 8 tests | **Passing** |
| resume-service | 2 tests (security) | **Passing** |
| interview-service | 2 tests (security) | **Passing** |
| user-service | 7 tests (repository, service, security) | **Passing** (H2 in-memory, `test` profile) |
| api-gateway | None | **Not implemented** |

Critical security tests: cross-user application access returns 404; unauthenticated user-service requests return 401.

Run tests:

```bash
cd backend && ./gradlew :application-service:test :user-service:test :resume-service:test :interview-service:test
```

---

## Technical Highlights

- **Java 21** and **Spring Boot 3.3** multi-module Gradle project
- **Database-per-service** with logical isolation on a shared PostgreSQL instance
- **Local JWT validation** (JWKS) — no per-request introspection calls to Keycloak
- **Flyway** versioned migrations in application-service and user-service
- **Optimistic locking** (`@Version`) on applications
- **Unidirectional JPA relationships** — offers and activities reference `applicationId` only
- **Dashboard aggregations** via repository count/GROUP BY queries (not in-memory)
- **Correlation IDs** (`X-Request-ID`) and structured logging via `shared-common`
- **Global exception handler** with RFC 7807 `ProblemDetail` responses (`shared-common`)
- **Prometheus metrics** and liveness/readiness probes
- **Transactional outbox** + Kafka domain events (`shared-events`, ADR-009)
- **Notification Service** stub consumer with idempotent `processed_events`

---

## Current Limitations

1. **No `@PreAuthorize` role checks** on controllers despite Keycloak roles being mapped (deferred).
3. **Gateway does not validate JWTs** — invalid tokens fail at downstream services.
4. **No CI/CD pipeline**.
5. **No real email/SMS notifications** — stub logging only; SMTP deferred.
6. **No distributed tracing** (OpenTelemetry) — correlation IDs provide log-level tracing only.

---

## Known Technical Debt

See [technical-debt.md](./technical-debt.md) and [decisions/](./decisions/) for detailed tracking.

High-priority items for Phase 6+:

- `@PreAuthorize` role enforcement (when additional services justify shared security module)
- Avro/Schema Registry for event contracts
- Real email delivery (SMTP)

---

## Planned Roadmap (Phase 5+)

See [roadmap.md](./roadmap.md) for the full plan.

| Phase | Focus |
|-------|-------|
| **Phase 7** | Deployment (CI/CD, cloud, monitoring) |
