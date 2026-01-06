CREATE TABLE basket_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    basket_id UUID REFERENCES shopping_baskets(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id),
    quantity INT DEFAULT 1,
    added_by UUID REFERENCES app_user(id),
    added_at TIMESTAMP DEFAULT NOW()
);