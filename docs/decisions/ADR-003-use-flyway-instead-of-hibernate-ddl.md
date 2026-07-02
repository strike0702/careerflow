# ADR-003: Use Flyway Instead of Hibernate DDL Generation

**Status:** Accepted (application-service and user-service)  
**Date:** 2026-06

## Context

Schema management strategies affect reproducibility, code review, and production safety.

Hibernate `ddl-auto: update` auto-mutates schema at startup — convenient for early development but risky for production and non-deterministic across environments.

## Decision

**Application Service** uses:

- **Flyway** for all schema changes (`db/migration/V*.sql`)
- **Hibernate `ddl-auto: validate`** — entities must match Flyway schema exactly

Migration files:

- `V1__create_applications.sql`
- `V2__create_offers.sql`
- `V3__create_activities.sql`

**User Service** uses:

- **Flyway** for all schema changes (`db/migration/V*.sql`)
- **Hibernate `ddl-auto: validate`**

Migration files:

- `V1__create_users.sql`
- `V2__create_candidate_profiles.sql`

## Consequences

**Positive:**

- Schema changes are versioned, reviewable, and replayable
- CI can verify migrations against clean databases
- Hibernate validation catches entity/schema drift at startup
- Aligns with production deployment practices

**Negative:**

- Requires maintaining SQL migrations alongside JPA entities
- Test profile disables Flyway and uses H2 `create-drop` (acceptable test trade-off)

**Follow-up:**

- Add migration verification to CI (Phase 6)
