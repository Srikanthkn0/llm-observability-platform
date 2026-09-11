package com.llmobs.config;

import com.llmobs.model.*;
import com.llmobs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataRunner implements CommandLineRunner {
    public static final String DEMO_API_KEY = "llmobs_demo_seed_key_do_not_use_in_prod";
    public static final String ADMIN_EMAIL = "admin@llmobs.local";
    public static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("Seed data already present");
            return;
        }

        User admin = userRepository.save(User.builder()
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .displayName("Demo Admin")
                .role(Role.ADMIN)
                .build());

        User developer = userRepository.save(User.builder()
                .email("dev@llmobs.local")
                .passwordHash(passwordEncoder.encode("dev123"))
                .displayName("Demo Developer")
                .role(Role.DEVELOPER)
                .build());

        Project project = projectRepository.save(Project.builder()
                .name("Demo Project")
                .slug("demo")
                .description("Seeded demo project for LLM observability")
                .ownerId(admin.getId())
                .build());

        membershipRepository.save(ProjectMembership.builder()
                .projectId(project.getId())
                .userId(admin.getId())
                .role(Role.ADMIN)
                .build());
        membershipRepository.save(ProjectMembership.builder()
                .projectId(project.getId())
                .userId(developer.getId())
                .role(Role.DEVELOPER)
                .build());

        String prefix = DEMO_API_KEY.substring(0, 12);
        apiKeyRepository.save(ApiKey.builder()
                .projectId(project.getId())
                .name("Demo Seed Key")
                .keyPrefix(prefix)
                .keyHash(passwordEncoder.encode(DEMO_API_KEY))
                .createdBy(admin.getId())
                .build());

        alertRuleRepository.save(AlertRule.builder()
                .projectId(project.getId())
                .name("High cost window")
                .metric("cost_usd")
                .operator(">")
                .threshold(new BigDecimal("1.00"))
                .windowMinutes(60)
                .enabled(true)
                .build());

        log.info("============================================================");
        log.info("SEED READY");
        log.info("  Admin login : {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
        log.info("  Dev login   : dev@llmobs.local / dev123");
        log.info("  Project     : {} ({})", project.getName(), project.getId());
        log.info("  API key     : {}", DEMO_API_KEY);
        log.info("============================================================");
    }
}
