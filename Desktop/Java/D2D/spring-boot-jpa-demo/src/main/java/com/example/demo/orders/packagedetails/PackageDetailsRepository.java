package com.example.demo.orders.packagedetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageDetailsRepository extends JpaRepository<PackageDetails, Long> {
}
