CREATE TABLE app_user (
    id VARCHAR(255) PRIMARY KEY NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    unique_code VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);