package com.example.demo.tracking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class TrackingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverLocationUpdateRepository repository;

    public TrackingController(SimpMessagingTemplate messagingTemplate, DriverLocationUpdateRepository repository) {
        this.messagingTemplate = messagingTemplate;
        this.repository = repository;
    }

    @MessageMapping("/tracking/ping")
    public void processLocationPing(@Payload DriverLocationUpdate ping) {
        log.info("Received GPS ping from Driver {} for Shipment {}: [{},{}]",
                ping.getDriverId(), ping.getShipmentId(), ping.getLatitude(), ping.getLongitude());

        // 1. Log safely to Time-Series Database Table
        DriverLocationUpdate saved = repository.save(ping);

        // 2. Transmit Live Signal directly into specific Shipment Subscription queues
        // Frontend clients subscribed to "/topic/tracking/123" will instantly receive the object
        messagingTemplate.convertAndSend("/topic/tracking/" + saved.getShipmentId(), saved);
    }
}
