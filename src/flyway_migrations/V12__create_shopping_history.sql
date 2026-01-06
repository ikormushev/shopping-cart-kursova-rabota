CREATE TABLE shopping_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES app_user(id),
    basket_id UUID,
    basket_name VARCHAR(255),
    total_spent DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'BGN',
    closed_at TIMESTAMP DEFAULT NOW()
);
CREATE TABLE shopping_history_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    history_id UUID REFERENCES shopping_history(id) ON DELETE CASCADE,
    product_name VARCHAR(255),
    quantity INT,
    price_at_purchase DECIMAL(10, 2)
);