package com.example.demo.orders.senderdetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SenderDetailsRepository extends JpaRepository<SenderDetails, String> {
}
