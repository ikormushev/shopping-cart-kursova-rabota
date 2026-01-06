CREATE TABLE session (
    session_id VARCHAR(50) PRIMARY KEY,
    status VARCHAR(255),
    cart_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
);