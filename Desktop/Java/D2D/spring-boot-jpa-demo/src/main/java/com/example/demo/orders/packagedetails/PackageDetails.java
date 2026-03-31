package com.example.demo.orders.packagedetails;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "package_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PackageDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_count", nullable = false)
    private Integer packageCount;

    @Column(name = "box_id", nullable = false, length = 50)
    private String boxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "box_type", nullable = false)
    private BoxType boxType;

    @Column(name = "length_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", nullable = false, precision = 6, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "fragile", nullable = false)
    @Builder.Default
    private boolean fragile = false;

    @Column(name = "hazardous_material", nullable = false)
    @Builder.Default
    private boolean hazardousMaterial = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "packaging_type", nullable = false)
    private PackagingType packagingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "seal_type")
    private SealType sealType;

    @Column(name = "handling_instructions")
    private String handlingInstructions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
