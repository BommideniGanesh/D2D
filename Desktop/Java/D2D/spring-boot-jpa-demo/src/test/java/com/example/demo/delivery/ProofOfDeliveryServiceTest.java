package com.example.demo.delivery;

import com.example.demo.assignment.ShipmentDriverAssignment;
import com.example.demo.assignment.ShipmentDriverAssignmentRepository;
import com.example.demo.assignment.ShipmentDriverAssignmentService;
import com.example.demo.driver.DriverProfile;
import com.example.demo.orders.shipment.Shipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProofOfDeliveryServiceTest {

    @Mock
    private PoDRepository poDRepository;

    @Mock
    private MLImageValidationService mlService;

    @Mock
    private ShipmentDriverAssignmentService assignmentService;

    @Mock
    private ShipmentDriverAssignmentRepository assignmentRepository;

    @InjectMocks
    private ProofOfDeliveryService proofOfDeliveryService;

    private ShipmentDriverAssignment assignment;
    private Shipment shipment;
    private DriverProfile driver;

    @BeforeEach
    void setUp() {
        shipment = new Shipment();
        shipment.setId(100L);

        driver = new DriverProfile();
        driver.setId(1L);

        assignment = new ShipmentDriverAssignment();
        assignment.setId(10L);
        assignment.setShipment(shipment);
        assignment.setDriver(driver);
        assignment.setAssignmentType(ShipmentDriverAssignment.AssignmentType.DELIVERY);
    }

    @Test
    void testSubmitPoD_Passed() {
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(mlService.validateDeliveryImage("good_photo.jpg")).thenReturn(true);

        ProofOfDelivery pod = proofOfDeliveryService.submitPoDAndCompleteDelivery(10L, "good_photo.jpg");

        assertNotNull(pod);
        assertEquals(ProofOfDelivery.MLValidationStatus.PASSED, pod.getMlValidationStatus());
        verify(poDRepository, times(1)).save(any(ProofOfDelivery.class));
        verify(assignmentService, times(1)).completeDelivery(10L);
    }

    @Test
    void testSubmitPoD_Failed() {
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(mlService.validateDeliveryImage("blurry.jpg")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            proofOfDeliveryService.submitPoDAndCompleteDelivery(10L, "blurry.jpg");
        });

        assertTrue(exception.getMessage().contains("ML Image Validation Failed"));
        
        // Ensure PoD was saved with FAILED status before throwing exception
        verify(poDRepository, times(1)).save(argThat(pod -> pod.getMlValidationStatus() == ProofOfDelivery.MLValidationStatus.FAILED));
        
        // Ensure completeDelivery was NEVER called because validation failed
        verify(assignmentService, never()).completeDelivery(10L);
    }
}
