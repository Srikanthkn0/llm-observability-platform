package com.llmobs.service;

import com.llmobs.dto.AuthResponse;
import com.llmobs.dto.LoginRequest;
import com.llmobs.exception.ApiException;
import com.llmobs.repository.UserRepository;
import com.llmobs.security.JwtService;
import com.llmobs.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthResponse login(LoginRequest request, String ip) {
        var user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        auditService.log(user.getId(), null, "AUTH_LOGIN", "user", user.getId().toString(), null, ip);
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .build();
    }
}
