package com.example.demo.orders.shipment;

import com.example.demo.userregistration.User;
import com.example.demo.userregistration.UserRegistrationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final UserRegistrationRepository userRepository;

    public ShipmentController(ShipmentService shipmentService, UserRegistrationRepository userRepository) {
        this.shipmentService = shipmentService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ShipmentDTO> createShipment(@RequestBody ShipmentDTO shipmentDTO, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            shipmentDTO.setUserId(user.getId());
        }
        ShipmentDTO createdShipment = shipmentService.createShipment(shipmentDTO);
        return new ResponseEntity<>(createdShipment, HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ShipmentDTO>> getPendingShipments() {
        List<ShipmentDTO> shipments = shipmentService.getPendingShipments();
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ShipmentDTO> getShipment(@PathVariable String identifier) {
        ShipmentDTO shipment;
        // Check if identifier is numerical (ID) or String (Tracking Number)
        if (identifier.matches("^\\d+$")) {
            shipment = shipmentService.getShipmentById(Long.valueOf(identifier));
        } else {
            shipment = shipmentService.getShipmentByTrackingNumber(identifier);
        }
        return ResponseEntity.ok(shipment);
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getAllShipments(Principal principal) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<ShipmentDTO> shipments = shipmentService.getAllShipmentsByUserId(user.getId());
            return ResponseEntity.ok(shipments);
        } else {
            List<ShipmentDTO> shipments = shipmentService.getAllShipments();
            return ResponseEntity.ok(shipments);
        }
    }
}
