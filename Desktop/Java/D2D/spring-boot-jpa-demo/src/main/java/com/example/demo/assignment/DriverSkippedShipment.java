package com.example.demo.assignment;

import com.example.demo.driver.DriverProfile;
import com.example.demo.orders.shipment.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * Tracks which shipments a driver has explicitly dismissed ("Not Interested").
 * Persisted so dismissals survive page refreshes.
 */
@Entity
@Table(name = "driver_skipped_shipments",
       uniqueConstraints = @UniqueConstraint(name = "uq_driver_shipment_skip",
               columnNames = {"driver_id", "shipment_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverSkippedShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @CreationTimestamp
    @Column(name = "skipped_at", nullable = false, updatable = false)
    private Timestamp skippedAt;
}
