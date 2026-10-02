CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    document VARCHAR(14) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_customer_document ON customer (document);
CREATE INDEX IF NOT EXISTS idx_customer_email ON customer (email);

CREATE TABLE IF NOT EXISTS vehicle (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    license_plate VARCHAR(10) NOT NULL UNIQUE,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    manufacture_year INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_vehicle_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_vehicle_customer_id ON vehicle (customer_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_license_plate ON vehicle (license_plate);
