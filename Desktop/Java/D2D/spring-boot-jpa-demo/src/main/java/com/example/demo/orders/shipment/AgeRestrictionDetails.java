package com.example.demo.orders.shipment;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeRestrictionDetails {

    @Builder.Default
    private boolean isRestricted = false;

    private int minimumAge;

    @Builder.Default
    private boolean idCheckRequired = false;
}
