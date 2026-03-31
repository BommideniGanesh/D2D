package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner run(javax.sql.DataSource dataSource) {
        return args -> {
            try (java.sql.Connection connection = dataSource.getConnection()) {
                System.out.println("Wait for it...");
                System.out.println("Wait for it...");
                System.out.println("SUCCESS: Connected to the database: " + connection.getCatalog());
            } catch (Exception e) {
                System.out.println("ERROR: Failed to connect to the database");
                e.printStackTrace();
            }
        };
    }

}
