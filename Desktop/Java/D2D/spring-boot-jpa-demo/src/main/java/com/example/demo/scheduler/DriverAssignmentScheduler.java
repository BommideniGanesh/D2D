package com.example.demo.scheduler;

import com.example.demo.assignment.ShipmentDriverAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DriverAssignmentScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DriverAssignmentScheduler.class);

    @Autowired
    private ShipmentDriverAssignmentService assignmentService;

    /**
     * Scheduled job that runs every 5 minutes to assign pickup drivers.
     * Uses fixedDelay to ensure the job completes before the next execution.
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes = 300,000 milliseconds
    public void assignPickupDrivers() {
        logger.info("=== Starting scheduled driver assignment job ===");

        try {
            int assignedCount = assignmentService.executeBatchAssignment();

            if (assignedCount > 0) {
                logger.info("Successfully assigned {} shipments to drivers", assignedCount);
            } else {
                logger.debug("No shipments assigned in this batch cycle");
            }

        } catch (Exception e) {
            logger.error("Error in scheduled driver assignment job: {}", e.getMessage(), e);
        }

        logger.info("=== Completed scheduled driver assignment job ===");
    }
}
