// src/main/java/com/guardianvision/backend/config/SecurityConfig.java
package com.guardianvision.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity, consider enabling for production with proper handling
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Integrate CORS
                .authorizeHttpRequests(auth -> auth
                        // Permitting specific paths
                        .requestMatchers("/api/admins/**").permitAll()
                        .requestMatchers("/api/audio/**").permitAll()
                        .requestMatchers("/api/caregivers/**").permitAll()
                        .requestMatchers("/api/alerts/**").permitAll()
                        .requestMatchers("/api/outages/**").permitAll()
                        .requestMatchers("/api/patients/**").permitAll()
                        .requestMatchers("/api/admins/login").permitAll()
                        .requestMatchers("/api/admins/me").permitAll()
                        .requestMatchers("/api/caregivers/login").permitAll()
                        .requestMatchers("/api/caregivers/me").permitAll()
                        .requestMatchers("/api/patients/login").permitAll()
                        .requestMatchers("/api/patients/me").permitAll()
                        .requestMatchers("/api/audio/alert").permitAll()
                        .anyRequest().permitAll() // Allow all other requests for now, adjust as needed for security
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // --- IMPORTANT CHANGES HERE ---
        // 1. Add your PROD HTTPS frontend URL
        // 2. Keep localhost for local development
        // 3. Remove HTTP production/IP URLs as they will be redirected to HTTPS by Nginx
        // 4. Remove "null" unless explicitly needed for non-browser clients (rare for this scenario)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // For local development with your frontend
                "http://localhost:5173",   // For local development with Vite/similar
                "https://guardianvision.duckdns.org" // <--- The PRIMARY PRODUCTION FRONTEND URL (HTTPS!)
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization")); // Authorization header is good to include
        config.setAllowCredentials(true); // <--- Absolutely ESSENTIAL for cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply this CORS config to all paths

        return source;
    }
}