# ADR-002: Each Microservice Owns Its Own Database

**Status:** Accepted  
**Date:** 2026-06

## Context

CareerFlow is structured as a microservices monorepo. Services must define clear ownership boundaries to avoid tight coupling and accidental data sharing.

Alternatives considered:

- **Shared database with shared tables** — simpler queries but violates service autonomy
- **Schema-per-service in one database** — partial isolation; still shared connection and backup blast radius
- **Database-per-service** — full logical isolation on a shared PostgreSQL instance (current approach)

## Decision

Each microservice connects to its **own logical PostgreSQL database**:

| Service | Database |
|---------|----------|
| User Service | `careerflow_user` |
| Application Service | `careerflow_application` |
| Resume Service (planned) | `careerflow_resume` |
| Interview Service (planned) | `careerflow_interview` |
| Keycloak | `keycloak_db` |

Services must not read or write another service's tables. Cross-service data is referenced by identifier only (e.g. `userId` from JWT `sub`).

## Consequences

**Positive:**

- Independent schema evolution (Flyway per service)
- Clear bounded contexts aligned with DDD
- Application Service stores no user PII — only `userId`
- Path to physical database separation in production

**Negative:**

- No cross-service SQL JOINs
- Dashboard metrics must be computed within service boundaries or via API/event aggregation
- Eventual consistency for cross-service workflows (future Kafka phase)

**Follow-up:**

- Implement Resume and Interview services with their own databases (Phase 5)
- Consider saga/outbox patterns when cross-service transactions are needed (Phase 4)
