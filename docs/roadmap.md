# CareerFlow Roadmap

Realistic phased plan based on current implementation status. Items marked ✅ are complete.

---

## ✅ Phase 1 — Identity & User Service

**Status:** Complete

- Keycloak realm with demo users and OAuth client
- Docker Compose for PostgreSQL and Keycloak
- API Gateway with user-service route
- User Service with JWT validation and lazy user sync
- Candidate profile GET/PUT
- Bruno collection for auth and user endpoints

**Known gaps:** No tests, Hibernate DDL instead of Flyway.

---

## ✅ Phase 2 — Application Service

**Status:** Complete

- Application CRUD and listing with filters/pagination
- Status updates with activity logging
- Offer upsert (one per application)
- Activity timeline and dashboard aggregations
- Flyway migrations (V1–V3)
- Feature-based package structure
- JWT ownership enforcement (404 on cross-user access)
- Integration tests (8 passing)
- Bruno collection for application endpoints

---

## Phase 3 — Observability & Production Readiness

**Status:** Complete

Goal: Make the existing services operable and consistent before adding new domains.

| Item | Status |
|------|--------|
| Structured logging | ✅ JSON in `prod` profile; human-readable in `dev` |
| Correlation IDs | ✅ `X-Request-ID` via gateway and `shared-common` |
| Exception handling | ✅ RFC 7807 `ProblemDetail` with `requestId` (all services) |
| Metrics | ✅ Prometheus via `/actuator/prometheus` |
| Health checks | ✅ Liveness/readiness probes |
| Configuration | ✅ Spring profiles; externalized env vars |
| Flyway (user-service) | ✅ `V1`–`V2` migrations; `ddl-auto: validate` |
| Bruno fixes | ✅ Readiness/prometheus checks; inherited auth |
| User-service tests | ✅ Repository, service, and security tests |
| Documentation | ✅ Updated architecture, status, ADR-007 |

**Deferred from original Phase 3 plan:** Shared security module and `@PreAuthorize` (revisit when more business services exist).

**Not in Phase 3:** New business domains, Kafka, cloud deployment.

---

## ✅ Phase 4 — Frontend MVP

**Status:** Complete

- React SPA (dashboard, applications, profile) via Keycloak Authorization Code + PKCE
- All API calls through API Gateway (`:9000`) with Axios interceptors
- Dashboard with metrics, status chart, and activity feed
- Applications list (filters, pagination), create, detail, status update, offer management
- Candidate profile view and edit
- Gateway CORS for `http://localhost:5173`; Vite `/api` dev proxy
- TanStack Query, React Hook Form + Zod, shadcn/ui, Recharts, next-themes

---

## ✅ Phase 4.1 — Self-Service Registration & Onboarding

**Status:** Complete

- Keycloak self-registration + Google Identity Provider (brokering); new users auto-assigned `CANDIDATE` via a default group
- Frontend landing screen with Sign In / Create Account (`AuthLanding`), replacing unconditional auto-redirect to login
- Profile completion banner on dashboard (dismissible nudge to `/profile`); no route gate
- No backend changes — lazy `User`/`CandidateProfile` sync unchanged (see [ADR-008](./decisions/ADR-008-self-service-registration-and-onboarding.md))

**Deferred:** SMTP + `verifyEmail: true` (required before public/non-local deployment); real Google OAuth credentials (realm export ships with placeholders).

---

## Phase 5 — Event-Driven Architecture

**Status:** Planned

Goal: Decouple side effects and enable notifications.

| Item | Description |
|------|-------------|
| Kafka | Local Docker Compose broker; Spring Kafka producers/consumers |
| Domain events | `ApplicationCreated`, `StatusChanged`, `OfferAdded`, etc. |
| Notification Service | Consume events; email/in-app notifications (initial stub) |
| Event schema | Avro or JSON schema registry (start simple) |

**Prerequisite:** Phase 3 observability (correlation IDs, error handling).

---

## Phase 6 — Resume Management

**Status:** Planned

Goal: Implement the resume bounded context and enrich application tracking.

| Item | Description |
|------|-------------|
| Resume Service | Gradle module on port 8082 |
| Flyway migrations | `careerflow_resume` schema |
| CRUD APIs | Register, list, delete resume versions |
| File storage | Metadata + mock/real object storage URL |
| Interview Service | Schedule rounds, post-interview retros |
| Feign integration | Dashboard pulls active interview count from Interview Service |
| Application ↔ Resume link | Optional `resumeId` on applications (requires design decision) |

**Note:** Gateway routes for `/api/v1/resumes/**` and `/api/v1/interviews/**` already exist.

---

## Phase 7 — Deployment

**Status:** Planned

Goal: Repeatable builds, automated testing, cloud-ready deployment.

| Item | Description |
|------|-------------|
| GitHub Actions | Build + test on PR |
| Container images | Dockerfiles per service |
| Cloud deployment | Target TBD (AWS ECS, Kubernetes, or similar) |
| Monitoring | Dashboards and alerting (e.g. Grafana, CloudWatch) |
| Secrets management | Keycloak client secrets, DB credentials |
| Environment parity | Dev/staging/prod configuration strategy |

---

## Explicitly Out of Scope (for now)

These are mentioned in design docs but not committed to a phase:

- Mobile client
- Analytics warehouse
- Full-text search (Elasticsearch)
- Multi-tenant organization support
- Payment/billing

---

## How to Use This Roadmap

1. Complete Phase 3 before starting Kafka or new services — it reduces carry-forward debt.
2. Phases 4 and 4.1 deliver the SPA and self-service auth; Phase 5+ adds new backend domains.
3. Each phase should end with updated docs in `docs/project-status.md` and Bruno collections.
4. ADRs in `docs/decisions/` should be added when significant architectural choices are made in future phases.

See also: [technical-debt.md](./technical-debt.md), [project-status.md](./project-status.md).
