package com.example.demo.events;

import org.springframework.context.ApplicationEvent;

public class ShipmentStatusChangedEvent extends ApplicationEvent {
    
    private final Long shipmentId;
    private final String userId;
    private final String oldStatus;
    private final String newStatus;
    private final String trackingNumber;

    public ShipmentStatusChangedEvent(Object source, Long shipmentId, String userId, String oldStatus, String newStatus, String trackingNumber) {
        super(source);
        this.shipmentId = shipmentId;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.trackingNumber = trackingNumber;
    }

    public Long getShipmentId() { return shipmentId; }
    public String getUserId() { return userId; }
    public String getOldStatus() { return oldStatus; }
    public String getNewStatus() { return newStatus; }
    public String getTrackingNumber() { return trackingNumber; }
}
