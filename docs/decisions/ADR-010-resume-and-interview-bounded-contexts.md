# ADR-010: Resume and Interview Bounded Contexts

## Status

Accepted

## Context

Phase 6 introduces resume version management and interview round tracking. These domains have distinct lifecycles from job applications and must remain independently evolvable for future AI Copilot capabilities (resume parsing, interview retrospectives, voice/chat assistants).

## Decision

1. **Resume Service** (`:8082`, `careerflow_resume`) owns resume versions, storage metadata, and parse status. It does not know about applications.
2. **Interview Service** (`:8084`, `careerflow_interview`) owns interview rounds, outcomes, and retrospectives. It stores `application_id` as a soft reference only (no cross-database FK).
3. **Application Service** owns the optional `resume_id` association on `applications`. Validated synchronously via OpenFeign when set.
4. Both new services follow existing patterns: JWT `sub` ownership, Flyway migrations, feature-based packages, transactional outbox tables (events wired in Phase 6).

## Consequences

- Resume and interview data can evolve independently (parsing, AI embeddings) without coupling to application aggregates.
- Application Service remains the hub for job pipeline state; resume/interview enrich it via references and separate APIs.
- Dashboard active-interview count is merged in the frontend from Interview Service `/stats` to avoid backend coupling.
