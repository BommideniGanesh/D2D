package com.example.demo.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverLocationUpdateRepository extends JpaRepository<DriverLocationUpdate, Long> {
    List<DriverLocationUpdate> findByShipmentIdOrderByPingedAtDesc(Long shipmentId);
}
