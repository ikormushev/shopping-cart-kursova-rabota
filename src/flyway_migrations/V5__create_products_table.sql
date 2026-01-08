    CREATE TABLE products (
        id VARCHAR(255) PRIMARY KEY,
        group_id VARCHAR(255) REFERENCES product_groups(id),
        store_id VARCHAR(255) REFERENCES stores(id),
        raw_name VARCHAR(255) NOT NULL,
        sku VARCHAR(100),
        price DECIMAL(10, 2) NOT NULL,
        currency VARCHAR(3) DEFAULT 'BGN',
        created_at TIMESTAMP DEFAULT NOW()
    );