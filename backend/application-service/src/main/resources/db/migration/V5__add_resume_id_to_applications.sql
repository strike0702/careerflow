ALTER TABLE applications ADD COLUMN resume_id UUID;

CREATE INDEX idx_applications_resume_id ON applications (resume_id);
