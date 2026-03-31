package com.example.demo.delivery;

import com.example.demo.orders.shipment.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "proof_of_delivery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProofOfDelivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ml_validation_status", nullable = false, length = 30)
    @Builder.Default
    private MLValidationStatus mlValidationStatus = MLValidationStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Timestamp timestamp;

    public enum MLValidationStatus {
        PENDING, PASSED, FAILED
    }
}
