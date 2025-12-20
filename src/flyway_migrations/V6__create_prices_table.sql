CREATE TABLE prices (
    id         UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    store_id   UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    price      DECIMAL(10, 2) NOT NULL,
    currency   VARCHAR(3) DEFAULT 'BGN', -- Добавено поле за валута
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() -- Променено име от timestamp на created_at
);
