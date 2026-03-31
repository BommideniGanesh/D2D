package com.example.demo.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DatabasePatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabasePatchConfig.class);

    @Bean
    public CommandLineRunner patchDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            logger.info("Executing database patch to update shipments status enum...");
            try {
                // Fix for "Data truncated" and "Check constraint violated" errors
                // We drop the constraints to allow the application (Enums) to handle validation
                try {
                    jdbcTemplate.execute("ALTER TABLE shipments DROP CHECK shipments_chk_1");
                } catch (Exception e) {
                    /* Ignore if not exists */ }

                try {
                    jdbcTemplate.execute("ALTER TABLE shipments DROP CHECK shipments_chk_2");
                } catch (Exception e) {
                    /* Ignore if not exists */ }

                try {
                    jdbcTemplate.execute("ALTER TABLE shipments DROP CHECK shipments_chk_3");
                } catch (Exception e) {
                    /* Ignore if not exists */ }

                // Ensure columns are wide enough
                jdbcTemplate.execute("ALTER TABLE shipments MODIFY COLUMN status VARCHAR(50)");
                jdbcTemplate.execute("ALTER TABLE shipments MODIFY COLUMN payment_mode VARCHAR(50)");
                jdbcTemplate.execute("ALTER TABLE shipments MODIFY COLUMN source VARCHAR(50)");

                logger.info("Database patch executed successfully.");

            } catch (Exception e) {
                logger.warn("Database patch might have failed or was not needed: {}", e.getMessage());
            }
        };
    }
}
