package com.example.demo.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransitBagRepository extends JpaRepository<TransitBag, Long> {
    Optional<TransitBag> findByBagBarcode(String bagBarcode);
}
