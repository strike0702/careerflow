CREATE TABLE interviews (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    application_id UUID NOT NULL,
    round_number INT NOT NULL,
    round_type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    mode VARCHAR(30) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    duration_minutes INT,
    meeting_link VARCHAR(2048),
    location VARCHAR(255),
    interviewer_names TEXT,
    status VARCHAR(30) NOT NULL,
    outcome VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_interviews_user_id ON interviews (user_id);
CREATE INDEX idx_interviews_user_application ON interviews (user_id, application_id);
CREATE INDEX idx_interviews_user_status ON interviews (user_id, status);
CREATE INDEX idx_interviews_user_scheduled_at ON interviews (user_id, scheduled_at);

CREATE TABLE interview_retrospectives (
    id UUID PRIMARY KEY,
    interview_id UUID NOT NULL UNIQUE REFERENCES interviews (id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    what_went_well TEXT,
    what_to_improve TEXT,
    questions_asked TEXT,
    self_rating INT,
    follow_up_actions TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_interview_retrospectives_user_id ON interview_retrospectives (user_id);

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
