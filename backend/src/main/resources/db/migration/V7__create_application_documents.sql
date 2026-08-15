CREATE TABLE application_documents (
    id                 BIGSERIAL PRIMARY KEY,
    application_id     BIGINT       NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    document_name      VARCHAR(255) NOT NULL,
    original_filename  VARCHAR(255) NOT NULL,
    stored_path        VARCHAR(500) NOT NULL,
    content_type       VARCHAR(100) NOT NULL,
    file_size_bytes    BIGINT       NOT NULL CHECK (file_size_bytes > 0 AND file_size_bytes <= 10485760),
    checksum_sha256    VARCHAR(64)  NOT NULL,
    uploaded_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_application_documents_application_id ON application_documents (application_id);
