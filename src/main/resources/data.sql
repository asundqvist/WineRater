-- Pre-seeded admin user (password = "password")
INSERT INTO app_user (username, password, role) VALUES
    ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    role = VALUES(role);
