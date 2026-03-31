package com.example.demo.userauthentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserAuthenticationController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(UserAuthenticationController.class);

    private final UserAuthenticationService userAuthenticationService;

    public UserAuthenticationController(UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String remoteAddr = httpRequest.getRemoteAddr();
        try {
            LoginResponse response = userAuthenticationService.login(request);
            logger.info("LOGIN SUCCESS | User: {} | IP: {}", request.getEmail(), remoteAddr);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("LOGIN FAILED | User: {} | IP: {} | Reason: {}", request.getEmail(), remoteAddr,
                    e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = userAuthenticationService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userAuthenticationService.forgotPassword(request);
            return ResponseEntity.ok("Password updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
