# ADR-004: Use JWT Subject as Ownership Boundary

**Status:** Accepted  
**Date:** 2026-06

## Context

CareerFlow is a personal job tracker: each user must only access their own data. Ownership must not depend on client-supplied identifiers, which can be forged or manipulated.

Alternatives considered:

- **Accept `userId` in request body/query** — vulnerable to IDOR
- **Session-based ownership** — requires session store; conflicts with stateless JWT approach
- **JWT `sub` claim as sole ownership key** — standard OIDC subject identifier

## Decision

1. Extract `userId` exclusively from `jwt.getSubject()` in controllers/services.
2. **Never** accept `userId` from client requests.
3. Scope all repository queries with `userId = jwt.sub`.
4. Return **404 Not Found** (not 403) when a resource exists but belongs to another user.

Application Service example:

```java
applicationRepository.findByIdAndUserId(id, jwt.getSubject())
```

## Consequences

**Positive:**

- Strong IDOR prevention
- Stateless services — no server-side session mapping
- Consistent identity source (Keycloak) across all services
- Security test verifies User A cannot access User B's application

**Negative:**

- Cannot implement admin "view any user" without separate authorization rules
- User ID format tied to Keycloak subject (typically UUID string)

**Follow-up:**

- Add `@PreAuthorize` for role-based access (Phase 3)
- Define admin override pattern if needed (separate endpoints with explicit authorization)
