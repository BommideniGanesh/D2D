package com.example.demo.orders.receiverdetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiverDetailsRepository extends JpaRepository<ReceiverDetails, String> {
}
