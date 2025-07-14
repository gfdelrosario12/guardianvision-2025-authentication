package com.guardianvision.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // Admin endpoints
                                "/admins/login",
                                "/admins/ping",
                                "/admins",

                                // Caregiver endpoints
                                "/caregivers/login",
                                "/caregivers/ping",
                                "/caregivers",

                                // Patient endpoints
                                "/patients/login",
                                "/patients/ping",
                                "/patients",
                                "/patients/caregiver/**",

                                // Alerts
                                "/alerts/patient/**",
                                "/alerts", // POST

                                // Outages
                                "/outages/patient/**",
                                "/outages", // POST

                                // Audio alert endpoint
                                "/audio/alert",

                                // S3 upload (public presigned request)
                                "/s3/presign"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
