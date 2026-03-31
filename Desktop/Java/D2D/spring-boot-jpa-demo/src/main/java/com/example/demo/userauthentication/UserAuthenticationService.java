package com.example.demo.userauthentication;

import com.example.demo.userregistration.User;
import com.example.demo.userregistration.UserRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final UserRegistrationRepository userRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // 1. Find user by email
        // Note: Using stream since repository method missing findByEmail
        User user = userRegistrationRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 3. Generate Tokens
        java.util.List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(java.util.stream.Collectors.toList());
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            // Optionally verify user still exists in DB

            // In a real app, you should fetch the user's current roles from DB here to
            // ensure they haven't changed.
            // For now, we'll pass an empty list or fetch user.
            // Let's assume we want to refresh roles too.
            // Since we only have username, we need to find user.
            User user = userRegistrationRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals(username))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("User not found"));

            java.util.List<String> roles = user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(java.util.stream.Collectors.toList());

            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), username, roles);
            // Return same refresh token (rotating refresh tokens is better security but
            // simpler here)
            // Or generate new one. Let's keep existing refresh token to indicate session
            // continuity.
            // Actually, usually you return a new access token.
            return new LoginResponse(newAccessToken, token);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRegistrationRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRegistrationRepository.save(user);
    }
}
