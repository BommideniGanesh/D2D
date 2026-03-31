package com.example.demo.userregistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/otp")
public class OtpController {

    @Autowired
    private EmailOtpService emailOtpService;

    @Autowired
    private UserRegistrationRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        if (userRepository.existsByEmail(email)) {
             return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        String phone = request.get("phone");
        if (phone != null && !phone.trim().isEmpty()) {
             if (userRepository.existsByPhone(phone)) {
                  return ResponseEntity.badRequest().body(Map.of("message", "Phone already registered"));
             }
        }

        try {
            emailOtpService.sendOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body("Email and code are required");
        }

        boolean isValid = emailOtpService.verifyOtp(email, code);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
