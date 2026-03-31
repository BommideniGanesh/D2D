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

@Entity
@Table(name = "shipment_driver_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_shipment_pickup", columnNames = { "shipment_id", "assignment_type" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDriverAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 30)
    private AssignmentType assignmentType;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Timestamp assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(name = "sequence_order", nullable = false)
    @Builder.Default
    private Integer sequenceOrder = 0;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public enum AssignmentType {
        PICKUP, DELIVERY, RETURN_PICKUP, RETURN_DELIVERY
    }

    public enum AssignmentStatus {
        ASSIGNED, ACCEPTED, COMPLETED, CANCELLED, REJECTED
    }
}
