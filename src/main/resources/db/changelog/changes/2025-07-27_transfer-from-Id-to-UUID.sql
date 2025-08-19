-- liquibase formatted sql

-- changeset pavel11sg:2.0-drop-old-tables
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS user_credentials CASCADE;

-- changeset pavel11sg:2.1-create-uuid-tables
CREATE TABLE user_credentials
(
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username             VARCHAR(50) UNIQUE             NOT NULL,
    password             VARCHAR(100)                   NOT NULL,
    email                VARCHAR(100) UNIQUE            NOT NULL,
    enabled              BOOLEAN          DEFAULT TRUE  NOT NULL,
    account_non_locked   BOOLEAN          DEFAULT TRUE  NOT NULL,
    last_password_change TIMESTAMP        DEFAULT NOW() NOT NULL,
    created_at           TIMESTAMP        DEFAULT NOW() NOT NULL
);

CREATE TABLE refresh_tokens
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID                           NOT NULL REFERENCES user_credentials (id) ON DELETE CASCADE,
    token       VARCHAR(255) UNIQUE            NOT NULL,
    device_info TEXT,
    ip_address  VARCHAR(45),
    expires_at  TIMESTAMP                      NOT NULL,
    created_at  TIMESTAMP        DEFAULT NOW() NOT NULL
);

-- changeset pavel11sg:2.2-create-indexes
CREATE INDEX idx_user_credentials_username ON user_credentials (username);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);