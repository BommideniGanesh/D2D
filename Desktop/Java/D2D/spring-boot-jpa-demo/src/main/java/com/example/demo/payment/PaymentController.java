package com.example.demo.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestBody Map<String, Object> body) {
        try {
            Long shipmentId = Long.valueOf(body.get("shipmentId").toString());
            String method = body.getOrDefault("paymentMethod", "CARD").toString();
            Payment payment = paymentService.initiatePayment(shipmentId, method);
            return ResponseEntity.ok(Map.of(
                    "id", payment.getId(),
                    "status", payment.getStatus(),
                    "amount", payment.getAmount(),
                    "transactionRef", payment.getTransactionRef(),
                    "paymentMethod", payment.getPaymentMethod()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.confirmPayment(id);
            return ResponseEntity.ok(Map.of(
                    "id", payment.getId(),
                    "status", payment.getStatus(),
                    "paidAt", payment.getPaidAt() != null ? payment.getPaidAt().toString() : null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<?> getPaymentByShipment(@PathVariable Long shipmentId) {
        return paymentService.getByShipmentId(shipmentId)
                .map(p -> ResponseEntity.ok((Object) Map.of(
                        "id", p.getId(),
                        "status", p.getStatus(),
                        "amount", p.getAmount(),
                        "paymentMethod", p.getPaymentMethod(),
                        "transactionRef", p.getTransactionRef() != null ? p.getTransactionRef() : "",
                        "paidAt", p.getPaidAt() != null ? p.getPaidAt().toString() : null
                )))
                .orElse(ResponseEntity.ok(Map.of("status", "NONE")));
    }

    @PostMapping("/refund/{shipmentId}")
    public ResponseEntity<?> refundPayment(@PathVariable Long shipmentId) {
        try {
            Payment payment = paymentService.refundPayment(shipmentId);
            return ResponseEntity.ok(Map.of("id", payment.getId(), "status", payment.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
