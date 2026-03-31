package com.example.demo.rating;

import com.example.demo.assignment.ShipmentDriverAssignment;
import com.example.demo.assignment.ShipmentDriverAssignmentRepository;
import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.shipment.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DeliveryRatingService {

    private final DeliveryRatingRepository ratingRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentDriverAssignmentRepository assignmentRepository;

    public DeliveryRatingService(DeliveryRatingRepository ratingRepository,
                                  ShipmentRepository shipmentRepository,
                                  ShipmentDriverAssignmentRepository assignmentRepository) {
        this.ratingRepository = ratingRepository;
        this.shipmentRepository = shipmentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public DeliveryRating submitRating(Long shipmentId, int rating, String comment, String ratedBy) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));

        if (shipment.getStatus() != Shipment.ShipmentStatus.DELIVERED) {
            throw new RuntimeException("Can only rate a DELIVERED shipment.");
        }

        if (ratingRepository.findByShipment_Id(shipmentId).isPresent()) {
            throw new RuntimeException("This shipment has already been rated.");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5.");
        }

        // Resolve driverId from the assignment
        String driverId = assignmentRepository.findByShipmentId(shipmentId).stream()
                .findFirst()
                .map(a -> String.valueOf(a.getDriver().getId()))
                .orElse(null);

        DeliveryRating dr = DeliveryRating.builder()
                .shipment(shipment)
                .driverId(driverId)
                .rating(rating)
                .comment(comment)
                .ratedBy(ratedBy)
                .build();

        return ratingRepository.save(dr);
    }

    public Optional<DeliveryRating> getRatingByShipmentId(Long shipmentId) {
        return ratingRepository.findByShipment_Id(shipmentId);
    }

    public Double getDriverAverageRating(String driverId) {
        Double avg = ratingRepository.findAverageRatingByDriverId(driverId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : null;
    }
}
