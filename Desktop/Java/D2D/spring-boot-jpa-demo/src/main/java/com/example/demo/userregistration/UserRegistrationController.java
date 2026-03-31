package com.example.demo.userregistration;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    public UserRegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping("/register")
    public org.springframework.http.ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            User user = userRegistrationService.registerUser(request);
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "User created successfully");
            response.put("user", user);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Email already exists") || e.getMessage().equals("Phone already exists")) {
                java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
                errorResponse.put("message", e.getMessage());
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                        .body(errorResponse);
            }
            return org.springframework.http.ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        return userRegistrationService.getUserById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody UserRegistrationRequest request) {
        return userRegistrationService.updateUser(id, request);
    }

    @PutMapping("/profile")
    public org.springframework.http.ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            User currentUser = userRegistrationService.getUserByEmail(email);
            User updatedUser = userRegistrationService.updateProfile(currentUser.getId(), request);
            return org.springframework.http.ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userRegistrationService.deleteUser(id);
    }
}
