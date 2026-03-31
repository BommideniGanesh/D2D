package com.example.demo.assignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
public class ShipmentDriverAssignmentController {

    @Autowired
    private ShipmentDriverAssignmentService assignmentService;

    /**
     * Manually trigger batch assignment.
     * Useful for testing or manual intervention.
     */
    @PostMapping("/trigger-batch")
    public ResponseEntity<Map<String, Object>> triggerBatchAssignment(
            @RequestParam(defaultValue = "10") int batchSize) {
        int assignedCount = assignmentService.executeBatchAssignment(batchSize);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("assignedCount", assignedCount);
        response.put("message", "Batch assignment completed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all assignments for a specific shipment.
     */
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<List<ShipmentDriverAssignmentDTO>> getAssignmentsByShipment(
            @PathVariable Long shipmentId) {
        List<ShipmentDriverAssignmentDTO> assignments = assignmentService.getAssignmentsByShipmentId(shipmentId);
        return ResponseEntity.ok(assignments);
    }

    /**
     * Get all assignments for a specific driver.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ShipmentDriverAssignmentDTO>> getAssignmentsByDriver(
            @PathVariable Long driverId) {
        List<ShipmentDriverAssignmentDTO> assignments = assignmentService.getAssignmentsByDriverId(driverId);
        return ResponseEntity.ok(assignments);
    }

    /**
     * Get dashboard assignments for a specific driver.
     */
    @GetMapping("/driver/{driverId}/dashboard")
    public ResponseEntity<List<com.example.demo.driver.dashboard.DriverDashboardDTO>> getDriverDashboard(
            @PathVariable Long driverId) {
        List<com.example.demo.driver.dashboard.DriverDashboardDTO> dashboardData = assignmentService
                .getDriverDashboardAssignments(driverId);
        return ResponseEntity.ok(dashboardData);
    }

    /**
     * Check if a shipment has a pickup assignment.
     */
    @GetMapping("/shipment/{shipmentId}/has-pickup")
    public ResponseEntity<Map<String, Boolean>> checkPickupAssignment(
            @PathVariable Long shipmentId) {
        boolean hasAssignment = assignmentService.hasPickupAssignment(shipmentId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasPickupAssignment", hasAssignment);

        return ResponseEntity.ok(response);
    }

    /**
     * Complete Pickup endpoint.
     */
    @PostMapping("/{assignmentId}/pickup")
    public ResponseEntity<Map<String, String>> completePickup(@PathVariable Long assignmentId) {
        assignmentService.completePickup(assignmentId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Pickup completed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Complete Delivery endpoint.
     */
    @PostMapping("/{assignmentId}/deliver")
    public ResponseEntity<Map<String, String>> completeDelivery(@PathVariable Long assignmentId) {
        assignmentService.completeDelivery(assignmentId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Delivery completed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Reject Assignment endpoint.
     */
    @PostMapping("/{assignmentId}/reject")
    public ResponseEntity<Map<String, String>> rejectAssignment(@PathVariable Long assignmentId) {
        assignmentService.rejectAssignment(assignmentId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Assignment rejected successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Assign a specific pending shipment to a driver manually.
     */
    @PostMapping("/shipment/{shipmentId}/assign/{driverId}")
    public ResponseEntity<Map<String, String>> assignShipmentToDriver(
            @PathVariable Long shipmentId,
            @PathVariable Long driverId) {
        assignmentService.assignPendingShipmentToDriver(shipmentId, driverId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipment assigned to driver successfully");
        return ResponseEntity.ok(response);
    }
    /**
     * Get earnings summary for a specific driver.
     */
    @GetMapping("/driver/{driverId}/earnings")
    public ResponseEntity<com.example.demo.driver.dashboard.DriverEarningsSummaryDTO> getDriverEarnings(
            @PathVariable Long driverId) {
        return ResponseEntity.ok(assignmentService.getDriverEarningsSummary(driverId));
    }
}
