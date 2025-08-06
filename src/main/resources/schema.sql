-- Drop existing tables if they exist (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS user_document_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- Create roles table first (referenced by user_roles)
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(20) UNIQUE NOT NULL
);

-- Create users table
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       google_sub VARCHAR(100) UNIQUE,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL
);

-- Create user_roles junction table (many-to-many relationship)
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create user_document_permissions table
CREATE TABLE user_document_permissions (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           user_id BIGINT NOT NULL,
                                           document_id BIGINT NOT NULL,
                                           role VARCHAR(20) NOT NULL,
                                           UNIQUE (user_id, document_id),
                                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

