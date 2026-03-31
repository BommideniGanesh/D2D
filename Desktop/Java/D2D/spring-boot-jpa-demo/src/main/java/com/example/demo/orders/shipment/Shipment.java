package com.example.demo.orders.shipment;

import com.example.demo.orders.packagedetails.PackageDetails;
import com.example.demo.orders.receiverdetails.ReceiverDetails;
import com.example.demo.orders.senderdetails.SenderDetails;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private SenderDetails sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private ReceiverDetails receiver;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PackageDetails packageDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transit_bag_id")
    private com.example.demo.warehouse.TransitBag transitBag;

    @Column(name = "base_shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseShippingCost;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "insurance_amount", precision = 10, scale = 2)
    private BigDecimal insuranceAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 50)
    private PaymentMode paymentMode;

    @Column(name = "insured", nullable = false)
    @Builder.Default
    private boolean insured = false;

    @Column(name = "insurance_provider", length = 100)
    private String insuranceProvider;

    @Column(name = "signature_required", nullable = false)
    @Builder.Default
    private boolean signatureRequired = false;

    @Embedded
    @Builder.Default
    private AgeRestrictionDetails ageRestrictionDetails = new AgeRestrictionDetails();

    @Column(name = "tracking_number", nullable = false, unique = true, length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ShipmentStatus status;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Timestamp lastUpdated;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "history", columnDefinition = "json")
    private List<Map<String, Object>> history;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private ShipmentSource source;

    @Column(name = "user_id", length = 36)
    private String userId;

    public enum PaymentMode {
        PREPAID, COD, THIRD_PARTY
    }

    public enum ShipmentStatus {
        CREATED, PICKUP_ASSIGNED, PICKED_UP, AT_HUB, BAGGED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, CANCELLED,
        RETURN_REQUESTED, RETURN_PICKED_UP, RETURN_DELIVERED
    }

    public enum ShipmentSource {
        WEB, MOBILE, API
    }
}
