CREATE TABLE product_groups (
    id VARCHAR(255) PRIMARY KEY,
    canonical_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    image_url VARCHAR(500)
);