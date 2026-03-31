package com.example.demo.billing;

import com.example.demo.userregistration.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "b2b_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2BClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // Links to the base User account

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "tax_id", nullable = false, length = 100)
    private String taxId;

    @Column(name = "billing_email", nullable = false, length = 255)
    private String billingEmail;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
}
