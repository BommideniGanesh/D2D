package com.example.demo.orders.packagedetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageDetailsDTO {

    private Long id;
    private Integer packageCount;
    private String boxId;
    private BoxType boxType;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private boolean fragile;
    private boolean hazardousMaterial;
    private PackagingType packagingType;
    private SealType sealType;
    private String handlingInstructions;
}
