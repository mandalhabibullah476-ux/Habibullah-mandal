-- ==============================================================
-- OMNIFEAST PRODUCTION DATABASE SCHEMA (POSTGRESQL 16 + POSTGIS 3.4)
-- Hyperlocal 10-Minute Dark Store & Delivery Platform
-- ==============================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. ENUMS
CREATE TYPE user_role_enum AS ENUM (
    'FOUNDER',
    'SUPER_ADMIN',
    'ADMIN',
    'MANAGER',
    'STORE_MANAGER',
    'STORE_STAFF',
    'DELIVERY_PARTNER',
    'CUSTOMER'
);

CREATE TYPE tracking_status_enum AS ENUM (
    'ACTIVE_DELIVERY',
    'ONLINE_IDLE',
    'OFFLINE',
    'LOCATION_DISABLED',
    'STALE'
);

CREATE TYPE order_status_enum AS ENUM (
    'PLACED',
    'PAYMENT_CONFIRMED',
    'STORE_ACCEPTED',
    'PICKING',
    'PACKED',
    'READY_FOR_PICKUP',
    'DRIVER_ASSIGNED',
    'PICKED_UP',
    'OUT_FOR_DELIVERY',
    'DELIVERED',
    'CANCELLED'
);

-- 3. USERS & AUTHENTICATION TABLE
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(150) NOT NULL,
    avatar_url TEXT,
    role user_role_enum NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DEACTIVATED, LOCKED
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    failed_login_attempts INT DEFAULT 0,
    lockout_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. ROLES & GRANULAR PERMISSIONS (RBAC)
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(100) UNIQUE NOT NULL, -- e.g. 'LIVE_LOCATION_VIEW', 'REFUND_MANAGE', 'STORE_ASSIGN'
    name VARCHAR(150) NOT NULL,
    module VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role user_role_enum NOT NULL,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role, permission_id)
);

-- 5. SESSIONS & DEVICE REVOCATION
CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    device_info TEXT,
    ip_address INET,
    is_revoked BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sessions_user_active ON sessions(user_id) WHERE is_revoked = FALSE;

-- 6. DARK STORES & URBAN MICRO-FULFILLMENT HUBS
CREATE TABLE IF NOT EXISTS stores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL, -- e.g. 'STORE-04'
    name VARCHAR(150) NOT NULL,
    manager_id UUID REFERENCES users(id),
    address TEXT NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL, -- PostGIS WGS84 Point
    service_radius_meters INT NOT NULL DEFAULT 3500, -- 3.5 km default 10-min SLA
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_stores_spatial ON stores USING GIST(location);

-- 7. GEOFENCES & SERVICE OPERATIONAL ZONES
CREATE TABLE IF NOT EXISTS geofence_zones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    zone_type VARCHAR(50) NOT NULL, -- 'SERVICE_AREA', 'RESTRICTED_ZONE', 'MANAGER_AREA'
    store_id UUID REFERENCES stores(id) ON DELETE CASCADE,
    boundary GEOMETRY(Polygon, 4326) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_geofence_boundary ON geofence_zones USING GIST(boundary);

-- 8. USER LOCATIONS (HIGH-FREQUENCY ADAPTIVE RETENTION)
CREATE TABLE IF NOT EXISTS user_locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    location GEOMETRY(Point, 4326) NOT NULL,
    accuracy_meters NUMERIC(6,2),
    speed_mps NUMERIC(6,2),
    heading_deg NUMERIC(5,2),
    captured_at TIMESTAMPTZ NOT NULL,
    session_id UUID REFERENCES sessions(id),
    tracking_status tracking_status_enum NOT NULL DEFAULT 'ONLINE_IDLE',
    is_current BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
-- Spatial Index for Fast Geographic Queries (Nearest Neighbor, Distance Checks)
CREATE INDEX idx_user_locations_spatial ON user_locations USING GIST(location);
CREATE INDEX idx_user_locations_current ON user_locations(user_id) WHERE is_current = TRUE;
CREATE INDEX idx_user_locations_captured ON user_locations(captured_at DESC);

-- 9. IMMUTABLE CRYPTOGRAPHIC AUDIT LOGS
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actor_user_id UUID NOT NULL REFERENCES users(id),
    actor_role user_role_enum NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(150) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    reason TEXT NOT NULL,
    ip_address INET,
    session_id UUID,
    checksum VARCHAR(64) NOT NULL, -- SHA-256 integrity hash
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);

-- ==============================================================
-- POSTGIS SPATIAL HELPER FUNCTIONS & GEOGRAPHIC QUERIES
-- ==============================================================

-- Check if point is inside Dark Store delivery radius (10-min SLA)
CREATE OR REPLACE FUNCTION is_within_store_radius(
    store_id UUID,
    lat NUMERIC,
    lng NUMERIC
) RETURNS BOOLEAN AS $$
DECLARE
    store_pt GEOMETRY;
    radius_m INT;
BEGIN
    SELECT location, service_radius_meters INTO store_pt, radius_m 
    FROM stores WHERE id = store_id;
    
    RETURN ST_DWithin(
        store_pt::geography, 
        ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography, 
        radius_m
    );
END;
$$ LANGUAGE plpgsql STABLE;

-- Prune historical location trails older than retention policy (e.g. 7 days)
CREATE OR REPLACE FUNCTION prune_historical_locations(retention_days INT) 
RETURNS INT AS $$
DECLARE
    deleted_count INT;
BEGIN
    DELETE FROM user_locations
    WHERE is_current = FALSE 
      AND captured_at < NOW() - (retention_days || ' days')::INTERVAL;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;
