-- liquibase formatted sql

-- changeset pavel11sg:1.1-create_user_credentials_table
CREATE TABLE user_credentials
(
    id                   BIGSERIAL PRIMARY KEY,
    username             VARCHAR(50) UNIQUE      NOT NULL,
    password             VARCHAR(100)            NOT NULL,
    email                VARCHAR(100) UNIQUE     NOT NULL,
    enabled              BOOLEAN   DEFAULT TRUE  NOT NULL,
    account_non_locked   BOOLEAN   DEFAULT TRUE  NOT NULL,
    last_password_change TIMESTAMP DEFAULT NOW() NOT NULL,
    created_at           TIMESTAMP DEFAULT NOW() NOT NULL
);
-- rollback drop table users cascade;

-- changeset pavel11sg:1.2-create_user_credentials_indexes
CREATE INDEX idx_user_credentials_username ON user_credentials (username);
--rollback drop index if exists idx_user_credentials_username;

-- changeset pavel11sg:1.3-create_refresh_tokens_table
CREATE TABLE refresh_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT                  NOT NULL REFERENCES user_credentials (id) ON DELETE CASCADE,
    token       VARCHAR(255) UNIQUE     NOT NULL,
    device_info TEXT,
    ip_address  VARCHAR(45),
    expires_at  TIMESTAMP               NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW() NOT NULL
);
-- rollback drop table refresh_tokens cascade;

-- changeset pavel11sg:1.4-create_refresh_tokens_indexes
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
--rollback drop index if exists idx_refresh_tokens_user_id;
--rollback drop index if exists idx_refresh_tokens_expires_at;