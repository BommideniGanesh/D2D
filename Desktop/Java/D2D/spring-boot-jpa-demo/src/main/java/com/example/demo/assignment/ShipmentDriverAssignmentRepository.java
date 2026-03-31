package com.example.demo.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentDriverAssignmentRepository extends JpaRepository<ShipmentDriverAssignment, Long> {

  /**
   * Batch assignment query that matches shipments with drivers based on pincode.
   * Only assigns to active, verified, available drivers.
   * Prevents duplicate assignments using LEFT JOIN.
   */
  @Modifying
  @Query(value = """
      INSERT INTO shipment_driver_assignments (
          shipment_id, driver_id, assignment_type, status, assigned_at
      )
      SELECT s.id, d.id, 'PICKUP', 'ASSIGNED', NOW()
      FROM shipments s
      JOIN sender_details sd ON s.sender_id = sd.id
      JOIN driver_profiles d ON d.pincode = sd.postal_code
      LEFT JOIN shipment_driver_assignments a
          ON a.shipment_id = s.id
          AND a.assignment_type = 'PICKUP'
      WHERE s.status = 'CREATED'
        AND d.is_active = TRUE
        AND d.is_verified = TRUE
        AND d.availability_status = 'AVAILABLE'
        AND a.id IS NULL
      ORDER BY s.created_at
      LIMIT :batchSize
      """, nativeQuery = true)
  int assignPickupDrivers(@Param("batchSize") int batchSize);

  /**
   * Call the stored procedure to assign orders.
   */
  @Modifying
  @Query(value = "CALL assign_orders_to_drivers_proc(:batchSize)", nativeQuery = true)
  void callAssignOrdersProc(@Param("batchSize") int batchSize);

  /**
   * Find assignment by shipment and type to check if already assigned.
   */
  @Query("SELECT a FROM ShipmentDriverAssignment a " +
      "WHERE a.shipment.id = :shipmentId " +
      "AND a.assignmentType = :assignmentType")
  Optional<ShipmentDriverAssignment> findByShipmentIdAndAssignmentType(
      @Param("shipmentId") Long shipmentId,
      @Param("assignmentType") ShipmentDriverAssignment.AssignmentType assignmentType);

  /**
   * Find all assignments for a specific shipment.
   */
  @Query("SELECT a FROM ShipmentDriverAssignment a " +
      "WHERE a.shipment.id = :shipmentId " +
      "ORDER BY a.assignedAt DESC")
  List<ShipmentDriverAssignment> findByShipmentId(@Param("shipmentId") Long shipmentId);

  /**
   * Find all assignments for a specific driver.
   */
  @Query("SELECT a FROM ShipmentDriverAssignment a " +
      "WHERE a.driver.id = :driverId " +
      "ORDER BY a.assignedAt DESC")
  List<ShipmentDriverAssignment> findByDriverId(@Param("driverId") Long driverId);

  /**
   * Get IDs of drivers who were just assigned (for status update).
   */
  @Query(value = """
      SELECT DISTINCT driver_id
      FROM shipment_driver_assignments
      WHERE status = 'ASSIGNED'
        AND assignment_type = 'PICKUP'
        AND assigned_at >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
      """, nativeQuery = true)
  List<Long> getRecentlyAssignedDriverIds();

  /**
   * Get IDs of shipments that were just assigned (for status update).
   */
  @Query(value = """
      SELECT DISTINCT shipment_id
      FROM shipment_driver_assignments
      WHERE status = 'ASSIGNED'
        AND assignment_type = 'PICKUP'
        AND assigned_at >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
      """, nativeQuery = true)
  List<Long> getRecentlyAssignedShipmentIds();
}
