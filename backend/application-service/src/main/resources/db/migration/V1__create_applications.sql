CREATE TABLE applications (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    job_url VARCHAR(2048),
    source VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    application_date DATE,
    notes TEXT,
    referred BOOLEAN NOT NULL DEFAULT FALSE,
    referrer_name VARCHAR(255),
    referrer_company_email VARCHAR(255),
    relationship VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_applications_user_id ON applications (user_id);
CREATE INDEX idx_applications_user_id_status ON applications (user_id, status);
CREATE INDEX idx_applications_user_id_company_name ON applications (user_id, company_name);
