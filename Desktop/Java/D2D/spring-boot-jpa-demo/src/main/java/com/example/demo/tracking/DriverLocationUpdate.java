package com.example.demo.tracking;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "driver_location_updates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverLocationUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @CreationTimestamp
    @Column(name = "pinged_at", nullable = false, updatable = false)
    private Timestamp pingedAt;
}
