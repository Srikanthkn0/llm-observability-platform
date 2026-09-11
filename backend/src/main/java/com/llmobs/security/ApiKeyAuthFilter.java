package com.llmobs.security;

import com.llmobs.model.ApiKey;
import com.llmobs.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    public static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String rawKey = request.getHeader(API_KEY_HEADER);
        if (rawKey != null && !rawKey.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
            String prefix = rawKey.length() >= 12 ? rawKey.substring(0, 12) : rawKey;
            List<ApiKey> candidates = apiKeyRepository.findByKeyPrefixAndRevokedAtIsNull(prefix);
            for (ApiKey apiKey : candidates) {
                if (passwordEncoder.matches(rawKey, apiKey.getKeyHash())) {
                    apiKey.setLastUsedAt(Instant.now());
                    apiKeyRepository.save(apiKey);
                    ApiKeyPrincipal principal = new ApiKeyPrincipal(apiKey.getId(), apiKey.getProjectId(), apiKey.getKeyPrefix());
                    var auth = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_API_KEY")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    break;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
