package com.example.demo.orders.shipment;

import com.example.demo.orders.packagedetails.PackageDetails;
import com.example.demo.orders.receiverdetails.ReceiverDetails;
import com.example.demo.orders.senderdetails.SenderDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDTO {

    private Long id;
    private String senderId;
    private String receiverId;
    private Long packageId;
    private String userId;

    // Details for response
    private SenderDetails senderDetails;
    private ReceiverDetails receiverDetails;
    private PackageDetails packageDetails;

    private BigDecimal baseShippingCost;
    private BigDecimal taxAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;

    private Shipment.PaymentMode paymentMode;
    private boolean insured;
    private String insuranceProvider;
    private boolean signatureRequired;
    private AgeRestrictionDetails ageRestrictionDetails;

    private String trackingNumber;
    private Shipment.ShipmentStatus status;
    private Timestamp lastUpdated;
    private List<Map<String, Object>> history;

    private String createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Shipment.ShipmentSource source;
}
