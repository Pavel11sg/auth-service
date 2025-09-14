
DELETE FROM user_roles;
DELETE FROM user_credentials;
DELETE FROM roles;

ALTER SEQUENCE IF EXISTS user_credentials_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS role_id_seq RESTART WITH 1;