-- changeset pavel11sg:3.1-create-table-roles
CREATE TABLE roles
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(20) NOT NULL UNIQUE
);
-- rollback DROP TABLE roles;

-- changeset pavel11sg:3.2-insert-default-roles
INSERT INTO roles (id, name)
VALUES (gen_random_uuid(), 'USER'),
       (gen_random_uuid(), 'ADMIN')
ON CONFLICT (name) DO NOTHING;
-- rollback DELETE FROM roles WHERE name IN ('USER', 'ADMIN');

-- changeset pavel11sg:3.3-create-user-roles-junction-table
CREATE TABLE user_roles
(
    user_id UUID REFERENCES user_credentials (id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
-- rollback DROP TABLE user_roles;

-- changeset pavel11sg:3.4-assign-default-role-to-existing-users
INSERT INTO user_roles (user_id, role_id)
SELECT uc.id, r.id
FROM user_credentials uc
         CROSS JOIN roles r
WHERE r.name = 'USER';
-- rollback DELETE FROM user_roles;

-- changeset pavel11sg:3.5-create-admin-user
WITH new_admin AS (
    INSERT INTO user_credentials (id, username, password, email)
        VALUES (gen_random_uuid(), 'admin', '$2a$12$XigP5inYFi1BMzqoo7ndquOjsJCDbHA32QzQUq2wMUgTGV44gq0FG',
                'admin@example.com')
        ON CONFLICT (username) DO NOTHING
        RETURNING id)
INSERT
INTO user_roles (user_id, role_id)
SELECT na.id, r.id
FROM new_admin na
         CROSS JOIN roles r
WHERE r.name IN ('USER', 'ADMIN');
-- rollback DELETE FROM user_credentials WHERE username = 'admin';