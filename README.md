# CareerFlow

An **Identity-Aware Job Application Tracker** built with Spring Boot microservices, Keycloak, and PostgreSQL. CareerFlow demonstrates production-style backend patterns: OAuth2/OIDC, JWT-scoped data ownership, database-per-service, Flyway migrations, and API gateway routing.

---

## Motivation

Job searching involves tracking applications across many companies, statuses, referrals, and offers. CareerFlow provides a backend platform that:

- Authenticates users through a real identity provider (Keycloak)
- Scopes all data to the authenticated user via JWT subject
- Separates identity/profile data from application tracking data
- Exposes a consistent REST API through an API Gateway

This project is designed to be readable by senior engineers, hiring managers, and interviewers evaluating backend architecture skills.

---

## Architecture

```mermaid
graph TB
    subgraph Client
        Bruno[Bruno / HTTP Client]
    end

    subgraph Identity
        KC[Keycloak :8080]
    end

    subgraph Gateway
        GW[API Gateway :9000]
    end

    subgraph Services
        US[User Service :8081]
        AS[Application Service :8083]
        RS[Resume Service :8082<br/>Planned]
        IS[Interview Service :8084<br/>Planned]
    end

    subgraph Data
        PG[(PostgreSQL :5432)]
    end

    Bruno -->|1. Get token| KC
    Bruno -->|2. Bearer JWT| GW
    GW --> US
    GW --> AS
    GW -.-> RS
    GW -.-> IS
    US --> PG
    AS --> PG
    KC --> PG
```

**Request flow:** Client obtains a JWT from Keycloak → sends requests to the gateway with `Authorization: Bearer <token>` → gateway forwards to the appropriate service → service validates JWT locally and scopes data to `jwt.sub`.

See [docs/architecture.md](docs/architecture.md) for full system design.

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3, Spring Security 6, Spring Data JPA |
| Gateway | Spring Cloud Gateway |
| Identity | Keycloak 24 (OAuth2/OIDC) |
| Database | PostgreSQL 16 |
| Migrations | Flyway (application-service) |
| Build | Gradle 8.10 (multi-module) |
| API Testing | Bruno (OpenCollection) |
| Containers | Docker Compose |

---

## Repository Structure

```
CareerFlow/
├── README.md
├── backend/                          # Gradle monorepo
│   ├── api-gateway/                  # Port 9000 — routing only
│   ├── user-service/                 # Port 8081 — users & profiles
│   └── application-service/          # Port 8083 — applications, offers, activities
├── infrastructure/
│   ├── docker-compose.yml            # PostgreSQL + Keycloak
│   ├── postgres/init.sql             # Logical database bootstrap
│   └── keycloak/realm-export.json    # Realm, roles, demo users
├── bruno/                            # API test collection
└── docs/
    ├── project-status.md             # Current implementation status
    ├── architecture.md               # System architecture
    ├── api-overview.md               # Implemented API reference
    ├── api-contracts.md              # Original design spec (partially outdated)
    ├── technical-debt.md             # Deferred work
    ├── roadmap.md                    # Future phases
    ├── setup-guide.md                # Infrastructure setup
    ├── local-development.md          # Dev workflow notes
    └── decisions/                    # Architecture Decision Records
```

---

## Features

### Implemented

- **Authentication** via Keycloak JWT access tokens
- **User Service:** sync user from JWT, read/update candidate profile
- **Application Service:**
  - Create and list job applications (with status/company filters and pagination)
  - View application details with offer and recent activities
  - Update application status with automatic activity logging
  - Upsert compensation offers
  - Activity timeline across all applications
  - Dashboard with aggregated metrics and response rate
- **Security:** JWT validation at each service; cross-user access returns 404
- **Flyway migrations** for application-service schema
- **Bruno collection** for manual API testing

### Planned

- Resume Service, Interview Service
- Role-based method security (`@PreAuthorize`)
- Event-driven notifications (Kafka)
- CI/CD and cloud deployment

See [docs/project-status.md](docs/project-status.md) for details.

---

## Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant Keycloak
    participant Gateway
    participant Service

    Client->>Keycloak: POST /token (password or auth code)
    Keycloak-->>Client: Access JWT
    Client->>Gateway: API request + Authorization Bearer
    Gateway->>Service: Forward request + JWT
    Service->>Service: Validate JWT via JWKS
    Service->>Service: Extract sub claim for ownership
    Service-->>Gateway: Response
    Gateway-->>Client: Response
```

Obtain a token with Bruno: **Auth → Get Candidate Token**, then paste the `access_token` into other requests.

---

## Running Locally

### Prerequisites

- JDK 21
- Docker & Docker Compose v2

### 1. Start infrastructure

```bash
cd infrastructure
docker compose up -d
```

Verify:

- PostgreSQL: `localhost:5432` (user `postgres`, password `password`)
- Keycloak admin: `http://localhost:8080/admin` (`admin` / `admin`)

### 2. Start backend services

Open separate terminals:

```bash
cd backend

# Terminal 1 — API Gateway
./gradlew :api-gateway:bootRun

# Terminal 2 — User Service
./gradlew :user-service:bootRun

# Terminal 3 — Application Service
./gradlew :application-service:bootRun
```

| Service | Direct URL | Via Gateway |
|---------|------------|-------------|
| API Gateway | `http://localhost:9000` | — |
| User Service | `http://localhost:8081` | `http://localhost:9000/api/v1/users/**` |
| Application Service | `http://localhost:8083` | `http://localhost:9000/api/v1/applications/**` |

### 3. Run tests

```bash
cd backend
./gradlew :application-service:test
```

See [docs/setup-guide.md](docs/setup-guide.md) and [docs/local-development.md](docs/local-development.md) for additional setup notes.

---

## Docker Setup

Docker Compose (`infrastructure/docker-compose.yml`) provides:

- **PostgreSQL 16** with init script creating logical databases
- **Keycloak 24.0.5** with realm auto-import

Databases: `careerflow_user`, `careerflow_application`, `careerflow_resume`, `careerflow_interview`, `keycloak_db`.

---

## Keycloak Setup

Realm: `careerflow-realm`

| Item | Value |
|------|-------|
| Token URL | `http://localhost:8080/realms/careerflow-realm/protocol/openid-connect/token` |
| JWKS URL | `http://localhost:8080/realms/careerflow-realm/protocol/openid-connect/certs` |
| OAuth client | `careerflow-api-gateway` |
| Demo candidate | `candidate@careerflow.com` / `password` |
| Roles | `CANDIDATE`, `ADMIN` |

---

## Flyway Migrations

Application Service uses Flyway with `spring.jpa.hibernate.ddl-auto: validate`.

Migrations live in `backend/application-service/src/main/resources/db/migration/`:

| Version | File | Creates |
|---------|------|---------|
| V1 | `V1__create_applications.sql` | `applications` table (with embedded referral columns) |
| V2 | `V2__create_offers.sql` | `offers` table |
| V3 | `V3__create_activities.sql` | `activities` table |

User Service currently uses Hibernate `ddl-auto: update` (no Flyway yet).

---

## API Gateway Routing

Configured in `backend/api-gateway/src/main/resources/application.yml`:

| Path prefix | Target | Status |
|-------------|--------|--------|
| `/api/v1/users/**` | `http://localhost:8081` | Implemented |
| `/api/v1/applications/**` | `http://localhost:8083` | Implemented |
| `/api/v1/resumes/**` | `http://localhost:8082` | Planned |
| `/api/v1/interviews/**` | `http://localhost:8084` | Planned |

The gateway does not validate JWTs; it forwards the `Authorization` header to downstream services.

---

## Bruno Usage

1. Open the `bruno/` folder in [Bruno](https://www.usebruno.com/).
2. Run **Auth → Get Candidate Token**.
3. Copy `access_token` into the bearer token field of other requests (or configure collection auth).
4. Test services directly (port 8081/8083) or through the gateway (port 9000).

Collection folders:

- **Auth** — Keycloak token requests
- **User Service** — profile endpoints
- **Application Service** — application lifecycle endpoints
- **Gateway** — routed requests through port 9000
- **Actuator** — health checks

---

## Current APIs

Quick reference (all require `Authorization: Bearer <JWT>`):

**User Service**

- `GET /api/v1/users/me`
- `GET /api/v1/users/me/profile`
- `PUT /api/v1/users/me/profile`

**Application Service**

- `POST /api/v1/applications`
- `GET /api/v1/applications`
- `GET /api/v1/applications/{id}`
- `PATCH /api/v1/applications/{id}/status`
- `PUT /api/v1/applications/{id}/offer`
- `GET /api/v1/applications/activities`
- `GET /api/v1/applications/dashboard`

Full schemas: [docs/api-overview.md](docs/api-overview.md)

---

## Screenshots

<!-- Placeholder: add UI screenshots here when a frontend is built -->

No frontend is implemented yet. API testing is done via Bruno or any HTTP client.

---

## Documentation Index

| Document | Purpose |
|----------|---------|
| [project-status.md](docs/project-status.md) | What is built vs planned |
| [architecture.md](docs/architecture.md) | System design and security |
| [api-overview.md](docs/api-overview.md) | Implemented API reference |
| [technical-debt.md](docs/technical-debt.md) | Deferred improvements |
| [roadmap.md](docs/roadmap.md) | Future phases |
| [decisions/](docs/decisions/) | Architecture Decision Records |

---

## Future Roadmap

| Phase | Status | Focus |
|-------|--------|-------|
| Phase 1 | ✅ Complete | Identity & User Service |
| Phase 2 | ✅ Complete | Application Service |
| Phase 3 | Planned | Observability & production readiness |
| Phase 4 | Planned | Event-driven architecture |
| Phase 5 | Planned | Resume management |
| Phase 6 | Planned | Deployment & CI/CD |

Details: [docs/roadmap.md](docs/roadmap.md)

---

## License

Not specified.
