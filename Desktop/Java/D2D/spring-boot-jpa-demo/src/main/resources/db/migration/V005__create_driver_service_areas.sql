-- =====================================================
-- Driver Service Areas - Database Migration
-- =====================================================
-- This script creates the driver_service_areas table
-- to allow drivers to serve multiple pincodes.
CREATE TABLE driver_service_areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    -- Foreign Key to driver_profiles
    CONSTRAINT fk_area_driver FOREIGN KEY (driver_id) REFERENCES driver_profiles(id) ON DELETE CASCADE,
    -- Ensure a driver doesn't have duplicate entries for the same pincode
    UNIQUE (driver_id, pincode)
);
-- Index for fast lookup by pincode (critical for assignment query)
CREATE INDEX idx_area_pincode ON driver_service_areas(pincode);