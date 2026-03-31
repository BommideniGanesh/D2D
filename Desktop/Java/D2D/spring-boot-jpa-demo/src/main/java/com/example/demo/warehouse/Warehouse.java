package com.example.demo.warehouse;

import com.example.demo.orders.receiverdetails.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "location_code", nullable = false, unique = true, length = 20)
    private String locationCode; // e.g., "JFK-HUB", "ORD-SORT"

    @Embedded
    private Address address;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
