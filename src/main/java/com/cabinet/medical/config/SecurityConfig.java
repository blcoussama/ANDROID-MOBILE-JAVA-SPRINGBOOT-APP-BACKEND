package com.cabinet.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * Configuration Spring Security
 *
 * Configuration JWT stateless (pas de sessions)
 * CORS activé pour Android
 * Routes publiques: /api/auth/**
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuration Security Filter Chain
     * Définit les règles d'accès aux endpoints
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (pas nécessaire pour API REST stateless)
                .csrf(csrf -> csrf.disable())

                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configuration autorisation
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques (pas d'authentification requise)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()

                        // Toutes autres routes nécessitent authentification
                        .anyRequest().authenticated())

                // Session stateless (JWT, pas de sessions serveur)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * Configuration CORS
     * Permet requêtes depuis app Android
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origines autorisées (Android localhost + émulateur)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8081", // Android Metro
                "http://10.0.2.2:8080", // Android Emulator → localhost
                "http://172.25.135.62:8080", // WSL2 IP
                "*" // Développement (à restreindre en production!)
        ));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"));

        // Credentials autorisés (cookies, Authorization header)
        configuration.setAllowCredentials(true);

        // Durée cache preflight (OPTIONS)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
