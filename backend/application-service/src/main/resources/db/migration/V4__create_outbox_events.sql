CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY,
    event_id        UUID NOT NULL UNIQUE,
    event_type      VARCHAR(100) NOT NULL,
    aggregate_id    UUID NOT NULL,
    partition_key   VARCHAR(255) NOT NULL,
    payload_json    TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ,
    attempts        INT NOT NULL DEFAULT 0,
    last_error      TEXT
);

CREATE INDEX idx_outbox_events_status_created ON outbox_events (status, created_at);
