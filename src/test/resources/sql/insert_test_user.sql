INSERT INTO user_credentials (id, username, password, email, enabled, account_non_locked, last_password_change,
                              created_at)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'john_doe',
        '$2a$12$fM6vjTHZVL3pGK0M3MqYFuTe6973jGPXF9kD1ynktBCPaurkHod1O', 'john.doe@example.com', true, true, NOW(),
        NOW()),
       ('123e4567-e89b-12d3-a456-426614174001', 'testuser',
        '$2a$12$fM6vjTHZVL3pGK0M3MqYFuTe6973jGPXF9kD1ynktBCPaurkHod1O', 'testuser@example.com', true, true, NOW(),
        NOW());