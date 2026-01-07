CREATE TABLE basket_members (
    basket_id VARCHAR(255) REFERENCES shopping_baskets(id) ON DELETE CASCADE,
    user_id VARCHAR(255) REFERENCES app_user(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL, -- 'OWNER', 'CONTRIBUTOR'
    PRIMARY KEY (basket_id, user_id)
);