CREATE TABLE activities (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_activities_user_id_created_at ON activities (user_id, created_at DESC);
CREATE INDEX idx_activities_application_id_created_at ON activities (application_id, created_at DESC);
