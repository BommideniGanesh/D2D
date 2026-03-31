package com.example.demo.assignment;

import com.example.demo.driver.DriverProfile;
import com.example.demo.driver.DriverProfileRepository;
import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.shipment.ShipmentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.example.demo.driver.dashboard.DriverEarningHistoryDTO;
import com.example.demo.driver.dashboard.DriverEarningsSummaryDTO;

@Service
public class ShipmentDriverAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentDriverAssignmentService.class);
    private static final int DEFAULT_BATCH_SIZE = 10;

    @Autowired
    private ShipmentDriverAssignmentRepository assignmentRepository;

    @Autowired
    private DriverProfileRepository driverRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Autowired
    private RouteOptimizationService routeOptimizationService;

    @Autowired
    private com.example.demo.delivery.PoDRepository poDRepository;

    /**
     * Optimizes all OPEN assignments for a specific driver using a TSP heuristic.
     * Updates their `sequenceOrder` for dynamic routing sequences.
     */
    @Transactional
    public void optimizeDriverRoute(Long driverId, double currentLat, double currentLon) {
        List<ShipmentDriverAssignment> assignments = assignmentRepository.findByDriverId(driverId)
                .stream()
                .filter(a -> a.getStatus() == ShipmentDriverAssignment.AssignmentStatus.ASSIGNED ||
                             a.getStatus() == ShipmentDriverAssignment.AssignmentStatus.ACCEPTED)
                .collect(Collectors.toList());

        List<ShipmentDriverAssignment> optimized = routeOptimizationService.optimizeRouteSequence(currentLat, currentLon, assignments);
        assignmentRepository.saveAll(optimized);
    }

    /**
     * Execute batch assignment of pickup drivers.
     * This is the main method called by the scheduler.
     */
    /**
     * Execute batch assignment with default batch size.
     */
    @Transactional
    public int executeBatchAssignment() {
        return executeBatchAssignment(DEFAULT_BATCH_SIZE);
    }

    @Transactional
    public int executeBatchAssignment(int batchSize) {
        logger.info("Starting batch driver assignment via native query for batch size {}", batchSize);

        try {
            // Use native query instead of stored procedure (SP not deployed to DB)
            int assigned = assignmentRepository.assignPickupDrivers(batchSize);
            logger.info("Batch assignment completed. Rows affected: {}", assigned);
            return assigned;

        } catch (Exception e) {
            logger.error("Error during batch assignment execution: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all assignments for a shipment.
     */
    public List<ShipmentDriverAssignmentDTO> getAssignmentsByShipmentId(Long shipmentId) {
        return assignmentRepository.findByShipmentId(shipmentId)
                .stream()
                .map(ShipmentDriverAssignmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all assignments for a driver.
     */
    public List<ShipmentDriverAssignmentDTO> getAssignmentsByDriverId(Long driverId) {
        return assignmentRepository.findByDriverId(driverId)
                .stream()
                .map(ShipmentDriverAssignmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Check if a shipment already has a pickup assignment.
     */
    public boolean hasPickupAssignment(Long shipmentId) {
        return assignmentRepository.findByShipmentIdAndAssignmentType(
                shipmentId,
                ShipmentDriverAssignment.AssignmentType.PICKUP).isPresent();
    }

    /**
     * Get dashboard assignments for a driver with full details.
     */
    /**
     * Get dashboard assignments for a driver with full details.
     * Excludes REJECTED assignments (Soft Delete for UI).
     */
    public List<com.example.demo.driver.dashboard.DriverDashboardDTO> getDriverDashboardAssignments(Long driverId) {
        return assignmentRepository.findByDriverId(driverId)
                .stream()
                .map(this::mapToDashboardDTO)
                .collect(Collectors.toList());
    }

    private com.example.demo.driver.dashboard.DriverDashboardDTO mapToDashboardDTO(
            ShipmentDriverAssignment assignment) {
        Shipment shipment = assignment.getShipment();

        // Look up PoD for COMPLETED DELIVERY assignments
        String podImageUrl = null;
        String podStatus = null;
        if (assignment.getAssignmentType() == ShipmentDriverAssignment.AssignmentType.DELIVERY
                && assignment.getStatus() == ShipmentDriverAssignment.AssignmentStatus.COMPLETED) {
            var pod = poDRepository.findByShipmentId(shipment.getId());
            if (pod.isPresent()) {
                podImageUrl = pod.get().getImageUrl();
                podStatus = pod.get().getMlValidationStatus().name();
            }
        }

        return com.example.demo.driver.dashboard.DriverDashboardDTO.builder()
                .assignmentId(assignment.getId())
                .assignmentStatus(assignment.getStatus().name())
                .assignmentType(assignment.getAssignmentType().name())
                .assignedAt(assignment.getAssignedAt())
                .shipmentId(shipment.getId())
                .trackingNumber(shipment.getTrackingNumber())
                .shipmentStatus(shipment.getStatus().name())
                .totalAmount(shipment.getTotalAmount())
                .paymentMode(shipment.getPaymentMode().name())
                .signatureRequired(shipment.isSignatureRequired())
                .senderName(shipment.getSender().getFirstName() + " " + shipment.getSender().getLastName())
                .senderPhone(shipment.getSender().getPhoneNumber())
                .pickupAddress(shipment.getSender().getAddressLine1())
                .pickupCity(shipment.getSender().getCity())
                .pickupState(shipment.getSender().getState())
                .pickupPostalCode(shipment.getSender().getPostalCode())
                .pickupLatitude(shipment.getSender().getLatitude())
                .pickupLongitude(shipment.getSender().getLongitude())
                .receiverName(shipment.getReceiver().getFirstName() + " " + shipment.getReceiver().getLastName())
                .receiverPhone(shipment.getReceiver().getPhoneNumber())
                .deliveryAddress(shipment.getReceiver().getAddress().getAddressLine1())
                .deliveryCity(shipment.getReceiver().getAddress().getCity())
                .deliveryState(shipment.getReceiver().getAddress().getState())
                .deliveryPostalCode(shipment.getReceiver().getAddress().getPostalCode())
                .deliveryLatitude(java.math.BigDecimal.ZERO)
                .deliveryLongitude(java.math.BigDecimal.ZERO)
                .packageDescription(shipment.getPackageDetails().getBoxType().name() + " - " +
                        (shipment.getPackageDetails().getHandlingInstructions() != null
                                ? shipment.getPackageDetails().getHandlingInstructions()
                                : "No instructions"))
                .weight(shipment.getPackageDetails().getWeightKg().doubleValue())
                .dimensions(shipment.getPackageDetails().getLengthCm() + "x" +
                        shipment.getPackageDetails().getWidthCm() + "x" +
                        shipment.getPackageDetails().getHeightCm())
                .podImageUrl(podImageUrl)
                .podStatus(podStatus)
                .build();
    }

    /**
     * Assign a driver to a shipment immediately based on pincode.
     */
    @Transactional
    public void assignDriverToShipment(Shipment shipment) {
        String pincode = shipment.getSender().getPostalCode();

        // Find drivers available in this pincode
        List<DriverProfile> availableDrivers = driverRepository.findByPincodeAndAvailabilityStatus(
                pincode, DriverProfile.AvailabilityStatus.AVAILABLE);

        if (!availableDrivers.isEmpty()) {
            // Pick the first available driver
            DriverProfile driver = availableDrivers.get(0);

            ShipmentDriverAssignment assignment = ShipmentDriverAssignment.builder()
                    .shipment(shipment)
                    .driver(driver)
                    .assignmentType(ShipmentDriverAssignment.AssignmentType.PICKUP)
                    .status(ShipmentDriverAssignment.AssignmentStatus.ASSIGNED)
                    .build();

            assignmentRepository.save(assignment);

            // Update driver status
            driver.setAvailabilityStatus(DriverProfile.AvailabilityStatus.ON_DELIVERY);
            driverRepository.save(driver);

            // Update shipment status
            shipment.setStatus(Shipment.ShipmentStatus.PICKUP_ASSIGNED);
            shipmentRepository.save(shipment);

            logger.info("Automatically assigned driver {} to shipment {}", driver.getId(), shipment.getId());
        } else {
            logger.info("No available driver found for shipment {} with pincode {}", shipment.getId(), pincode);
        }
    }

    /**
     * Manually assign a pending shipment to a specific driver.
     */
    @Transactional
    public void assignPendingShipmentToDriver(Long shipmentId, Long driverId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        if (shipment.getStatus() != Shipment.ShipmentStatus.CREATED) {
            throw new RuntimeException("Shipment is not in CREATED status");
        }

        DriverProfile driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        ShipmentDriverAssignment assignment = ShipmentDriverAssignment.builder()
                .shipment(shipment)
                .driver(driver)
                .assignmentType(ShipmentDriverAssignment.AssignmentType.PICKUP)
                .status(ShipmentDriverAssignment.AssignmentStatus.ASSIGNED)
                .build();

        assignmentRepository.save(assignment);

        // Update driver status
        driver.setAvailabilityStatus(DriverProfile.AvailabilityStatus.ON_DELIVERY);
        driverRepository.save(driver);

        // Update shipment status
        shipment.setStatus(Shipment.ShipmentStatus.PICKUP_ASSIGNED);
        shipmentRepository.save(shipment);

        logger.info("Manually assigned driver {} to shipment {}", driverId, shipmentId);
    }

    /**
     * Complete a PICKUP assignment.
     * - Marks pickup assignment as COMPLETED.
     * - Updates Shipment to PICKED_UP.
     * - Creates a DELIVERY assignment for the same driver.
     */
    @Transactional
    public void completePickup(Long assignmentId) {
        ShipmentDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getAssignmentType() != ShipmentDriverAssignment.AssignmentType.PICKUP) {
            throw new RuntimeException("Invalid assignment type. Expected PICKUP.");
        }

        // 1. Complete Pickup Assignment
        assignment.setStatus(ShipmentDriverAssignment.AssignmentStatus.COMPLETED);
        assignmentRepository.save(assignment);

        // 2. Update Shipment Status
        Shipment shipment = assignment.getShipment();
        shipment.setStatus(Shipment.ShipmentStatus.PICKED_UP);
        shipmentRepository.save(shipment);

        eventPublisher.publishEvent(new com.example.demo.events.ShipmentStatusChangedEvent(
                this, shipment.getId(), shipment.getUserId(), "PICKUP_ASSIGNED", "PICKED_UP", shipment.getTrackingNumber()));

        // 3. Create Delivery Assignment (Same Driver)
        ShipmentDriverAssignment deliveryAssignment = ShipmentDriverAssignment.builder()
                .shipment(shipment)
                .driver(assignment.getDriver())
                .assignmentType(ShipmentDriverAssignment.AssignmentType.DELIVERY)
                .status(ShipmentDriverAssignment.AssignmentStatus.ASSIGNED)
                .build();
        assignmentRepository.save(deliveryAssignment);

        logger.info("Completed pickup for shipment {}. Created delivery assignment.", shipment.getId());
    }

    /**
     * Complete a DELIVERY assignment.
     * - Marks delivery assignment as COMPLETED.
     * - Updates Shipment to DELIVERED.
     * - Sets Driver to AVAILABLE.
     */
    @Transactional
    public void completeDelivery(Long assignmentId) {
        ShipmentDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getAssignmentType() != ShipmentDriverAssignment.AssignmentType.DELIVERY) {
            throw new RuntimeException("Invalid assignment type. Expected DELIVERY.");
        }

        // 1. Complete Delivery Assignment
        assignment.setStatus(ShipmentDriverAssignment.AssignmentStatus.COMPLETED);
        assignmentRepository.save(assignment);

        // 2. Update Shipment Status
        Shipment shipment = assignment.getShipment();
        shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
        shipmentRepository.save(shipment);

        eventPublisher.publishEvent(new com.example.demo.events.ShipmentStatusChangedEvent(
                this, shipment.getId(), shipment.getUserId(), "PICKED_UP", "DELIVERED", shipment.getTrackingNumber()));

        // 3. Update Driver Status to AVAILABLE
        DriverProfile driver = assignment.getDriver();
        driver.setAvailabilityStatus(DriverProfile.AvailabilityStatus.AVAILABLE);
        driverRepository.save(driver);

        logger.info("Completed delivery for shipment {}. Driver {} is now AVAILABLE.", shipment.getId(),
                driver.getId());
    }

    /**
     * Reject an assignment.
     * - Marks assignment as REJECTED.
     * - Resets Shipment to CREATED (so it can be reassigned).
     * - Resets Driver to AVAILABLE.
     */
    @Transactional
    public void rejectAssignment(Long assignmentId) {
        ShipmentDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getStatus() != ShipmentDriverAssignment.AssignmentStatus.ASSIGNED) {
            throw new RuntimeException("Can only reject active assignments.");
        }

        // 1. Mark Assignment as REJECTED
        assignment.setStatus(ShipmentDriverAssignment.AssignmentStatus.REJECTED);
        assignmentRepository.save(assignment);

        // 2. Reset Shipment Status to CREATED (for re-assignment)
        Shipment shipment = assignment.getShipment();
        shipment.setStatus(Shipment.ShipmentStatus.CREATED);
        shipmentRepository.save(shipment);

        // 3. Reset Driver Status to AVAILABLE
        DriverProfile driver = assignment.getDriver();
        driver.setAvailabilityStatus(DriverProfile.AvailabilityStatus.AVAILABLE);
        driverRepository.save(driver);

        logger.info("Driver {} rejected assignment {}. Shipment {} is back to CREATED status.",
                driver.getId(), assignment.getId(), shipment.getId());
    }

    /**
     * Assign a driver to pick up a return from the receiver's location.
     */
    @Transactional
    public void assignReturnPickup(Shipment shipment) {
        String pincode = shipment.getReceiver().getAddress().getPostalCode(); // Receiver's address

        // Find drivers available in this pincode
        List<DriverProfile> availableDrivers = driverRepository.findByPincodeAndAvailabilityStatus(
                pincode, DriverProfile.AvailabilityStatus.AVAILABLE);

        if (!availableDrivers.isEmpty()) {
            DriverProfile driver = availableDrivers.get(0);

            ShipmentDriverAssignment assignment = ShipmentDriverAssignment.builder()
                    .shipment(shipment)
                    .driver(driver)
                    .assignmentType(ShipmentDriverAssignment.AssignmentType.RETURN_PICKUP)
                    .status(ShipmentDriverAssignment.AssignmentStatus.ASSIGNED)
                    .build();

            assignmentRepository.save(assignment);

            driver.setAvailabilityStatus(DriverProfile.AvailabilityStatus.ON_DELIVERY);
            driverRepository.save(driver);

            logger.info("Automatically assigned driver {} to RETURN_PICKUP for shipment {}", driver.getId(), shipment.getId());
        } else {
            logger.info("No available driver found for RETURN_PICKUP for shipment {} with pincode {}", shipment.getId(), pincode);
        }
    }

    /**
     * Complete a RETURN_PICKUP assignment and spawn a RETURN_DELIVERY.
     */
    @Transactional
    public void completeReturnPickup(Long assignmentId) {
        ShipmentDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getAssignmentType() != ShipmentDriverAssignment.AssignmentType.RETURN_PICKUP) {
            throw new RuntimeException("Invalid assignment type. Expected RETURN_PICKUP.");
        }

        assignment.setStatus(ShipmentDriverAssignment.AssignmentStatus.COMPLETED);
        assignmentRepository.save(assignment);

        Shipment shipment = assignment.getShipment();
        shipment.setStatus(Shipment.ShipmentStatus.RETURN_PICKED_UP);
        shipmentRepository.save(shipment);

        ShipmentDriverAssignment deliveryAssignment = ShipmentDriverAssignment.builder()
                .shipment(shipment)
                .driver(assignment.getDriver())
                .assignmentType(ShipmentDriverAssignment.AssignmentType.RETURN_DELIVERY)
                .status(ShipmentDriverAssignment.AssignmentStatus.ASSIGNED)
                .build();
        assignmentRepository.save(deliveryAssignment);

        logger.info("Completed RETURN_PICKUP for shipment {}. Created RETURN_DELIVERY assignment.", shipment.getId());
    }

    /**
     * Complete a RETURN_DELIVERY assignment.
     */
    @Transactional
    public void completeReturnDelivery(Long assignmentId) {
        ShipmentDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getAssignmentType() != ShipmentDriverAssignment.AssignmentType.RETURN_DELIVERY) {
            throw new RuntimeException("Invalid assignment type. Expected RETURN_DELIVERY.");
        }

        assignment.setStatus(ShipmentDriverAssignment.AssignmentStatus.COMPLETED);
        assignmentRepository.save(assignment);

        Shipment shipment = assignment.getShipment();
        shipment.setStatus(Shipment.ShipmentStatus.RETURN_DELIVERED);
        shipmentRepository.save(shipment);

        DriverProfile driver = assignment.getDriver();
        driver.setAvailabilityStatus(DriverProfile.AvailabilityStatus.AVAILABLE);
        driverRepository.save(driver);

        logger.info("Completed RETURN_DELIVERY for shipment {}. Driver {} is now AVAILABLE.", shipment.getId(), driver.getId());
    }
    public DriverEarningsSummaryDTO getDriverEarningsSummary(Long driverId) {
        List<ShipmentDriverAssignment> assignments = assignmentRepository.findByDriverId(driverId);
        List<ShipmentDriverAssignment> completed = assignments.stream()
                .filter(a -> a.getStatus() == ShipmentDriverAssignment.AssignmentStatus.COMPLETED
                        && a.getAssignmentType() == ShipmentDriverAssignment.AssignmentType.DELIVERY)
                .collect(Collectors.toList());

        BigDecimal today = BigDecimal.ZERO;
        BigDecimal week = BigDecimal.ZERO;
        BigDecimal month = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        List<DriverEarningHistoryDTO> history = completed.stream().map(a -> {
            DriverEarningHistoryDTO dto = new DriverEarningHistoryDTO();
            dto.setTrackingNumber(a.getShipment().getTrackingNumber());
            dto.setAssignmentType(a.getAssignmentType().name());
            dto.setCompletedAt(a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getAssignedAt());
            
            BigDecimal earned = a.getShipment().getTotalAmount().multiply(new BigDecimal("0.10"));
            dto.setEarnedAmount(earned);
            return dto;
        }).collect(Collectors.toList());

        for (DriverEarningHistoryDTO h : history) {
            LocalDate completedDate = h.getCompletedAt().toLocalDateTime().toLocalDate();
            if (completedDate.isEqual(now)) {
                today = today.add(h.getEarnedAmount());
            }
            if (completedDate.isAfter(now.minusDays(7))) {
                week = week.add(h.getEarnedAmount());
            }
            if (YearMonth.from(completedDate).equals(currentMonth)) {
                month = month.add(h.getEarnedAmount());
            }
        }

        DriverEarningsSummaryDTO summary = new DriverEarningsSummaryDTO();
        summary.setTodayEarnings(today);
        summary.setWeekEarnings(week);
        summary.setMonthEarnings(month);
        summary.setTotalDeliveries(completed.size());
        summary.setHistory(history);

        return summary;
    }
}
