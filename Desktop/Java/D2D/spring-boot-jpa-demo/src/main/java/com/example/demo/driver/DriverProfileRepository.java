package com.example.demo.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
        Optional<DriverProfile> findByUserId(String userId);

        Optional<DriverProfile> findByLicenseNumber(String licenseNumber);

        Optional<DriverProfile> findByVehicleNumber(String vehicleNumber);

        boolean existsByLicenseNumber(String licenseNumber);

        boolean existsByVehicleNumber(String vehicleNumber);

        List<DriverProfile> findByPincodeAndAvailabilityStatus(String pincode,
                        DriverProfile.AvailabilityStatus availabilityStatus);

        long countByAvailabilityStatus(DriverProfile.AvailabilityStatus availabilityStatus);

        List<DriverProfile> findByAvailabilityStatusIn(List<DriverProfile.AvailabilityStatus> statuses);
}
