package com.example.demo.billing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "client_wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2b_client_id", nullable = false, unique = true)
    private B2BClient b2bClient;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO; // Typically negative if they owe money, positive if pre-funded

    @Column(name = "credit_limit", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = new BigDecimal("10000.00");

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Timestamp lastUpdated;
}
