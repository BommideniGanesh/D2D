ALTER TABLE shipments
ADD COLUMN user_id VARCHAR(36);
ALTER TABLE shipments
ADD CONSTRAINT fk_shipments_user FOREIGN KEY (user_id) REFERENCES users(id);