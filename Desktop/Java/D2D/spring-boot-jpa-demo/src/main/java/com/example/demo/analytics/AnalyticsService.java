package com.example.demo.analytics;

import com.example.demo.driver.DriverProfile;
import com.example.demo.driver.DriverProfileRepository;
import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.shipment.ShipmentRepository;
import com.example.demo.userregistration.UserRegistrationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

        private final UserRegistrationRepository userRepository;
        private final ShipmentRepository shipmentRepository;
        private final DriverProfileRepository driverRepository;

        public AnalyticsService(UserRegistrationRepository userRepository,
                        ShipmentRepository shipmentRepository,
                        DriverProfileRepository driverRepository) {
                this.userRepository = userRepository;
                this.shipmentRepository = shipmentRepository;
                this.driverRepository = driverRepository;
        }

        public AdminDashboardDTO getDashboardData() {
                // 1. Total Active Users
                long totalUsers = userRepository.countByIsDeletedFalse();

                // 2. Total Delivered Orders
                long totalDelivered = shipmentRepository.countByStatus(Shipment.ShipmentStatus.DELIVERED);

                // 3. Active Drivers (AVAILABLE + ON_DELIVERY)
                long activeDrivers = driverRepository
                                .countByAvailabilityStatus(DriverProfile.AvailabilityStatus.AVAILABLE) +
                                driverRepository.countByAvailabilityStatus(
                                                DriverProfile.AvailabilityStatus.ON_DELIVERY);

                // 4. Revenue
                BigDecimal revenue = shipmentRepository.sumTotalAmountByStatus(Shipment.ShipmentStatus.DELIVERED);

                // 5. Driver Stats for Graph
                Map<String, Long> driverStats = new HashMap<>();
                driverStats.put("Available", activeDrivers);
                driverStats.put("Busy", driverRepository
                                .countByAvailabilityStatus(DriverProfile.AvailabilityStatus.ON_DELIVERY));
                driverStats.put("Offline",
                                driverRepository.countByAvailabilityStatus(DriverProfile.AvailabilityStatus.OFFLINE));

                // 6. Orders Graph Data (Actual last 7 days)
                List<Map<String, Object>> graphData = new ArrayList<>();
                LocalDate today = LocalDate.now();
                for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        LocalDateTime startOfDay = date.atStartOfDay();
                        LocalDateTime nextDay = date.plusDays(1).atStartOfDay();
                        
                        long count = shipmentRepository.countDeliveredShipmentsBetweenDates(
                                Timestamp.valueOf(startOfDay),
                                Timestamp.valueOf(nextDay)
                        );
                        
                        Map<String, Object> dayStat = new HashMap<>();
                        dayStat.put("date", date.toString()); // YYYY-MM-DD
                        dayStat.put("orders", count);
                        graphData.add(dayStat);
                }
                
                // 7. Damaged / Returned
                long damagedOrders = shipmentRepository.countByStatus(Shipment.ShipmentStatus.RETURN_REQUESTED) +
                                     shipmentRepository.countByStatus(Shipment.ShipmentStatus.RETURN_PICKED_UP) +
                                     shipmentRepository.countByStatus(Shipment.ShipmentStatus.RETURN_DELIVERED);

                return AdminDashboardDTO.builder()
                                .totalUsers(totalUsers)
                                .totalDeliveredOrders(totalDelivered)
                                .activeDrivers(activeDrivers)
                                .revenue(revenue)
                                .totalDamagedOrders(damagedOrders)
                                .driverStatusStats(driverStats)
                                .ordersGraphData(graphData)
                                .build();
        }

        public List<com.example.demo.userregistration.User> getActiveUsers() {
                return userRepository.findByIsDeletedFalse();
        }

        public List<DriverProfile> getActiveDrivers() {
                return driverRepository.findByAvailabilityStatusIn(List.of(
                                DriverProfile.AvailabilityStatus.AVAILABLE,
                                DriverProfile.AvailabilityStatus.ON_DELIVERY));
        }

        public List<Shipment> getDeliveredOrders() {
                return shipmentRepository.findByStatusWithDetails(Shipment.ShipmentStatus.DELIVERED);
        }

        public List<Shipment> getDamagedOrReturnedOrders() {
                return shipmentRepository.findByStatusInWithDetails(List.of(
                                Shipment.ShipmentStatus.RETURN_REQUESTED,
                                Shipment.ShipmentStatus.RETURN_PICKED_UP,
                                Shipment.ShipmentStatus.RETURN_DELIVERED));
        }
}
