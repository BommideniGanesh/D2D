package com.example.demo.warehouse;

import com.example.demo.orders.shipment.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transit_bags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitBag {

    public enum BagStatus {
        OPEN,       // Currently accepting shipments
        SEALED,     // Locked, ready for transport
        IN_TRANSIT, // Moving between warehouses
        RECEIVED,   // Arrived at destination hub
        UNPACKED    // Emptied, shipments are free
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bag_barcode", nullable = false, unique = true, length = 50)
    private String bagBarcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_warehouse_id", nullable = false)
    private Warehouse originWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BagStatus status = BagStatus.OPEN;

    @OneToMany(mappedBy = "transitBag", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Shipment> shipments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "sealed_at")
    private Timestamp sealedAt;
}
