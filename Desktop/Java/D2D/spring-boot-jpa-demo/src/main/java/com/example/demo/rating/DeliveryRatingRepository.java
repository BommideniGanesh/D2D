package com.example.demo.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryRatingRepository extends JpaRepository<DeliveryRating, Long> {
    Optional<DeliveryRating> findByShipment_Id(Long shipmentId);
    List<DeliveryRating> findByDriverId(String driverId);

    @Query("SELECT AVG(r.rating) FROM DeliveryRating r WHERE r.driverId = :driverId")
    Double findAverageRatingByDriverId(@Param("driverId") String driverId);
}
