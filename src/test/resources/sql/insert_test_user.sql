
INSERT INTO roles (id, name) VALUES
                                 ('11111111-1111-1111-1111-111111111111', 'USER'),
                                 ('22222222-2222-2222-2222-222222222222', 'ADMIN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_credentials (id, username, password, email, enabled, account_non_locked, last_password_change, created_at)
VALUES
    ('123e4567-e89b-12d3-a456-426614174001', 'testuser', '$2a$12$ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345', 'testuser@example.com', true, true, NOW(), NOW()),
    ('123e4567-e89b-12d3-a456-426614174000', 'john_doe', '$2a$12$MxzlpquiIIupoQcTeHt89.OBHg19TjSBOddkmEwDEjlVqLlGDGIdS', 'john.doe@example.com', true, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
                               username = EXCLUDED.username,
                               password = EXCLUDED.password,
                               email = EXCLUDED.email,
                               enabled = EXCLUDED.enabled,
                               account_non_locked = EXCLUDED.account_non_locked,
                               last_password_change = EXCLUDED.last_password_change,
                               created_at = EXCLUDED.created_at;

INSERT INTO user_roles (user_id, role_id)
VALUES
    ('123e4567-e89b-12d3-a456-426614174001', '11111111-1111-1111-1111-111111111111'),
    ('123e4567-e89b-12d3-a456-426614174000', '11111111-1111-1111-1111-111111111111')
ON CONFLICT (user_id, role_id) DO NOTHING;