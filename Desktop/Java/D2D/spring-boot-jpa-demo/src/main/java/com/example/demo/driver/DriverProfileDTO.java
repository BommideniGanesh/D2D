package com.example.demo.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfileDTO {
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String state;
    private String pincode;
    private String phoneNumber;
    private String licenseNumber;
    private String licenseType;
    private Date licenseExpiryDate;
    private String vehicleNumber;
    private String vehicleType;
    private String vehicleModel;
    private String vehicleColor;
    private boolean isActive;
    private boolean isVerified;
    private String availabilityStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
