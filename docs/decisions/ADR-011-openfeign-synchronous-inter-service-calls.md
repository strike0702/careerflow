# ADR-011: Synchronous Inter-Service Communication via OpenFeign

## Status

Accepted

## Context

Interview Service must validate that an `applicationId` belongs to the authenticated user before scheduling a round. Application Service must optionally validate `resumeId` when linking a resume to an application. These are read-only ownership checks requiring synchronous HTTP calls.

## Decision

Use **Spring Cloud OpenFeign** (`spring-cloud-starter-openfeign`, BOM `2023.0.1`) for synchronous inter-service calls:

- **Interview Service → Application Service:** `ApplicationClient.getApplication(id)` on create
- **Application Service → Resume Service:** `ResumeClient.getResume(id)` when `resumeId` is provided on application create

Each calling service registers a `FeignAuthInterceptor` that forwards `Authorization` (Bearer JWT) and `X-Request-ID` from the inbound request context.

Services call peer services **directly** (not through the gateway) using configurable base URLs (`careerflow.clients.*.url`).

## Consequences

- Adds Spring Cloud dependency to application-service and interview-service.
- JWT and correlation context propagate to downstream validation calls.
- Cross-service failures surface as 404 (not found) or 502-style illegal state for unexpected Feign errors.
- Async domain events remain the preferred pattern for side effects; sync calls are limited to ownership validation.
