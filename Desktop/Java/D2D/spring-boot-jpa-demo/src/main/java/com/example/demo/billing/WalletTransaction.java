package com.example.demo.billing;

import com.example.demo.orders.shipment.Shipment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private ClientWallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment; // The shipment this charge is related to

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_invoiced", nullable = false)
    @Builder.Default
    private boolean isInvoiced = false; // Flag for Spring Batch aggregator

    @CreationTimestamp
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private Timestamp transactionDate;

    public enum TransactionType {
        DEBIT, // Charging them for a shipment (lowers balance)
        CREDIT // They paid their invoice (raises balance)
    }
}
