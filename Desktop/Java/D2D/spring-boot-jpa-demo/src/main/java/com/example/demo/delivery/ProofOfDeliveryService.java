package com.example.demo.delivery;

import com.example.demo.assignment.ShipmentDriverAssignment;
import com.example.demo.assignment.ShipmentDriverAssignmentRepository;
import com.example.demo.assignment.ShipmentDriverAssignmentService;
import com.example.demo.orders.shipment.Shipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProofOfDeliveryService {

    private final PoDRepository poDRepository;
    private final MLImageValidationService mlService;
    private final ShipmentDriverAssignmentService assignmentService;
    private final ShipmentDriverAssignmentRepository assignmentRepository;

    public ProofOfDeliveryService(PoDRepository poDRepository,
                                  MLImageValidationService mlService,
                                  ShipmentDriverAssignmentService assignmentService,
                                  ShipmentDriverAssignmentRepository assignmentRepository) {
        this.poDRepository = poDRepository;
        this.mlService = mlService;
        this.assignmentService = assignmentService;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public ProofOfDelivery submitPoDAndCompleteDelivery(Long assignmentId, String imageUrl) {
        ShipmentDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getAssignmentType() != ShipmentDriverAssignment.AssignmentType.DELIVERY) {
            throw new RuntimeException("Assignment must be a standard DELIVERY to submit PoD");
        }

        Shipment shipment = assignment.getShipment();

        boolean isValid = mlService.validateDeliveryImage(imageUrl);
        
        ProofOfDelivery.MLValidationStatus status = isValid 
                ? ProofOfDelivery.MLValidationStatus.PASSED 
                : ProofOfDelivery.MLValidationStatus.FAILED;

        ProofOfDelivery pod = ProofOfDelivery.builder()
                .shipment(shipment)
                .driverId(assignment.getDriver().getId())
                .imageUrl(imageUrl)
                .mlValidationStatus(status)
                .build();
                
        poDRepository.save(pod);

        if (!isValid) {
            throw new RuntimeException("ML Image Validation Failed: The provided image is too blurry, dark, or invalid.");
        }

        // Complete the delivery now that PoD has passed
        assignmentService.completeDelivery(assignmentId);
        
        return pod;
    }
}
