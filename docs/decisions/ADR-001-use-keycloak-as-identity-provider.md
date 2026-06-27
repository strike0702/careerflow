# ADR-001: Use Keycloak as Identity Provider

**Status:** Accepted  
**Date:** 2026-06

## Context

CareerFlow requires user authentication, JWT issuance, and role management. Building a custom auth server would distract from the core domain (job application tracking) and would not demonstrate industry-standard identity integration expected in production systems.

Alternatives considered:

- **Custom JWT auth** — full control but high maintenance and security risk
- **Auth0 / Cognito** — managed but adds external dependency and cost for a portfolio project
- **Keycloak (self-hosted)** — open source, OIDC-compliant, runs locally via Docker

## Decision

Use **Keycloak 24** as the Identity Provider with:

- Realm: `careerflow-realm`
- OAuth2 client: `careerflow-api-gateway`
- Realm roles: `CANDIDATE`, `ADMIN`
- JWT access tokens validated locally by each service via JWKS

## Consequences

**Positive:**

- Standard OAuth2/OIDC flow demonstrable to interviewers
- JWKS-based validation avoids per-request token introspection
- Realm import (`realm-export.json`) enables reproducible local setup
- Demo users available immediately after `docker compose up`

**Negative:**

- Additional infrastructure to run (Keycloak container)
- Issuer/JWKS URL must match between Keycloak and services (`localhost` vs container hostname)
- Role mapping requires custom `KeycloakRoleConverter` in each service until shared module exists

**Follow-up:**

- Extract shared security configuration (see ADR-005 area, [technical-debt.md](../technical-debt.md))
- Apply `@PreAuthorize` for role enforcement (Phase 3)
