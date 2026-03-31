package com.example.demo.assignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDriverAssignmentDTO {

    private Long id;
    private Long shipmentId;
    private String trackingNumber;
    private Long driverId;
    private String driverName;
    private String driverPhoneNumber;
    private String assignmentType;
    private String status;
    private Timestamp assignedAt;

    public static ShipmentDriverAssignmentDTO fromEntity(ShipmentDriverAssignment assignment) {
        return ShipmentDriverAssignmentDTO.builder()
                .id(assignment.getId())
                .shipmentId(assignment.getShipment().getId())
                .trackingNumber(assignment.getShipment().getTrackingNumber())
                .driverId(assignment.getDriver().getId())
                .driverName(assignment.getDriver().getFirstName() + " " + assignment.getDriver().getLastName())
                .driverPhoneNumber(assignment.getDriver().getPhoneNumber())
                .assignmentType(assignment.getAssignmentType().name())
                .status(assignment.getStatus().name())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }
}
