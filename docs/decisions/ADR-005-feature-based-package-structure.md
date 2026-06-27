# ADR-005: Use Feature-Based Package Structure

**Status:** Accepted (application-service)  
**Date:** 2026-06

## Context

Package organization affects navigability and coupling. Layer-based packages (`controller`, `service`, `repository` at root) scatter a single feature across many directories.

User Service uses a flatter structure (`UserController`, `domain/`, `config/`) suitable for its small scope. Application Service has three domains (application, offer, activity) with more complexity.

## Decision

**Application Service** uses feature-based packaging:

```
com.careerflow.applicationservice/
├── application/   web, service, repository, model, dto
├── offer/         web, service, repository, model, dto
├── activity/      web, service, repository, model, dto
└── shared/        security, exception, mapper
```

Each feature owns its web layer, business logic, persistence, models, and DTOs. Cross-cutting concerns live in `shared/`.

User Service retains its simpler structure until it grows enough to warrant refactoring.

## Consequences

**Positive:**

- Related code for a feature is co-located
- Easier to extract a feature into a separate service later
- Clear mapping between domain concepts and code structure
- Aligns with vertical slice architecture principles

**Negative:**

- Some duplication across features (separate controllers on same base path)
- `shared/` can become a dumping ground if not disciplined
- Inconsistent with user-service structure across the monorepo

**Follow-up:**

- Apply same structure to Resume and Interview services when implemented
- Consider shared module for security/config only, not domain code
