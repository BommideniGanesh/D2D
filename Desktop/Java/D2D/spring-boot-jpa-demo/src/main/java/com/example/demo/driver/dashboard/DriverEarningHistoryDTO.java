package com.example.demo.driver.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class DriverEarningHistoryDTO {
    private String trackingNumber;
    private String assignmentType;
    private Timestamp completedAt;
    private BigDecimal earnedAmount;
}
