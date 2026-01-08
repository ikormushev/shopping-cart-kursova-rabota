CREATE TABLE basket_items (
    id VARCHAR(255) PRIMARY KEY,
    basket_id VARCHAR(255) REFERENCES shopping_baskets(id) ON DELETE CASCADE,
    product_id VARCHAR(255) REFERENCES products(id),
    quantity INT DEFAULT 1,
    added_by VARCHAR(255) REFERENCES app_user(id),
    added_at TIMESTAMP DEFAULT NOW()
);