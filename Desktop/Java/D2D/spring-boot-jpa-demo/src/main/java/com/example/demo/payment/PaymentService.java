package com.example.demo.payment;

import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.shipment.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          ShipmentRepository shipmentRepository) {
        this.paymentRepository = paymentRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public Payment initiatePayment(Long shipmentId, String method) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));

        // Prevent duplicate payment
        Optional<Payment> existing = paymentRepository.findByShipment_Id(shipmentId);
        if (existing.isPresent() && existing.get().getStatus() == Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Shipment already paid.");
        }

        Payment payment = Payment.builder()
                .shipment(shipment)
                .amount(shipment.getTotalAmount())
                .paymentMethod(Payment.PaymentMethod.valueOf(method.toUpperCase()))
                .status(Payment.PaymentStatus.PENDING)
                .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaidAt(Timestamp.from(Instant.now()));
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long shipmentId) {
        Payment payment = paymentRepository.findByShipment_Id(shipmentId)
                .orElseThrow(() -> new RuntimeException("No payment found for shipment: " + shipmentId));
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getByShipmentId(Long shipmentId) {
        return paymentRepository.findByShipment_Id(shipmentId);
    }
}
