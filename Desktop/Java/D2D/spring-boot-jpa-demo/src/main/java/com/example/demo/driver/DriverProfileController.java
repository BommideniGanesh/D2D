package com.example.demo.driver;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverProfileController {

    private final DriverProfileService driverProfileService;

    @PostMapping
    public ResponseEntity<DriverProfileDTO> createDriverProfile(@RequestBody DriverProfileDTO dto) {
        return ResponseEntity.ok(driverProfileService.createDriverProfile(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverProfileDTO> getDriverProfile(@PathVariable Long id) {
        return ResponseEntity.ok(driverProfileService.getDriverProfileById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DriverProfileDTO> getDriverProfileByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(driverProfileService.getDriverProfileByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverProfileDTO> updateDriverProfile(@PathVariable Long id,
            @RequestBody DriverProfileDTO dto) {
        return ResponseEntity.ok(driverProfileService.updateDriverProfile(id, dto));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<DriverProfileDTO> verifyDriver(@PathVariable Long id) {
        return ResponseEntity.ok(driverProfileService.verifyDriver(id));
    }
}
