package com.example.demo.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalDeliveredOrders;
    private long activeDrivers;
    private BigDecimal revenue;
    private long totalDamagedOrders;

    // For graphs: e.g., orders per day for the current month
    private List<Map<String, Object>> ordersGraphData;

    // For graphs: e.g., driver distribution by status
    private Map<String, Long> driverStatusStats;
}
