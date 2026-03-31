package com.example.demo.rating;

import com.example.demo.orders.shipment.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "delivery_ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    private Shipment shipment;

    @Column(name = "driver_id", length = 36)
    private String driverId;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1–5

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "rated_by", nullable = false, length = 100)
    private String ratedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
}
