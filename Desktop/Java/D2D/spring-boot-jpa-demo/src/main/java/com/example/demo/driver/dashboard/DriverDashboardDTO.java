package com.example.demo.driver.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDashboardDTO {

    private Long assignmentId;
    private String assignmentStatus;
    private String assignmentType;
    private Timestamp assignedAt;

    // Shipment Details
    private Long shipmentId;
    private String trackingNumber;
    private String shipmentStatus;
    private BigDecimal totalAmount;
    private String paymentMode;
    private boolean signatureRequired;

    // Sender Details (Pickup Location)
    private String senderName;
    private String senderPhone;
    private String pickupAddress;
    private String pickupCity;
    private String pickupState;
    private String pickupPostalCode;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;

    // Receiver Details (Delivery Location)
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryPostalCode;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    // Package Details
    private String packageDescription;
    private Double weight;
    private String dimensions; // L x W x H

    // Proof of Delivery (populated for COMPLETED DELIVERY assignments)
    private String podImageUrl;
    private String podStatus; // PENDING, PASSED, FAILED
}
