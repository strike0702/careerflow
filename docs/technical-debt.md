# CareerFlow Technical Debt

This document tracks intentionally deferred work. Items here are known gaps—not oversights—and are prioritized in [roadmap.md](./roadmap.md).

---

## Deferred Improvements

### Shared Security Module

**Why deferred:** Phase 1–2 focused on proving service boundaries and JWT validation per service. Copy-paste `SecurityConfig` was faster than extracting a shared Gradle module.

**Benefits:**

- Single place to update Keycloak role mapping
- Consistent actuator/security rules across services
- Reduced drift between user-service and application-service configs

**Suggested phase:** Phase 3 (alongside configuration improvements)

---

### Flyway in User Service

**Why deferred:** User Service predates the Flyway strategy adopted in Phase 2. It currently uses Hibernate `ddl-auto: update`.

**Benefits:**

- Version-controlled schema
- Consistent with application-service approach
- Safe production deployments

**Suggested phase:** Phase 3

---

### Method-Level Role Enforcement (`@PreAuthorize`)

**Why deferred:** Authentication (valid JWT) was prioritized over fine-grained authorization. `@EnableMethodSecurity` is configured but controllers lack `@PreAuthorize("hasRole('CANDIDATE')")`.

**Benefits:**

- Enforces Keycloak roles at API level
- Prevents admin/candidate boundary violations

**Suggested phase:** Phase 3

---

### Global Exception Handling (User Service)

**Why deferred:** Application Service introduced `GlobalExceptionHandler` with RFC 7807 `ProblemDetail`. User Service still throws raw exceptions.

**Benefits:**

- Consistent error responses across services
- Better client experience and debugging

**Suggested phase:** Phase 3

---

### Event Publishing / Kafka

**Why deferred:** Phase 2 is synchronous CRUD. No cross-service workflows require events yet.

**Benefits:**

- Decoupled notification delivery
- Audit trail replication
- Future analytics pipeline

**Suggested phase:** Phase 4

---

### Notification Service

**Why deferred:** No event infrastructure; no user-facing notification requirements in Phase 1–2.

**Benefits:**

- Email/push on status changes, offer deadlines
- Driven by domain events from application-service

**Suggested phase:** Phase 4

---

### Resume Service

**Why deferred:** Gateway route exists; application tracking was higher priority. Application Service does not reference resume IDs.

**Benefits:**

- Versioned resume metadata
- Attach resume versions to applications (future)

**Suggested phase:** Phase 5

---

### Interview Service

**Why deferred:** Dashboard currently derives `activeInterviews` from application status (`INTERVIEWING`), not from scheduled interview rounds.

**Benefits:**

- Interview scheduling and retro logging
- Accurate active interview counts
- Feeds richer dashboard metrics

**Suggested phase:** Phase 5 (after or alongside Resume Service)

---

### Analytics Service

**Why deferred:** Dashboard aggregations are sufficient for Phase 2. No separate read model or warehouse exists.

**Benefits:**

- Historical trends, funnel analysis
- Offloads heavy queries from OLTP databases

**Suggested phase:** Post Phase 6 (only if scale demands it)

---

### Search Optimization

**Why deferred:** List endpoint supports status + company filter with DB indexes. Full-text search is not required at current scale.

**Benefits:**

- Faster company/title search
- Elasticsearch/OpenSearch integration

**Suggested phase:** Phase 5+ (when application volume grows)

---

### Distributed Tracing

**Why deferred:** No multi-hop service calls in implemented code. Single-service requests dominate.

**Benefits:**

- End-to-end request visibility across gateway and services
- Latency debugging in production

**Suggested phase:** Phase 3 (with correlation IDs and structured logging)

---

### OpenFeign / Inter-Service HTTP

**Why deferred:** Documented in `local-development.md` but not implemented. Dashboard does not call Interview Service.

**Benefits:**

- Live interview counts from Interview Service
- Composed dashboard without duplicating interview state

**Suggested phase:** Phase 5 (when Interview Service exists)

---

### CI/CD Pipeline

**Why deferred:** Local development workflow only. No GitHub Actions or deployment targets configured.

**Benefits:**

- Automated test runs on PR
- Repeatable builds and deployments

**Suggested phase:** Phase 6

---

### Bruno Collection Fixes

**Why deferred:** Collection grew organically during Phase 1–2 development.

**Known issues:**

- `Update Candidate Profile` uses GET instead of PUT
- `Gateway Health Check` points to port 8081 instead of 9000
- Hardcoded JWT in Update Candidate Profile request

**Suggested phase:** Phase 3 (documentation pass follow-up)

---

### Legacy Documentation (`api-contracts.md`)

**Why deferred:** Original design spec written before Phase 2 implementation. Describes different field names (`roleTitle` vs `jobTitle`), separate referrals table, `POST /offers` vs `PUT /offer`, and unimplemented services.

**Benefits:**

- Single source of truth ([api-overview.md](./api-overview.md))
- Less confusion for reviewers

**Suggested phase:** Phase 3 — deprecate or rewrite `api-contracts.md` to reference `api-overview.md`

---

## Summary Table

| Item | Phase | Priority |
|------|-------|----------|
| Flyway (user-service) | 3 | High |
| Shared security module | 3 | Medium |
| `@PreAuthorize` | 3 | Medium |
| Global exception handler (user-service) | 3 | Medium |
| Correlation IDs / structured logging | 3 | Medium |
| Bruno collection fixes | 3 | Low |
| Kafka / events | 4 | Medium |
| Notification Service | 4 | Medium |
| Resume Service | 5 | Medium |
| Interview Service + Feign | 5 | Medium |
| CI/CD | 6 | High |
| Distributed tracing | 3–6 | Low |
| Analytics / search | 5+ | Low |
