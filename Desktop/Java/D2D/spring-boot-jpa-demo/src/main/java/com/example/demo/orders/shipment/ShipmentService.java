package com.example.demo.orders.shipment;

import com.example.demo.orders.packagedetails.PackageDetails;
import com.example.demo.orders.packagedetails.PackageDetailsRepository;
import com.example.demo.orders.receiverdetails.ReceiverDetails;
import com.example.demo.orders.receiverdetails.ReceiverDetailsRepository;
import com.example.demo.orders.senderdetails.SenderDetails;
import com.example.demo.orders.senderdetails.SenderDetailsRepository;
import com.example.demo.assignment.ShipmentDriverAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

        private final ShipmentRepository shipmentRepository;
        private final SenderDetailsRepository senderDetailsRepository;
        private final ReceiverDetailsRepository receiverDetailsRepository;
        private final com.example.demo.orders.packagedetails.PackageDetailsRepository packageDetailsRepository;

        private final com.example.demo.assignment.ShipmentDriverAssignmentService assignmentService;
        private final com.example.demo.pricing.PricingEngineService pricingEngineService;
        private final com.example.demo.userregistration.UserRegistrationRepository userRepository;
        private final com.example.demo.billing.WalletService walletService;
        private final org.springframework.context.ApplicationEventPublisher eventPublisher;

        public ShipmentService(ShipmentRepository shipmentRepository,
                        SenderDetailsRepository senderDetailsRepository,
                        ReceiverDetailsRepository receiverDetailsRepository,
                        PackageDetailsRepository packageDetailsRepository,
                        ShipmentDriverAssignmentService assignmentService,
                        com.example.demo.pricing.PricingEngineService pricingEngineService,
                        com.example.demo.userregistration.UserRegistrationRepository userRepository,
                        com.example.demo.billing.WalletService walletService,
                        org.springframework.context.ApplicationEventPublisher eventPublisher) {
                this.shipmentRepository = shipmentRepository;
                this.senderDetailsRepository = senderDetailsRepository;
                this.receiverDetailsRepository = receiverDetailsRepository;
                this.packageDetailsRepository = packageDetailsRepository;
                this.assignmentService = assignmentService;
                this.pricingEngineService = pricingEngineService;
                this.userRepository = userRepository;
                this.walletService = walletService;
                this.eventPublisher = eventPublisher;
        }

        @Transactional
        public ShipmentDTO createShipment(ShipmentDTO shipmentDTO) {
                // Validation: Check if referenced entities exist
                if (shipmentDTO.getSenderId() == null || shipmentDTO.getSenderId().isEmpty()) {
                        throw new IllegalArgumentException("Sender ID cannot be null or empty");
                }
                if (shipmentDTO.getReceiverId() == null || shipmentDTO.getReceiverId().isEmpty()) {
                        throw new IllegalArgumentException("Receiver ID cannot be null or empty");
                }
                if (shipmentDTO.getPackageId() == null) {
                        throw new IllegalArgumentException("Package ID cannot be null");
                }

                SenderDetails sender = senderDetailsRepository.findById(shipmentDTO.getSenderId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Sender not found with ID: " + shipmentDTO.getSenderId()));

                ReceiverDetails receiver = receiverDetailsRepository.findById(shipmentDTO.getReceiverId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Receiver not found with ID: " + shipmentDTO.getReceiverId()));

                PackageDetails pkg = packageDetailsRepository.findById(shipmentDTO.getPackageId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Package not found with ID: " + shipmentDTO.getPackageId()));

                // Calculate dynamic pricing based on package dimensions, weight, and mock weather
                pricingEngineService.calculateAndApplyPricing(shipmentDTO, pkg, sender.getPostalCode());

                // Generate Tracking Number if not provided
                String trackingNumber = shipmentDTO.getTrackingNumber();
                if (trackingNumber == null || trackingNumber.isEmpty()) {
                        trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                }

                Shipment shipment = Shipment.builder()
                                .sender(sender)
                                .receiver(receiver)
                                .packageDetails(pkg)
                                .baseShippingCost(shipmentDTO.getBaseShippingCost())
                                .taxAmount(shipmentDTO.getTaxAmount())
                                .insuranceAmount(shipmentDTO.getInsuranceAmount())
                                .discountAmount(shipmentDTO.getDiscountAmount())
                                .totalAmount(shipmentDTO.getTotalAmount())
                                .currency(shipmentDTO.getCurrency())
                                .paymentMode(shipmentDTO.getPaymentMode())
                                .insured(shipmentDTO.isInsured())
                                .insuranceProvider(shipmentDTO.getInsuranceProvider())
                                .signatureRequired(shipmentDTO.isSignatureRequired())
                                .ageRestrictionDetails(shipmentDTO.getAgeRestrictionDetails() != null
                                                ? shipmentDTO.getAgeRestrictionDetails()
                                                : new AgeRestrictionDetails())
                                .trackingNumber(trackingNumber)
                                .status(Shipment.ShipmentStatus.CREATED) // Default status
                                .createdBy(shipmentDTO.getCreatedBy() != null ? shipmentDTO.getCreatedBy() : "SYSTEM")
                                .userId(shipmentDTO.getUserId())
                                .source(shipmentDTO.getSource() != null ? shipmentDTO.getSource()
                                                : Shipment.ShipmentSource.API)
                                .history(List.of(Map.of(
                                                "status", "CREATED",
                                                "timestamp", Instant.now().toString(),
                                                "description", "Shipment created")))
                                .build();

                Shipment savedShipment = shipmentRepository.save(shipment);

                eventPublisher.publishEvent(new com.example.demo.events.ShipmentStatusChangedEvent(
                        this, savedShipment.getId(), savedShipment.getUserId(), "NONE", "CREATED", savedShipment.getTrackingNumber()));

                // B2B Wallet Deduction Check
                if (shipmentDTO.getUserId() != null) {
                        try {
                                com.example.demo.userregistration.User user = userRepository.findById(shipmentDTO.getUserId()).orElse(null);
                                if (user != null) {
                                        walletService.getB2BClient(user).ifPresent(client -> {
                                                walletService.chargeWalletForShipment(client, savedShipment, savedShipment.getTotalAmount());
                                        });
                                }
                        } catch (Exception e) {
                                // Ignore issues
                        }
                }

                // Attempt to assign a driver immediately
                assignmentService.assignDriverToShipment(savedShipment);

                return mapToDTO(savedShipment);
        }

        public ShipmentDTO getShipmentById(Long id) {
                Shipment shipment = shipmentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + id));
                return mapToDTO(shipment);
        }

        public ShipmentDTO getShipmentByTrackingNumber(String trackingNumber) {
                Shipment shipment = shipmentRepository.findByTrackingNumberWithDetails(trackingNumber)
                                .orElseThrow(() -> new RuntimeException(
                                                "Shipment not found with tracking number: " + trackingNumber));
                return mapToDTO(shipment);
        }

        public List<ShipmentDTO> getAllShipments() {
                return shipmentRepository.findAllWithDetails().stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        public List<ShipmentDTO> getAllShipmentsByUserId(String userId) {
                return shipmentRepository.findAllByUserIdWithDetails(userId).stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        public List<ShipmentDTO> getPendingShipments() {
                return shipmentRepository.findByStatusWithDetails(Shipment.ShipmentStatus.CREATED).stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        private ShipmentDTO mapToDTO(Shipment shipment) {
                return ShipmentDTO.builder()
                                .id(shipment.getId())
                                .senderId(shipment.getSender().getId())
                                .receiverId(shipment.getReceiver().getId())
                                .packageId(shipment.getPackageDetails().getId())
                                // In a real app, we might map the full sender/receiver/package DTOs here
                                .senderDetails(shipment.getSender())
                                .receiverDetails(shipment.getReceiver())
                                .packageDetails(shipment.getPackageDetails())
                                .baseShippingCost(shipment.getBaseShippingCost())
                                .taxAmount(shipment.getTaxAmount())
                                .insuranceAmount(shipment.getInsuranceAmount())
                                .discountAmount(shipment.getDiscountAmount())
                                .totalAmount(shipment.getTotalAmount())
                                .currency(shipment.getCurrency())
                                .paymentMode(shipment.getPaymentMode())
                                .insured(shipment.isInsured())
                                .insuranceProvider(shipment.getInsuranceProvider())
                                .signatureRequired(shipment.isSignatureRequired())
                                .ageRestrictionDetails(shipment.getAgeRestrictionDetails())
                                .trackingNumber(shipment.getTrackingNumber())
                                .status(shipment.getStatus())
                                .lastUpdated(shipment.getLastUpdated())
                                .history(shipment.getHistory())
                                .createdBy(shipment.getCreatedBy())
                                .userId(shipment.getUserId())
                                .createdAt(shipment.getCreatedAt())
                                .updatedAt(shipment.getUpdatedAt())
                                .source(shipment.getSource())
                                .build();
        }
}
