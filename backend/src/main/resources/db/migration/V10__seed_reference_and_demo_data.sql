-- Reference data required for the app to function.
INSERT INTO roles (name, description) VALUES
    ('STUDENT', 'Applies for scholarships and tracks application progress'),
    ('REVIEWER', 'Evaluates assigned scholarship applications'),
    ('ADMIN', 'Manages scholarships, users, reviewers, and platform analytics');

-- Demo/seed accounts so the whole flow (login -> apply -> review -> approve)
-- can be exercised immediately after `docker compose up` with no manual
-- setup. Passwords below are bcrypt hashes (cost 10) -- the plaintext
-- values are documented in README.md for local/demo use only and MUST be
-- rotated (or these rows removed) before any real deployment.
INSERT INTO users (email, password_hash, first_name, last_name, role_id, status, email_verified)
VALUES
    ('admin@scholarshiphub.com',
     '$2b$10$YNj9K4gbMLwEJgergQrKhOWuVrYuzeUxznT/hK1WuL3GfMIEWXcaq',
     'Ada', 'Admin',
     (SELECT id FROM roles WHERE name = 'ADMIN'),
     'ACTIVE', TRUE),
    ('reviewer@scholarshiphub.com',
     '$2b$10$Oj.qvZkKXtGEJR0iZJXtQ.oILGz6vX/0PPTxI7x6dXIFSxfhACvwS',
     'Rita', 'Reviewer',
     (SELECT id FROM roles WHERE name = 'REVIEWER'),
     'ACTIVE', TRUE),
    ('student@scholarshiphub.com',
     '$2b$10$U7v/iTRTSJOACneQ5LpmMu0iutS6RTcIrEMm9z.GPw9mzpW8DO7e6',
     'Sam', 'Student',
     (SELECT id FROM roles WHERE name = 'STUDENT'),
     'ACTIVE', TRUE);

INSERT INTO student_profiles (user_id, education_level, school, gpa, personal_statement)
SELECT id, 'UNDERGRADUATE', 'State University', 3.75,
       'Demo seed profile used to exercise the application workflow end-to-end.'
FROM users WHERE email = 'student@scholarshiphub.com';
