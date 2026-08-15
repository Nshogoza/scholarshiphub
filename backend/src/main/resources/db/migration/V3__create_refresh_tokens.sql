-- Refresh tokens are never stored in plaintext -- only a SHA-256 hash of the
-- opaque token value the client holds (in an httpOnly cookie). `family_id`
-- ties together every token produced by one rotation chain so that reuse of
-- a revoked token can invalidate the whole family (theft detection).
CREATE TABLE refresh_tokens (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash              VARCHAR(255) NOT NULL UNIQUE,
    family_id               UUID         NOT NULL,
    issued_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at              TIMESTAMPTZ  NOT NULL,
    revoked_at              TIMESTAMPTZ,
    replaced_by_token_hash  VARCHAR(255),
    user_agent              VARCHAR(255),
    ip_address              VARCHAR(64)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
