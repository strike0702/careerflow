CREATE TABLE resumes (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    label VARCHAR(255) NOT NULL,
    version_no INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    file_size_bytes BIGINT,
    storage_url VARCHAR(2048) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    parse_status VARCHAR(30) NOT NULL DEFAULT 'NOT_PARSED',
    parsed_at TIMESTAMPTZ,
    parse_error TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_resumes_user_id ON resumes (user_id);
CREATE UNIQUE INDEX idx_resumes_user_primary ON resumes (user_id) WHERE is_primary = TRUE;

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
