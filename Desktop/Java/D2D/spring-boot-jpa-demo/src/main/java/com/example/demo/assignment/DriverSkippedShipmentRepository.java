package com.example.demo.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverSkippedShipmentRepository extends JpaRepository<DriverSkippedShipment, Long> {

    /** All shipment IDs skipped by a driver (for fast filtering). */
    @Query("SELECT s.shipment.id FROM DriverSkippedShipment s WHERE s.driver.id = :driverId")
    List<Long> findSkippedShipmentIdsByDriverId(@Param("driverId") Long driverId);

    /** Check if already skipped. */
    boolean existsByDriverIdAndShipmentId(Long driverId, Long shipmentId);

    /** All skip records for a driver (for Rejected tab display). */
    @Query("SELECT s FROM DriverSkippedShipment s " +
           "JOIN FETCH s.shipment sh " +
           "WHERE s.driver.id = :driverId " +
           "ORDER BY s.skippedAt DESC")
    List<DriverSkippedShipment> findByDriverIdWithShipment(@Param("driverId") Long driverId);
}
