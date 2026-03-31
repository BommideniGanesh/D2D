package com.example.demo.returns;

import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.shipment.ShipmentRepository;
import com.example.demo.assignment.ShipmentDriverAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentDriverAssignmentService assignmentService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public ReturnService(ReturnRequestRepository returnRequestRepository,
                         ShipmentRepository shipmentRepository,
                         ShipmentDriverAssignmentService assignmentService,
                         org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.returnRequestRepository = returnRequestRepository;
        this.shipmentRepository = shipmentRepository;
        this.assignmentService = assignmentService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReturnRequest requestReturn(Long shipmentId, String reason, String requestedBy) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        if (shipment.getStatus() != Shipment.ShipmentStatus.DELIVERED) {
            throw new RuntimeException("Only DELIVERED shipments can be returned.");
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .shipment(shipment)
                .reason(reason)
                .requestedBy(requestedBy)
                .status(ReturnRequest.ReturnStatus.PENDING_APPROVAL)
                .build();

        returnRequestRepository.save(returnRequest);

        shipment.setStatus(Shipment.ShipmentStatus.RETURN_REQUESTED);
        shipmentRepository.save(shipment);

        eventPublisher.publishEvent(new com.example.demo.events.ShipmentStatusChangedEvent(
                this, 
                shipment.getId(), 
                shipment.getUserId(), 
                "DELIVERED", 
                "RETURN_REQUESTED", 
                shipment.getTrackingNumber()
        ));

        return returnRequest;
    }

    @Transactional
    public void approveReturn(Long returnRequestId) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new RuntimeException("Return Request not found"));

        if (returnRequest.getStatus() != ReturnRequest.ReturnStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Return Request is not pending approval.");
        }

        returnRequest.setStatus(ReturnRequest.ReturnStatus.APPROVED);
        returnRequestRepository.save(returnRequest);

        // Assign a driver to pick up the return from the Receiver
        assignmentService.assignReturnPickup(returnRequest.getShipment());
    }
}
