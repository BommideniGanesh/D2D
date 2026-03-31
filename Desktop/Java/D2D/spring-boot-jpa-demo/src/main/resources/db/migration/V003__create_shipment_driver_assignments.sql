-- =====================================================
-- Driver Assignment System - Database Migration
-- =====================================================
-- This script creates the shipment_driver_assignments table
-- for managing pickup and delivery driver assignments
CREATE TABLE shipment_driver_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    assignment_type VARCHAR(30) NOT NULL CHECK (assignment_type IN ('PICKUP', 'DELIVERY')),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL CHECK (
        status IN ('ASSIGNED', 'ACCEPTED', 'COMPLETED', 'CANCELLED')
    ),
    -- Unique constraint: one shipment can only have one pickup assignment
    CONSTRAINT uq_shipment_pickup UNIQUE (shipment_id, assignment_type),
    -- Foreign key to shipments table
    CONSTRAINT fk_assignment_shipment FOREIGN KEY (shipment_id) REFERENCES shipments(id) ON DELETE CASCADE,
    -- Foreign key to driver_profiles table
    CONSTRAINT fk_assignment_driver FOREIGN KEY (driver_id) REFERENCES driver_profiles(id) ON DELETE CASCADE
);
-- Indexes for performance optimization
CREATE INDEX idx_assignment_shipment ON shipment_driver_assignments(shipment_id);
CREATE INDEX idx_assignment_driver ON shipment_driver_assignments(driver_id);
CREATE INDEX idx_assignment_status ON shipment_driver_assignments(status);
CREATE INDEX idx_assignment_type ON shipment_driver_assignments(assignment_type);
CREATE INDEX idx_assignment_assigned_at ON shipment_driver_assignments(assigned_at);
-- =====================================================
-- Update shipments table to support new status
-- =====================================================
-- Note: If your shipments table already has data, you may need to
-- handle existing records differently. This assumes the status column
-- uses VARCHAR/ENUM and can accommodate the new value.
-- For MySQL ENUM columns, you would need:
-- ALTER TABLE shipments MODIFY COLUMN status 
--     ENUM('CREATED', 'PICKUP_ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') 
--     NOT NULL;
-- For VARCHAR columns (which is what we're using), no change needed
-- The application will handle the new status value
-- =====================================================
-- Verification Queries
-- =====================================================
-- Check table structure
-- DESCRIBE shipment_driver_assignments;
-- Check constraints
-- SELECT * FROM information_schema.TABLE_CONSTRAINTS 
-- WHERE TABLE_NAME = 'shipment_driver_assignments';
-- Check indexes
-- SHOW INDEX FROM shipment_driver_assignments;