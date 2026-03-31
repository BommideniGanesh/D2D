package com.example.demo.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboardData() {
        AdminDashboardDTO dashboardData = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/users/active")
    public ResponseEntity<java.util.List<com.example.demo.userregistration.User>> getActiveUsers() {
        return ResponseEntity.ok(analyticsService.getActiveUsers());
    }

    @GetMapping("/drivers/active")
    public ResponseEntity<java.util.List<com.example.demo.driver.DriverProfile>> getActiveDrivers() {
        return ResponseEntity.ok(analyticsService.getActiveDrivers());
    }

    @GetMapping("/shipments/delivered")
    public ResponseEntity<java.util.List<com.example.demo.orders.shipment.Shipment>> getDeliveredOrders() {
        return ResponseEntity.ok(analyticsService.getDeliveredOrders());
    }

    @GetMapping("/shipments/damaged-returns")
    public ResponseEntity<java.util.List<com.example.demo.orders.shipment.Shipment>> getDamagedOrReturnedOrders() {
        return ResponseEntity.ok(analyticsService.getDamagedOrReturnedOrders());
    }
}
