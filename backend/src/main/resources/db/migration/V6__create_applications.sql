-- One application per student per scholarship (a rejected/completed
-- application is not deleted, so re-applying is intentionally disallowed --
-- students edit their existing draft/submitted application instead).
CREATE TABLE applications (
    id             BIGSERIAL PRIMARY KEY,
    scholarship_id BIGINT      NOT NULL REFERENCES scholarships (id),
    student_id     BIGINT      NOT NULL REFERENCES users (id),
    reviewer_id    BIGINT      REFERENCES users (id),
    status         VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
                   CHECK (status IN (
                       'DRAFT', 'SUBMITTED', 'UNDER_REVIEW',
                       'APPROVED', 'REJECTED', 'ADDITIONAL_INFO_REQUIRED'
                   )),
    submitted_at   TIMESTAMPTZ,
    decided_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (scholarship_id, student_id)
);

CREATE INDEX idx_applications_student_id ON applications (student_id);
CREATE INDEX idx_applications_scholarship_id ON applications (scholarship_id);
CREATE INDEX idx_applications_reviewer_id ON applications (reviewer_id);
CREATE INDEX idx_applications_status ON applications (status);
