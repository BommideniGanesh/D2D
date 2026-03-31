package com.example.demo.driver;

import com.example.demo.userregistration.User;
import com.example.demo.userregistration.UserRegistrationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRegistrationRepository userRepository;

    @Transactional
    public DriverProfileDTO createDriverProfile(DriverProfileDTO dto) {
        // 1. Validate User - handle both email and user ID
        User user;
        if (dto.getUserId().contains("@")) {
            // It's an email, look up by email
            user = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals(dto.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + dto.getUserId()));
        } else {
            // It's a user ID
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.getUserId()));
        }

        // 2. Check Uniqueness
        if (driverProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already exists");
        }
        if (driverProfileRepository.existsByVehicleNumber(dto.getVehicleNumber())) {
            throw new IllegalArgumentException("Vehicle number already exists");
        }
        if (driverProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalArgumentException("Driver profile already exists for this user");
        }

        // 3. Map DTO to Entity
        DriverProfile driverProfile = mapToEntity(dto, user);

        // 4. Save
        DriverProfile savedProfile = driverProfileRepository.save(driverProfile);

        // 5. Map back to DTO
        return mapToDTO(savedProfile);
    }

    @Transactional(readOnly = true)
    public DriverProfileDTO getDriverProfileById(Long id) {
        DriverProfile driverProfile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver profile not found with ID: " + id));
        return mapToDTO(driverProfile);
    }

    @Transactional(readOnly = true)
    public DriverProfileDTO getDriverProfileByUserId(String userId) {
        DriverProfile driverProfile = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Driver profile not found for user ID: " + userId));
        return mapToDTO(driverProfile);
    }

    @Transactional
    public DriverProfileDTO updateDriverProfile(Long id, DriverProfileDTO dto) {
        DriverProfile existingProfile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver profile not found with ID: " + id));

        // Update fields
        existingProfile.setFirstName(dto.getFirstName());
        existingProfile.setLastName(dto.getLastName());
        existingProfile.setAddressLine1(dto.getAddressLine1());
        existingProfile.setAddressLine2(dto.getAddressLine2());
        existingProfile.setState(dto.getState());
        existingProfile.setPincode(dto.getPincode());
        existingProfile.setPhoneNumber(dto.getPhoneNumber());
        existingProfile.setLicenseType(dto.getLicenseType());
        existingProfile.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        existingProfile.setVehicleType(DriverProfile.VehicleType.valueOf(dto.getVehicleType()));
        existingProfile.setVehicleModel(dto.getVehicleModel());
        existingProfile.setVehicleColor(dto.getVehicleColor());
        existingProfile.setAvailabilityStatus(DriverProfile.AvailabilityStatus.valueOf(dto.getAvailabilityStatus()));

        // Note: License Number and Vehicle Number usually shouldn't be updated easily
        // or require re-verification
        // skipping them for simple update, or handle with care.

        DriverProfile savedProfile = driverProfileRepository.save(existingProfile);
        return mapToDTO(savedProfile);
    }

    @Transactional
    public DriverProfileDTO verifyDriver(Long id) {
        DriverProfile driverProfile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver profile not found with ID: " + id));

        driverProfile.setVerified(true);
        DriverProfile savedProfile = driverProfileRepository.save(driverProfile);
        return mapToDTO(savedProfile);
    }

    // Mappers
    private DriverProfile mapToEntity(DriverProfileDTO dto, User user) {
        return DriverProfile.builder()
                .user(user)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .phoneNumber(dto.getPhoneNumber())
                .licenseNumber(dto.getLicenseNumber())
                .licenseType(dto.getLicenseType())
                .licenseExpiryDate(dto.getLicenseExpiryDate())
                .vehicleNumber(dto.getVehicleNumber())
                .vehicleType(DriverProfile.VehicleType.valueOf(dto.getVehicleType()))
                .vehicleModel(dto.getVehicleModel())
                .vehicleColor(dto.getVehicleColor())
                .availabilityStatus(DriverProfile.AvailabilityStatus.valueOf(dto.getAvailabilityStatus()))
                .isActive(true) // Default
                .isVerified(false) // Default
                .build();
    }

    private DriverProfileDTO mapToDTO(DriverProfile entity) {
        return DriverProfileDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .state(entity.getState())
                .pincode(entity.getPincode())
                .phoneNumber(entity.getPhoneNumber())
                .licenseNumber(entity.getLicenseNumber())
                .licenseType(entity.getLicenseType())
                .licenseExpiryDate(entity.getLicenseExpiryDate())
                .vehicleNumber(entity.getVehicleNumber())
                .vehicleType(entity.getVehicleType().name())
                .vehicleModel(entity.getVehicleModel())
                .vehicleColor(entity.getVehicleColor())
                .isActive(entity.isActive())
                .isVerified(entity.isVerified())
                .availabilityStatus(entity.getAvailabilityStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
