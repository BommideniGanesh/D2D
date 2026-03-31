package com.example.demo.driver.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DriverEarningsSummaryDTO {
    private BigDecimal todayEarnings;
    private BigDecimal weekEarnings;
    private BigDecimal monthEarnings;
    private int totalDeliveries;
    private List<DriverEarningHistoryDTO> history;
}
