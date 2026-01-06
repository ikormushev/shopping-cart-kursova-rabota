CREATE TABLE shopping_baskets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES app_user(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    is_shared BOOLEAN DEFAULT FALSE,
    share_code VARCHAR(10) UNIQUE,
    created_at TIMESTAMP DEFAULT NOW()
);