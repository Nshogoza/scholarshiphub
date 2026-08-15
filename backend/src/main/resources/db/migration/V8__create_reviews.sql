-- Every review action (including "request additional info") is appended
-- here rather than overwritten, so the full review history for an
-- application is reconstructable from this table alone.
CREATE TABLE reviews (
    id             BIGSERIAL PRIMARY KEY,
    application_id BIGINT        NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    reviewer_id    BIGINT        NOT NULL REFERENCES users (id),
    score          NUMERIC(5, 2) CHECK (score >= 0 AND score <= 100),
    comments       TEXT,
    recommendation VARCHAR(30)   NOT NULL
                   CHECK (recommendation IN ('APPROVE', 'REJECT', 'REQUEST_ADDITIONAL_INFO')),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_reviews_application_id ON reviews (application_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews (reviewer_id);
