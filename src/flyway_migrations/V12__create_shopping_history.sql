CREATE TABLE shopping_history (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) REFERENCES app_user(id),
    basket_id VARCHAR(255),
    basket_name VARCHAR(255),
    total_spent DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'BGN',
    closed_at TIMESTAMP DEFAULT NOW()
);
CREATE TABLE shopping_history_items (
    id VARCHAR(255) PRIMARY KEY,
    history_id VARCHAR(255) REFERENCES shopping_history(id) ON DELETE CASCADE,
    product_name VARCHAR(255),
    quantity INT,
    price_at_purchase DECIMAL(10, 2)
);