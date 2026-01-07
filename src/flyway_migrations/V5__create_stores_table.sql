CREATE TABLE stores (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    website_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW()
);