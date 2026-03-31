package com.example.demo.rating;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class DeliveryRatingController {

    private final DeliveryRatingService ratingService;

    public DeliveryRatingController(DeliveryRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<?> submitRating(@RequestBody Map<String, Object> body) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String ratedBy = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"))
                    ? auth.getName() : "anonymous";

            Long shipmentId = Long.valueOf(body.get("shipmentId").toString());
            int rating = Integer.parseInt(body.get("rating").toString());
            String comment = body.getOrDefault("comment", "").toString();

            DeliveryRating saved = ratingService.submitRating(shipmentId, rating, comment, ratedBy);
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "rating", saved.getRating(),
                    "comment", saved.getComment() != null ? saved.getComment() : "",
                    "driverId", saved.getDriverId() != null ? saved.getDriverId() : "",
                    "ratedBy", saved.getRatedBy()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<?> getRatingByShipment(@PathVariable Long shipmentId) {
        return ratingService.getRatingByShipmentId(shipmentId)
                .map(r -> ResponseEntity.ok((Object) Map.of(
                        "id", r.getId(),
                        "rating", r.getRating(),
                        "comment", r.getComment() != null ? r.getComment() : "",
                        "ratedBy", r.getRatedBy()
                )))
                .orElse(ResponseEntity.ok(Map.of("rated", false)));
    }

    @GetMapping("/driver/{driverId}/average")
    public ResponseEntity<?> getDriverAverage(@PathVariable String driverId) {
        Double avg = ratingService.getDriverAverageRating(driverId);
        return ResponseEntity.ok(Map.of(
                "driverId", driverId,
                "averageRating", avg != null ? avg : 0.0
        ));
    }
}
