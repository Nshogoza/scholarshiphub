-- Generic append-only audit trail for security and business events (logins,
-- failed logins, submissions, review decisions, admin actions). Kept
-- separate from `reviews`/status columns because it must survive even if the
-- actor is later deleted (hence ON DELETE SET NULL, not CASCADE).
CREATE TABLE audit_logs (
    id            BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT       REFERENCES users (id) ON DELETE SET NULL,
    action        VARCHAR(100) NOT NULL,
    entity_type   VARCHAR(100),
    entity_id     BIGINT,
    details       TEXT,
    ip_address    VARCHAR(64),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
