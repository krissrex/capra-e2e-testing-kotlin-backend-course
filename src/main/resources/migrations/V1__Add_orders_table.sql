-- Orders table
CREATE TABLE orders
(
  id   TEXT PRIMARY KEY,
  data JSONB
);

-- Avoid multiple orders with the same ID
CREATE UNIQUE INDEX ON orders ((data ->> 'orderId'));

-- Search based on customer id
CREATE INDEX order_customer_gin_index ON orders USING gin ((data -> 'customer' ->> 'id') jsonb_path_ops);
--CREATE INDEX order_customer_index ON orders ((data -> 'customer' -> 'id'));
