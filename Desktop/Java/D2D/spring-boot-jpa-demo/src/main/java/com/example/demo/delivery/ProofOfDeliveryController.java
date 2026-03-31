package com.example.demo.delivery;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
public class ProofOfDeliveryController {

    private final ProofOfDeliveryService proofOfDeliveryService;
    private final PoDRepository poDRepository;

    public ProofOfDeliveryController(ProofOfDeliveryService proofOfDeliveryService,
                                     PoDRepository poDRepository) {
        this.proofOfDeliveryService = proofOfDeliveryService;
        this.poDRepository = poDRepository;
    }

    @PostMapping("/{assignmentId}/proof")
    public ResponseEntity<Map<String, Object>> submitProofOfDelivery(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, String> payload) {
        
        String imageUrl = payload.get("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image URL is required"));
        }

        try {
            ProofOfDelivery pod = proofOfDeliveryService.submitPoDAndCompleteDelivery(assignmentId, imageUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Proof of delivery accepted and assignment completed.");
            response.put("podId", pod.getId());
            response.put("status", pod.getMlValidationStatus());
            response.put("imageUrl", pod.getImageUrl());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * POST upload PoD image file → saves to static/pod-images/ → returns URL.
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadPodImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID() + ext;

            // Save to external uploads/pod-images/ directory so it's accessible immediately without restart
            Path uploadDir = Paths.get("uploads", "pod-images");
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(filename), file.getBytes());

            String imageUrl = "/pod-images/" + filename;
            Map<String, Object> result = new HashMap<>();
            result.put("imageUrl", imageUrl);
            result.put("filename", filename);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to save image: " + e.getMessage()));
        }
    }

    /**
     * GET proof of delivery for a shipment.
     */
    @GetMapping("/shipment/{shipmentId}/proof")
    public ResponseEntity<Map<String, Object>> getPodByShipment(@PathVariable Long shipmentId) {
        return poDRepository.findByShipmentId(shipmentId)
                .map(pod -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("podId", pod.getId());
                    response.put("shipmentId", pod.getShipment().getId());
                    response.put("driverId", pod.getDriverId());
                    response.put("imageUrl", pod.getImageUrl());
                    response.put("mlValidationStatus", pod.getMlValidationStatus());
                    response.put("timestamp", pod.getTimestamp());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
