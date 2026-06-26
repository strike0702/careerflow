CREATE TABLE offers (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL UNIQUE REFERENCES applications (id) ON DELETE CASCADE,
    base_salary NUMERIC(19, 2),
    joining_bonus NUMERIC(19, 2),
    annual_bonus NUMERIC(19, 2),
    stock_value NUMERIC(19, 2),
    currency VARCHAR(3),
    joining_date DATE,
    offer_status VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_offers_application_id ON offers (application_id);
