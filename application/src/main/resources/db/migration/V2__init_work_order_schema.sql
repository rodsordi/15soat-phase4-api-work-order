CREATE TABLE IF NOT EXISTS work_order (
    id UUID PRIMARY KEY,
    customer_id UUID,
    vehicle_id UUID,
    customer_document VARCHAR(14) NOT NULL,
    license_plate VARCHAR(10) NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    CONSTRAINT fk_work_order_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE SET NULL,
    CONSTRAINT fk_work_order_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_work_order_status ON work_order (status);
CREATE INDEX IF NOT EXISTS idx_work_order_customer_document ON work_order (customer_document);
CREATE INDEX IF NOT EXISTS idx_work_order_license_plate ON work_order (license_plate);
CREATE INDEX IF NOT EXISTS idx_work_order_customer_id ON work_order (customer_id);
CREATE INDEX IF NOT EXISTS idx_work_order_vehicle_id ON work_order (vehicle_id);
