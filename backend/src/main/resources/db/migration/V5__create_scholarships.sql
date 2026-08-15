CREATE TABLE scholarships (
    id                    BIGSERIAL PRIMARY KEY,
    title                 VARCHAR(255)   NOT NULL,
    description           TEXT           NOT NULL,
    eligibility_criteria  TEXT           NOT NULL,
    amount                NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    application_deadline  TIMESTAMPTZ    NOT NULL,
    status                VARCHAR(20)    NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED')),
    created_by            BIGINT         NOT NULL REFERENCES users (id),
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_scholarships_status ON scholarships (status);
CREATE INDEX idx_scholarships_deadline ON scholarships (application_deadline);

-- Normalized child table (rather than a JSON column) so required documents
-- participate in real FK constraints and can be queried relationally.
CREATE TABLE scholarship_required_documents (
    id             BIGSERIAL PRIMARY KEY,
    scholarship_id BIGINT       NOT NULL REFERENCES scholarships (id) ON DELETE CASCADE,
    document_name  VARCHAR(255) NOT NULL,
    is_mandatory   BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE (scholarship_id, document_name)
);

CREATE INDEX idx_scholarship_required_documents_scholarship_id
    ON scholarship_required_documents (scholarship_id);
