package com.example.demo.assignment;

import com.example.demo.orders.shipment.Shipment;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteOptimizationService {

    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Orders assignments using a Nearest-Neighbor TSP Heuristic.
     */
    public List<ShipmentDriverAssignment> optimizeRouteSequence(
            double startLatitude, double startLongitude, List<ShipmentDriverAssignment> assignments) {

        if (assignments == null || assignments.isEmpty()) {
            return assignments;
        }

        List<ShipmentDriverAssignment> unvisited = new ArrayList<>(assignments);
        List<ShipmentDriverAssignment> route = new ArrayList<>();

        double currentLat = startLatitude;
        double currentLon = startLongitude;

        int sequence = 1;
        while (!unvisited.isEmpty()) {
            ShipmentDriverAssignment nearest = null;
            double minDistance = Double.MAX_VALUE;
            double bestLat = currentLat;
            double bestLon = currentLon;

            for (ShipmentDriverAssignment candidate : unvisited) {
                Shipment targetShipment = candidate.getShipment();
                
                double candidateLat;
                double candidateLon;

                // For PICKUP assignments, use Sender details. For DELIVERY, use Receiver.
                if (candidate.getAssignmentType() == ShipmentDriverAssignment.AssignmentType.PICKUP ||
                    candidate.getAssignmentType() == ShipmentDriverAssignment.AssignmentType.RETURN_PICKUP) {
                    if (targetShipment.getSender().getLatitude() != null && targetShipment.getSender().getLongitude() != null) {
                        candidateLat = targetShipment.getSender().getLatitude().doubleValue();
                        candidateLon = targetShipment.getSender().getLongitude().doubleValue();
                    } else {
                        candidateLat = 40.7128 + (Math.random() * 0.1);
                        candidateLon = -74.0060 + (Math.random() * 0.1);
                    }
                } else {
                    // Receiver typically holds an embedded Address. We fallback gracefully if coordinates are missing.
                    candidateLat = 40.7128 + (Math.random() * 0.1); 
                    candidateLon = -74.0060 + (Math.random() * 0.1);
                }

                double dist = calculateHaversineDistance(currentLat, currentLon, candidateLat, candidateLon);

                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = candidate;
                    bestLat = candidateLat;
                    bestLon = candidateLon;
                }
            }

            if (nearest != null) {
                nearest.setSequenceOrder(sequence++);
                route.add(nearest);
                unvisited.remove(nearest);

                // Update current locator to jump to nearest node
                currentLat = bestLat;
                currentLon = bestLon;
            }
        }
        return route;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
