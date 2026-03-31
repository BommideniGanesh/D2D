package com.example.demo.assignment;

import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.senderdetails.SenderDetails;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteOptimizationServiceTest {

    @Test
    void testOptimizeRouteSequence_NearestNeighbor() {
        RouteOptimizationService service = new RouteOptimizationService();

        // Create mock assignments with specific Lat / Lon directly up a single longitude line
        // Point A: 40.0, -74.0
        // Point B: 40.5, -74.0
        // Point C: 41.0, -74.0
        // If driver starts at 39.5, -74.0 (South of A), sequence must mathematically be A -> B -> C
        
        ShipmentDriverAssignment assignmentA = createMockAssignment(1L, 40.0, -74.0);
        ShipmentDriverAssignment assignmentB = createMockAssignment(2L, 40.5, -74.0);
        ShipmentDriverAssignment assignmentC = createMockAssignment(3L, 41.0, -74.0);

        // Intentionally shuffle the initial list fed into the optimizer
        List<ShipmentDriverAssignment> assignments = Arrays.asList(assignmentC, assignmentA, assignmentB);

        // Calculate
        List<ShipmentDriverAssignment> optimized = service.optimizeRouteSequence(39.5, -74.0, assignments);

        // Assert 
        assertEquals(3, optimized.size(), "Should return exactly 3 mapped entries");
        
        // Assert the exact sorted physical sequence matches spatial layout
        assertEquals(1L, optimized.get(0).getId(), "First stop should be ID 1 (Point A)");
        assertEquals(2L, optimized.get(1).getId(), "Second stop should be ID 2 (Point B)");
        assertEquals(3L, optimized.get(2).getId(), "Third stop should be ID 3 (Point C)");

        // Assert sequence markers mutated correctly
        assertEquals(1, optimized.get(0).getSequenceOrder());
        assertEquals(2, optimized.get(1).getSequenceOrder());
        assertEquals(3, optimized.get(2).getSequenceOrder());
    }

    private ShipmentDriverAssignment createMockAssignment(Long id, double lat, double lon) {
        ShipmentDriverAssignment assignment = new ShipmentDriverAssignment();
        assignment.setId(id);
        assignment.setAssignmentType(ShipmentDriverAssignment.AssignmentType.PICKUP);

        SenderDetails sender = new SenderDetails();
        sender.setLatitude(BigDecimal.valueOf(lat));
        sender.setLongitude(BigDecimal.valueOf(lon));

        Shipment shipment = new Shipment();
        shipment.setSender(sender);
        assignment.setShipment(shipment);

        return assignment;
    }
}
