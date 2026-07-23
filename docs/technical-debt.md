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

**Suggested phase:** Post Phase 3 — revisit when Resume, Notification, or Interview services are added

---

### Method-Level Role Enforcement (`@PreAuthorize`)

**Why deferred:** Authentication (valid JWT) was prioritized over fine-grained authorization. `@EnableMethodSecurity` is configured but controllers lack `@PreAuthorize("hasRole('CANDIDATE')")`.

**Benefits:**

- Enforces Keycloak roles at API level
- Prevents admin/candidate boundary violations

**Suggested phase:** Post Phase 3 — revisit with shared security module when more services exist

---

### Event Publishing / Kafka — Implemented (Phase 5)

Transactional outbox in Application Service publishes to `careerflow.application.events`. See [ADR-009](./decisions/ADR-009-kafka-event-driven-architecture.md).

**Remaining:**

- Avro + Confluent Schema Registry
- Analytics/event replication pipeline

---

### Notification Service — Partially Implemented (Phase 5)

Stub consumer logs would-notify messages. No public REST API, no email/SMS.

**Remaining:**

- SMTP/Mailhog integration
- In-app notification inbox + gateway route
- User email enrichment via User Service (async)

---

### Resume Service

**Why deferred:** Gateway route exists; application tracking was higher priority. Application Service does not reference resume IDs.

**Benefits:**

- Versioned resume metadata
- Attach resume versions to applications (future)

**Suggested phase:** Phase 6

---

### Interview Service

**Why deferred:** Dashboard currently derives `activeInterviews` from application status (`INTERVIEWING`), not from scheduled interview rounds.

**Benefits:**

- Interview scheduling and retro logging
- Accurate active interview counts
- Feeds richer dashboard metrics

**Suggested phase:** Phase 6 (after or alongside Resume Service)

---

### Analytics Service

**Why deferred:** Dashboard aggregations are sufficient for Phase 2. No separate read model or warehouse exists.

**Benefits:**

- Historical trends, funnel analysis
- Offloads heavy queries from OLTP databases

**Suggested phase:** Post Phase 7 (only if scale demands it)

---

### Search Optimization

**Why deferred:** List endpoint supports status + company filter with DB indexes. Full-text search is not required at current scale.

**Benefits:**

- Faster company/title search
- Elasticsearch/OpenSearch integration

**Suggested phase:** Phase 6+ (when application volume grows)

---

### Distributed Tracing

**Why deferred:** No multi-hop service calls in implemented code. Single-service requests dominate.

**Benefits:**

- End-to-end request visibility across gateway and services
- Latency debugging in production

**Suggested phase:** Phase 7+ (correlation IDs and structured logging are in place from Phase 3)

---

### OpenFeign / Inter-Service HTTP

**Why deferred:** Documented in `local-development.md` but not implemented. Dashboard does not call Interview Service.

**Benefits:**

- Live interview counts from Interview Service
- Composed dashboard without duplicating interview state

**Suggested phase:** Phase 6 (when Interview Service exists)

---

### CI/CD Pipeline

**Why deferred:** Local development workflow only. No GitHub Actions or deployment targets configured.

**Benefits:**

- Automated test runs on PR
- Repeatable builds and deployments

**Suggested phase:** Phase 7

---

### Legacy Documentation (`api-contracts.md`)

**Why deferred:** Original design spec written before Phase 2 implementation. Describes different field names (`roleTitle` vs `jobTitle`), separate referrals table, `POST /offers` vs `PUT /offer`, and unimplemented services.

**Benefits:**

- Single source of truth ([api-overview.md](./api-overview.md))
- Less confusion for reviewers

**Suggested phase:** Resolved — use [api-overview.md](./api-overview.md) as the API reference (`api-contracts.md` removed)

---

## Summary Table

| Item | Phase | Priority |
|------|-------|----------|
| Shared security module | Post 3 | Medium |
| `@PreAuthorize` | Post 3 | Medium |
| Kafka / events | 5 | Medium |
| Notification Service | 5 | Medium |
| Resume Service | 6 | Medium |
| Interview Service + Feign | 6 | Medium |
| CI/CD | 7 | High |
| Distributed tracing (OpenTelemetry) | 7+ | Low |
| Analytics / search | 6+ | Low |
