package com.example.demo.pricing;

import com.example.demo.orders.packagedetails.PackageDetails;
import com.example.demo.orders.shipment.ShipmentDTO;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingEngineService {

    private static final Logger logger = LoggerFactory.getLogger(PricingEngineService.class);
    private final WeatherMockService weatherMockService;

    public PricingEngineService(WeatherMockService weatherMockService) {
        this.weatherMockService = weatherMockService;
    }

    /**
     * Calculates and populates the pricing fields in the ShipmentDTO
     * before the shipment is persisted.
     */
    public void calculateAndApplyPricing(ShipmentDTO shipmentDTO, PackageDetails pkg, String originPostalCode) {
        // Base rate
        BigDecimal baseRate = new BigDecimal("10.00");
        
        // DIM Weight Calculation (L x W x H) / 5000 is standard for kg
        double volumeCm3 = pkg.getLengthCm().doubleValue() * pkg.getWidthCm().doubleValue() * pkg.getHeightCm().doubleValue();
        double dimWeight = volumeCm3 / 5000.0;
        double actualWeight = pkg.getWeightKg().doubleValue();
        
        double chargeableWeight = Math.max(dimWeight, actualWeight);
        
        // Calculate weight cost ($2 per kg of chargeable weight)
        BigDecimal weightCost = BigDecimal.valueOf(chargeableWeight * 2.0);
        
        BigDecimal subTotal = baseRate.add(weightCost);
        
        // Surge pricing based on weather at origin
        String weather = weatherMockService.getCurrentWeather(originPostalCode);
        BigDecimal surgeMultiplier = getSurgeMultiplier(weather);
        
        BigDecimal surgedTotal = subTotal.multiply(surgeMultiplier);
        
        logger.info("Pricing Engine: Base={}, WeightCost={}, DIM={}, Surge={}, Final={}", 
            baseRate, weightCost, dimWeight, surgeMultiplier, surgedTotal);
        
        // Calculate Tax (e.g., 10%)
        BigDecimal tax = surgedTotal.multiply(new BigDecimal("0.10"));
        
        // Insurance calculation if requested
        BigDecimal insurance = shipmentDTO.isInsured() ? new BigDecimal("15.00") : BigDecimal.ZERO;
        
        // Assign values to DTO
        shipmentDTO.setBaseShippingCost(surgedTotal.setScale(2, RoundingMode.HALF_UP));
        shipmentDTO.setTaxAmount(tax.setScale(2, RoundingMode.HALF_UP));
        shipmentDTO.setInsuranceAmount(insurance);
        shipmentDTO.setDiscountAmount(BigDecimal.ZERO); // Could integrate PromoEngine here
        
        BigDecimal total = shipmentDTO.getBaseShippingCost()
            .add(shipmentDTO.getTaxAmount())
            .add(shipmentDTO.getInsuranceAmount())
            .subtract(shipmentDTO.getDiscountAmount());
            
        shipmentDTO.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }
    
    private BigDecimal getSurgeMultiplier(String weather) {
        return switch (weather) {
            case "RAIN" -> new BigDecimal("1.2");
            case "SNOW" -> new BigDecimal("1.5");
            case "STORM" -> new BigDecimal("1.8");
            default -> new BigDecimal("1.0"); // CLEAR or unknown
        };
    }
}
