package com.project.LibManager.config;

import java.util.HashSet;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.LibManager.constant.PredefinedRole;
import com.project.LibManager.entity.Role;
import com.project.LibManager.entity.User;
import com.project.LibManager.repository.RoleRepository;
import com.project.LibManager.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;
    @Bean
    public ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {
            if(userRepository.findByEmail("admin@gmail.com")==null) {
                roleRepository.save(Role.builder()
                        .name(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin123"))
                        .isVerified(true)
                        .fullName("ADMIN")
                        .isDeleted(false)
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin123, please change it");
            }
            log.info("Application initialization completed .....");
        };
    }
}
