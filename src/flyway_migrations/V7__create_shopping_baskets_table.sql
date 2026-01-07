CREATE TABLE shopping_baskets (
    id VARCHAR(255) PRIMARY KEY,
    owner_id VARCHAR(255) REFERENCES app_user(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,

    share_code VARCHAR(10) UNIQUE,
    created_at TIMESTAMP DEFAULT NOW()
);