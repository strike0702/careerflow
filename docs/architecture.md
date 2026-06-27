# CareerFlow Architecture

This document describes the **current implemented architecture** of CareerFlow. Planned services and features are explicitly marked.

---

## 1. System Overview

CareerFlow is a microservices-based backend for tracking job applications. It uses:

- **Keycloak** for identity and JWT issuance
- **Spring Cloud Gateway** as a single HTTP entry point
- **Independent Spring Boot services** with separate databases
- **Local JWT validation** at each service (OAuth2 Resource Server)

```mermaid
graph TB
    subgraph External
        Client[HTTP Client / Bruno]
    end

    subgraph Infrastructure["Infrastructure (Docker Compose)"]
        KC[Keycloak<br/>:8080]
        PG[(PostgreSQL<br/>:5432)]
    end

    subgraph Backend["Backend (Gradle Monorepo)"]
        GW[API Gateway<br/>:9000]
        US[User Service<br/>:8081]
        AS[Application Service<br/>:8083]
    end

    Client -->|OAuth2 token| KC
    Client -->|REST + JWT| GW
    GW --> US
    GW --> AS
    US -->|careerflow_user| PG
    AS -->|careerflow_application| PG
    KC -->|keycloak_db| PG
```

**Planned but not implemented:** Resume Service (`:8082`), Interview Service (`:8084`). Gateway routes for these paths exist; backends do not.

---

## 2. Service Boundaries

Each service owns a bounded context and its database. Services do not share tables or join across databases.

| Service | Responsibility | Does NOT own |
|---------|----------------|--------------|
| **User Service** | User records synced from Keycloak; candidate profiles (target roles, salary range, skills) | Job applications, offers, interviews |
| **Application Service** | Job applications, embedded referral info, offers, activity log, dashboard metrics | User email, name, profile details |
| **API Gateway** | Path-based routing, JWT passthrough | Business logic, authentication |

### Application Service internal structure

Feature-based packages (not layer-based):

```
application-service/
├── application/   web, service, repository, model, dto
├── offer/         web, service, repository, model, dto
├── activity/      web, service, repository, model, dto
└── shared/        security, exception, mapper
```

JPA entities do **not** use bidirectional relationships. `Offer` and `Activity` store `applicationId` and are loaded via separate repository queries.

---

## 3. Request Flow

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Gateway as API Gateway (9000)
    participant Service as Business Service
    participant DB as PostgreSQL

    Client->>Gateway: GET /api/v1/applications<br/>Authorization: Bearer JWT
    Note over Gateway: Match route predicate<br/>No JWT validation
    Gateway->>Service: Forward request + Authorization header
    Note over Service: Fetch/cache JWKS from Keycloak<br/>Validate signature, expiry, issuer
    Note over Service: Extract jwt.sub as userId
    Service->>DB: SELECT ... WHERE user_id = :sub
    DB-->>Service: Rows
    Service-->>Gateway: JSON response
    Gateway-->>Client: HTTP 200
```

### Gateway routing

| Route ID | Path | Upstream |
|----------|------|----------|
| user-service | `/api/v1/users/**` | `localhost:8081` |
| application-service | `/api/v1/applications/**` | `localhost:8083` |
| resume-service | `/api/v1/resumes/**` | `localhost:8082` (planned) |
| interview-service | `/api/v1/interviews/**` | `localhost:8084` (planned) |

---

## 4. Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Keycloak
    participant Service

    Client->>Keycloak: POST /realms/careerflow-realm/protocol/openid-connect/token
    Note over Client,Keycloak: grant_type=password (dev)<br/>client_id=careerflow-api-gateway
    Keycloak-->>Client: access_token (JWT)

    Client->>Service: API call with Bearer token
    Service->>Keycloak: GET /protocol/openid-connect/certs (JWKS, cached)
    Note over Service: Verify RS256 signature<br/>Check exp, iss claims
    Service-->>Client: Authorized response
```

### Token claims used

| Claim | Usage |
|-------|-------|
| `sub` | Primary user identifier; stored as `userId` in application-service |
| `email`, `given_name`, `family_name` | User sync in user-service (not stored in application-service) |
| `realm_access.roles` | Mapped to Spring authorities (`ROLE_CANDIDATE`, `ROLE_ADMIN`) |

---

## 5. JWT Validation

Each business service configures:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8080/realms/careerflow-realm/protocol/openid-connect/certs
```

Validation happens **locally** using Keycloak's public keys (JWKS). Services do not call Keycloak's token introspection endpoint on every request.

`SecurityConfig` in each service:

- Disables CSRF (stateless API)
- Permits `/actuator/health` and `/actuator/info` without auth
- Requires authentication for all other endpoints
- Registers `KeycloakRoleConverter` to map `realm_access.roles` to `GrantedAuthority`

**Note:** `@EnableMethodSecurity` is enabled, but controllers do not yet use `@PreAuthorize("hasRole('CANDIDATE')")`. Any valid JWT can call authenticated endpoints.

---

## 6. Ownership and Multi-Tenancy

CareerFlow is a single-user-per-account system. Data isolation is enforced by scoping every query to the JWT subject.

```mermaid
flowchart LR
    JWT[JWT sub claim] --> Controller["@AuthenticationPrincipal Jwt"]
    Controller --> Service["Service layer"]
    Service --> Repo["Repository<br/>findByIdAndUserId(...)"]
    Repo --> DB[(Filtered rows)]
```

**Application Service rule:** If a resource exists but belongs to another user, return **404 Not Found** — not 403 Forbidden. This avoids leaking resource existence.

Clients must **never** send `userId` in request bodies. Ownership is derived exclusively from the JWT.

---

## 7. Database Ownership

Single PostgreSQL instance, multiple logical databases (database-per-service pattern):

```
PostgreSQL :5432
├── careerflow_user        → User Service
├── careerflow_application → Application Service (Flyway)
├── careerflow_resume      → Planned
├── careerflow_interview   → Planned
└── keycloak_db            → Keycloak
```

### User Service schema (Hibernate-managed)

- `users` — id (Keycloak sub), email, name, role
- `candidate_profiles` — target roles, salary range, skills
- `candidate_skills` — element collection join table

### Application Service schema (Flyway-managed)

| Table | Purpose |
|-------|---------|
| `applications` | Job applications + embedded referral columns + `@Version` |
| `offers` | One offer per application (`application_id` UNIQUE) |
| `activities` | Audit trail; indexed by `user_id` and `application_id` |

---

## 8. Communication Between Services

**Current:** No synchronous inter-service calls are implemented.

- Application Service dashboard computes `activeInterviews` from application status (`INTERVIEWING`), not from Interview Service.
- No OpenFeign clients exist in the codebase.

**Planned:** Interview Service would expose an active interview count; Application Service would call it via Feign with forwarded JWT (see [technical-debt.md](./technical-debt.md)).

**Planned:** Event-driven communication via Kafka for domain events (Phase 4).

---

## 9. Design Rationale

### Why Keycloak?

- Production-grade OAuth2/OIDC without building custom auth
- JWKS-based JWT validation (standard, offline-capable)
- Realm roles, clients, and user federation out of the box
- Demonstrates real-world identity integration

### Why each service owns its database?

- **Loose coupling:** Services evolve schemas independently
- **Clear boundaries:** No shared-table anti-pattern
- **Security:** Application Service never stores PII it doesn't need
- **Scalability path:** Databases can be split to separate instances later

Trade-off: no cross-service JOINs; aggregation requires API calls or eventual consistency (events).

### Why Flyway (application-service)?

- Version-controlled, reviewable schema changes
- Reproducible environments (dev, CI, prod)
- Hibernate `ddl-auto: validate` catches entity/schema drift
- Avoids accidental schema mutation in production

User Service still uses `ddl-auto: update` — migration to Flyway is planned.

### Why JWT `sub` as ownership boundary?

- **Single source of truth** for identity (Keycloak)
- **Stateless services** — no session store
- **IDOR prevention** — every query filtered by authenticated subject
- **No trusted client input** — `userId` never accepted from request body

---

## 10. Error Handling

Application Service uses `@RestControllerAdvice` returning RFC 7807 `ProblemDetail`:

| Condition | HTTP Status |
|-----------|-------------|
| Resource not found / wrong owner | 404 |
| Validation failure (`@Valid`) | 400 |
| Missing/invalid JWT | 401 (Spring Security default) |

User Service throws `IllegalArgumentException` for some cases without a global handler.

---

## 11. Testing Architecture

Application Service tests use:

- `@SpringBootTest` with `test` profile
- H2 in-memory database (`MODE=PostgreSQL`)
- Mock `JwtDecoder` for security tests
- `@AutoConfigureMockMvc` for HTTP-level ownership tests

Flyway is disabled in tests; Hibernate `create-drop` builds schema from entities.

---

## 12. Related Documents

- [project-status.md](./project-status.md) — implementation checklist
- [api-overview.md](./api-overview.md) — endpoint reference
- [decisions/](./decisions/) — Architecture Decision Records
- [technical-debt.md](./technical-debt.md) — known gaps
