# ADR-009: Event-Driven Architecture with Kafka and Transactional Outbox

**Status:** Accepted  
**Date:** 2026-07

## Context

CareerFlow needs to decouple side effects (notifications, future analytics) from synchronous HTTP request handling in Application Service. Cross-service workflows must respect database-per-service boundaries ([ADR-002](./ADR-002-each-service-owns-database.md)) and avoid dual-write failures (DB commit succeeds, Kafka publish fails).

Alternatives considered:

- **Direct Kafka publish after save** — simple but loses events when broker is down after commit
- **`@TransactionalEventListener(AFTER_COMMIT)` + Kafka** — same broker availability gap
- **Transactional outbox** — event persisted in same DB transaction; async poller publishes to Kafka
- **Change Data Capture (Debezium)** — powerful but heavy for local dev and current scale

## Decision

1. **Apache Kafka** (single-node KRaft in Docker Compose) carries domain events between services.
2. **Application Service** publishes via a **transactional outbox** table in `careerflow_application`.
3. **Notification Service** consumes from topic `careerflow.application.events` with idempotent processing.
4. **Event contract:** versioned JSON envelope (`specVersion`, `eventType`, `eventVersion`, `eventId`, metadata, payload). Avro/Schema Registry deferred.
5. **Correlation:** HTTP `X-Request-ID` copied into event metadata and Kafka headers ([ADR-007](./ADR-007-correlation-id-via-x-request-id.md)).
6. **Failure handling:** consumer retries with backoff; poison messages to `careerflow.application.events.DLT`.

### Initial domain events

| Event type | Trigger |
|------------|---------|
| `ApplicationCreated` | POST application |
| `ApplicationStatusChanged` | PATCH status |
| `OfferAdded` | PUT offer (create) |
| `OfferUpdated` | PUT offer (update) |

Events carry `userId` (JWT `sub`) only — no PII enrichment at publish time.

## Consequences

**Positive:**

- HTTP path never blocked on broker availability
- Outbox survives restarts; at-least-once delivery with idempotent consumers
- Clear path to Avro/Schema Registry without changing domain semantics
- Activity log (user timeline) stays separate from cross-service fan-out

**Negative:**

- Eventual consistency for notifications
- Outbox poller adds operational surface (lag, failed rows)
- Duplicate delivery possible — consumers must dedupe on `eventId`

**Follow-up:**

- Email/SMTP when Keycloak verification is enabled
- In-app notification inbox API
- Avro + Confluent Schema Registry when event volume or schema governance requires it
- Saga patterns if multi-service transactions are needed beyond notifications
