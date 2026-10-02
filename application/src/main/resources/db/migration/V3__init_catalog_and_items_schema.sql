CREATE TABLE IF NOT EXISTS material (
    id UUID PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    unit_cost NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    unit_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    min_stock INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_material_sku ON material (sku);
CREATE INDEX IF NOT EXISTS idx_material_name ON material (name);

CREATE TABLE IF NOT EXISTS service (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    estimated_minutes BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_service_code ON service (code);
CREATE INDEX IF NOT EXISTS idx_service_name ON service (name);

CREATE TABLE IF NOT EXISTS work_order_service (
    id UUID PRIMARY KEY,
    work_order_id UUID NOT NULL,
    service_id UUID NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_wos_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_wos_service FOREIGN KEY (service_id) REFERENCES service (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_wos_work_order_id ON work_order_service (work_order_id);
CREATE INDEX IF NOT EXISTS idx_wos_service_id ON work_order_service (service_id);

CREATE TABLE IF NOT EXISTS work_order_material (
    id UUID PRIMARY KEY,
    work_order_id UUID NOT NULL,
    material_id UUID NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_wom_work_order FOREIGN KEY (work_order_id) REFERENCES work_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_wom_material FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_wom_work_order_id ON work_order_material (work_order_id);
CREATE INDEX IF NOT EXISTS idx_wom_material_id ON work_order_material (material_id);
