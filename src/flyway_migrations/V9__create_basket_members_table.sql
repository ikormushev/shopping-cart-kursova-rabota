CREATE TABLE basket_members (
    basket_id UUID REFERENCES shopping_baskets(id) ON DELETE CASCADE,
    user_id UUID REFERENCES app_user(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL, -- 'OWNER', 'CONTRIBUTOR'
    PRIMARY KEY (basket_id, user_id)
);