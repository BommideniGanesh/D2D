package com.example.demo.driver;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_service_areas", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "driver_id", "pincode" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverServiceArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DriverProfile driver;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;
}
