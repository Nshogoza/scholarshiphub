-- Student-only profile fields, normalized out of `users` so reviewer/admin
-- rows never carry a pile of nullable education columns.
CREATE TABLE student_profiles (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    education_level     VARCHAR(50),
    school              VARCHAR(255),
    gpa                 NUMERIC(4, 2) CHECK (gpa >= 0),
    personal_statement  TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);
