-- =====================================================
-- COMPLETE DATABASE SCHEMA REFERENCE (D2D)
-- =====================================================
-- This file represents the fully consolidated schema as of the latest version.
-- It includes:
-- 1. Users & Roles
-- 2. Sender, Receiver, Package Details
-- 3. Shipments (with updated VARCHAR lengths and removed CHECK constraints)
-- 4. Driver Profiles & Service Areas
-- 5. Shipment Assignments
-- =====================================================
-- 1. USER MANAGEMENT
-- =====================================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    accepted_terms BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
-- =====================================================
-- 2. ORDER ENTITIES
-- =====================================================
CREATE TABLE IF NOT EXISTS sender_details (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS receiver_details (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    delivery_instructions TEXT,
    is_residential BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS package_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_count INT NOT NULL,
    box_id VARCHAR(50) NOT NULL,
    box_type VARCHAR(50) NOT NULL,
    -- Stored as String (ENUM handled by App)
    length_cm DECIMAL(6, 2) NOT NULL,
    width_cm DECIMAL(6, 2) NOT NULL,
    height_cm DECIMAL(6, 2) NOT NULL,
    weight_kg DECIMAL(6, 2) NOT NULL,
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    hazardous_material BOOLEAN NOT NULL DEFAULT FALSE,
    packaging_type VARCHAR(50) NOT NULL,
    -- Stored as String
    seal_type VARCHAR(50),
    -- Stored as String
    handling_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- =====================================================
-- 3. SHIPMENTS
-- =====================================================
CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id VARCHAR(36) NOT NULL,
    receiver_id VARCHAR(36) NOT NULL,
    package_id BIGINT NOT NULL,
    base_shipping_cost DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    insurance_amount DECIMAL(10, 2),
    discount_amount DECIMAL(10, 2),
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    -- Note: VARCHAR(50) used here to be flexible and avoid "Data truncated" errors.
    -- App logic enforces specific Enum values.
    payment_mode VARCHAR(50) NOT NULL,
    source VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    insured BOOLEAN NOT NULL DEFAULT FALSE,
    insurance_provider VARCHAR(100),
    signature_required BOOLEAN NOT NULL DEFAULT FALSE,
    age_restricted_delivery BOOLEAN NOT NULL DEFAULT FALSE,
    tracking_number VARCHAR(100) UNIQUE NOT NULL,
    history JSON,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shipments_sender FOREIGN KEY (sender_id) REFERENCES sender_details(id),
    CONSTRAINT fk_shipments_receiver FOREIGN KEY (receiver_id) REFERENCES receiver_details(id),
    CONSTRAINT fk_shipments_package FOREIGN KEY (package_id) REFERENCES package_details(id)
);
-- =====================================================
-- 4. DRIVER SYSTEM
-- =====================================================
CREATE TABLE IF NOT EXISTS driver_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    license_type VARCHAR(50),
    license_expiry_date DATE NOT NULL,
    vehicle_number VARCHAR(50) NOT NULL UNIQUE,
    vehicle_type VARCHAR(50) NOT NULL,
    vehicle_model VARCHAR(100),
    vehicle_color VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    availability_status VARCHAR(30) NOT NULL,
    -- AVAILABLE, ON_DELIVERY, OFFLINE
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_driver_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
-- NEW: Driver Service Areas (Option 1 Implementation)
CREATE TABLE IF NOT EXISTS driver_service_areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    CONSTRAINT fk_area_driver FOREIGN KEY (driver_id) REFERENCES driver_profiles(id) ON DELETE CASCADE,
    UNIQUE (driver_id, pincode)
);
CREATE INDEX idx_area_pincode ON driver_service_areas(pincode);
-- =====================================================
-- 5. ASSIGNMENTS
-- =====================================================
CREATE TABLE IF NOT EXISTS shipment_driver_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    assignment_type VARCHAR(30) NOT NULL,
    -- PICKUP, DELIVERY
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    -- ASSIGNED, ACCEPTED, COMPLETED, CANCELLED
    CONSTRAINT uq_shipment_pickup UNIQUE (shipment_id, assignment_type),
    CONSTRAINT fk_assignment_shipment FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_driver FOREIGN KEY (driver_id) REFERENCES driver_profiles(id) ON DELETE CASCADE
);