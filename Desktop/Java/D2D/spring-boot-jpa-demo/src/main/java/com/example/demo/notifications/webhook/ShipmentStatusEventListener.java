package com.example.demo.notifications.webhook;

import com.example.demo.events.ShipmentStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShipmentStatusEventListener {

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookDispatchService dispatchService;

    public ShipmentStatusEventListener(WebhookSubscriptionRepository subscriptionRepository, WebhookDispatchService dispatchService) {
        this.subscriptionRepository = subscriptionRepository;
        this.dispatchService = dispatchService;
    }

    @Async
    @EventListener
    public void handleShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        if (event.getUserId() == null) return;

        List<WebhookSubscription> activeSubscriptions = subscriptionRepository.findByUserIdAndIsActiveTrue(event.getUserId());
        if (activeSubscriptions.isEmpty()) return;

        String jsonPayload = String.format(
                "{\"shipmentId\": %d, \"trackingNumber\": \"%s\", \"oldStatus\": \"%s\", \"newStatus\": \"%s\"}",
                event.getShipmentId(),
                event.getTrackingNumber(),
                event.getOldStatus(),
                event.getNewStatus()
        );

        for (WebhookSubscription sub : activeSubscriptions) {
            dispatchService.dispatch(sub, "SHIPMENT.STATUS.UPDATED", jsonPayload);
        }
    }
}
