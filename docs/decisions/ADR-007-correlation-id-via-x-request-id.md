# ADR-007: Correlation ID Propagation via X-Request-ID

**Status:** Accepted  
**Date:** 2026-06

## Context

CareerFlow requests may pass through the API Gateway before reaching a business service. Without a shared request identifier, logs across services cannot be correlated during debugging or incident response.

Alternatives considered:

- **W3C `traceparent`** — standard for distributed tracing; heavier than needed without OpenTelemetry
- **Custom `X-Correlation-ID`** — common but less conventional than `X-Request-ID`
- **`X-Request-ID`** — widely used, simple, sufficient for log correlation in Phase 3

## Decision

Use the **`X-Request-ID`** HTTP header for request correlation:

1. **API Gateway** generates a UUID when the header is absent, forwards it to downstream services, and echoes it on the response.
2. **Business services** read the header (or generate one for direct calls), bind the value to SLF4J MDC as `requestId`, and echo it on the response.
3. **Error responses** include `requestId` in RFC 7807 `ProblemDetail` payloads.
4. **Access logs** include `requestId` but never log `Authorization` headers or request bodies.

Implementation lives in the `shared-common` Gradle module (servlet filters) and `api-gateway` (reactive global filter).

## Consequences

**Positive:**

- End-to-end log correlation for gateway-routed requests
- Consistent debugging experience across services
- Minimal overhead; no tracing infrastructure required

**Negative:**

- Direct service calls bypassing the gateway may receive a different ID if the client omits the header (mitigated by per-service generation)
- Not a substitute for full distributed tracing (deferred)

**Follow-up:**

- Consider OpenTelemetry integration if multi-hop workflows grow (Phase 7+)
