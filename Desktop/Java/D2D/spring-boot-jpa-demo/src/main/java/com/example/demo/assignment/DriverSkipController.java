package com.example.demo.assignment;

import com.example.demo.driver.DriverProfile;
import com.example.demo.driver.DriverProfileRepository;
import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.shipment.ShipmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assignments")
public class DriverSkipController {

    private final DriverSkippedShipmentRepository skipRepository;
    private final DriverProfileRepository driverRepository;
    private final ShipmentRepository shipmentRepository;

    public DriverSkipController(DriverSkippedShipmentRepository skipRepository,
                                DriverProfileRepository driverRepository,
                                ShipmentRepository shipmentRepository) {
        this.skipRepository = skipRepository;
        this.driverRepository = driverRepository;
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Driver marks a pending shipment as "Not Interested".
     * POST /api/assignments/driver/{driverId}/skip/{shipmentId}
     */
    @PostMapping("/driver/{driverId}/skip/{shipmentId}")
    public ResponseEntity<Map<String, Object>> skipShipment(
            @PathVariable Long driverId,
            @PathVariable Long shipmentId) {

        // Idempotent: skip only if not already skipped
        if (skipRepository.existsByDriverIdAndShipmentId(driverId, shipmentId)) {
            return ResponseEntity.ok(Map.of("message", "Already skipped", "skipped", true));
        }

        DriverProfile driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        DriverSkippedShipment skip = DriverSkippedShipment.builder()
                .driver(driver)
                .shipment(shipment)
                .build();
        skipRepository.save(skip);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Shipment skipped successfully");
        response.put("shipmentId", shipmentId);
        response.put("skipped", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all shipments the driver has skipped (for the Rejected tab).
     * GET /api/assignments/driver/{driverId}/skipped
     */
    @GetMapping("/driver/{driverId}/skipped")
    public ResponseEntity<List<Map<String, Object>>> getSkippedShipments(
            @PathVariable Long driverId) {

        List<Map<String, Object>> result = skipRepository.findByDriverIdWithShipment(driverId)
                .stream()
                .map(s -> {
                    Shipment shipment = s.getShipment();
                    Map<String, Object> item = new HashMap<>();
                    item.put("skipId", s.getId());
                    item.put("shipmentId", shipment.getId());
                    item.put("trackingNumber", shipment.getTrackingNumber());
                    item.put("skippedAt", s.getSkippedAt());
                    item.put("senderName", shipment.getSender().getFirstName() + " " + shipment.getSender().getLastName());
                    item.put("senderAddress", shipment.getSender().getAddressLine1() + ", " + shipment.getSender().getCity());
                    item.put("receiverName", shipment.getReceiver().getFirstName() + " " + shipment.getReceiver().getLastName());
                    item.put("receiverAddress", shipment.getReceiver().getAddress().getAddressLine1());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Get all skipped shipment IDs for a driver (fast filter for pending orders).
     * GET /api/assignments/driver/{driverId}/skipped-ids
     */
    @GetMapping("/driver/{driverId}/skipped-ids")
    public ResponseEntity<List<Long>> getSkippedShipmentIds(@PathVariable Long driverId) {
        return ResponseEntity.ok(skipRepository.findSkippedShipmentIdsByDriverId(driverId));
    }
}
