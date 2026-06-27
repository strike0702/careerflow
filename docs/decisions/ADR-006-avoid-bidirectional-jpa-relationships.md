# ADR-006: Avoid Bidirectional JPA Relationships

**Status:** Accepted  
**Date:** 2026-06

## Context

JPA bidirectional associations (`@OneToMany` / `@ManyToOne` pairs, `@OneToOne` on both sides) introduce:

- Lazy loading surprises and N+1 queries
- Circular JSON serialization issues
- Complex cascade and orphan removal rules
- Tight coupling between aggregate roots

Application Service has an `Application` aggregate with related `Offer` (1:1) and `Activity` (1:N) entities.

## Decision

Use **unidirectional references by foreign key only**:

- `Offer.applicationId` → `applications.id` (no `Application.offer` field)
- `Activity.applicationId` → `applications.id` (no `Application.activities` list)

Load related entities through separate repository methods:

```java
offerRepository.findByApplicationId(applicationId);
activityRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId, pageable);
```

Do not use `@OneToMany`, `@OneToOne` inverse mappings, or `@ManyToOne` on the `Application` entity for offer/activity.

Referral information is an `@Embedded` value object within `Application`, not a separate entity.

## Consequences

**Positive:**

- Simpler entity model and transaction boundaries
- Explicit fetch control in service layer
- Avoids accidental lazy initialization in web layer
- Detail API composes application + offer + activities in service/mapper layer

**Negative:**

- No JPA-level referential navigation (`application.getOffer()`)
- Manual composition in `ApplicationMapper.toDetailResponse()`
- Application-level deletes rely on DB `ON DELETE CASCADE` for offers/activities

**Follow-up:**

- Maintain this pattern in Resume and Interview services
- Document in onboarding materials for contributors
