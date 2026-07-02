CREATE TABLE candidate_profiles (
    user_id VARCHAR(255) PRIMARY KEY,
    target_roles VARCHAR(255),
    target_salary_min DOUBLE PRECISION,
    target_salary_max DOUBLE PRECISION,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_candidate_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE candidate_skills (
    user_id VARCHAR(255) NOT NULL,
    skill VARCHAR(255),
    CONSTRAINT fk_candidate_skills_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_candidate_skills_user_id ON candidate_skills (user_id);
