-- ============================================================================
-- V4: Work Order Status History, Material Stock Reservation and Item Snapshots
-- ============================================================================

-- 1. Status History Table for Auditing & Metrics
CREATE TABLE IF NOT EXISTS work_order_status_history (
    id UUID PRIMARY KEY,
    work_order_id UUID NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    changed_by VARCHAR(100),
    CONSTRAINT fk_wosh_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wosh_work_order_id ON work_order_status_history (work_order_id);
CREATE INDEX IF NOT EXISTS idx_wosh_changed_at ON work_order_status_history (changed_at);

-- 2. Material Stock Reservation
ALTER TABLE material
    ADD COLUMN IF NOT EXISTS reserved_quantity INTEGER NOT NULL DEFAULT 0;

-- 3. Historical Item Snapshots in Work Order Items (Prevent Catalog Edits From Altering Closed History)
ALTER TABLE work_order_service
    ADD COLUMN IF NOT EXISTS item_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS item_code VARCHAR(50);

ALTER TABLE work_order_material
    ADD COLUMN IF NOT EXISTS item_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS item_sku VARCHAR(50);

-- Populate existing rows with catalog names if any
UPDATE work_order_service wos
SET item_name = s.name, item_code = s.code
FROM service s
WHERE wos.service_id = s.id AND wos.item_name IS NULL;

UPDATE work_order_material wom
SET item_name = m.name, item_sku = m.sku
FROM material m
WHERE wom.material_id = m.id AND wom.item_name IS NULL;

-- 4. Drop Redundant Indexes Over UNIQUE Constraint Columns
DROP INDEX IF EXISTS idx_customer_document;
DROP INDEX IF EXISTS idx_customer_email;
DROP INDEX IF EXISTS idx_vehicle_license_plate;
DROP INDEX IF EXISTS idx_material_sku;
DROP INDEX IF EXISTS idx_service_code;
