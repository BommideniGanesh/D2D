package com.example.demo.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoDRepository extends JpaRepository<ProofOfDelivery, Long> {
    Optional<ProofOfDelivery> findByShipmentId(Long shipmentId);
}
