CREATE TABLE fulfilment_assignment
(
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    store_id     BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    CONSTRAINT uq_assignment UNIQUE (store_id, product_id, warehouse_id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    FOREIGN KEY (product_id) REFERENCES product (id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouse (id)
);