package com.cabinet.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig - Configuration de sécurité Spring Security
 * 
 * TEMPORAIRE POUR TESTS:
 * Désactive la sécurité pour permettre les tests des endpoints
 * 
 * À L'ÉTAPE 6 (JWT):
 * Cette config sera remplacée par une vraie protection JWT
 * avec endpoints publics (/api/auth/*) et protégés
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuration de la chaîne de filtres de sécurité
     * 
     * CONFIGURATION ACTUELLE (temporaire):
     * - CSRF désactivé (pour tests POST/PUT/DELETE)
     * - Tous les endpoints autorisés (permitAll)
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactiver CSRF temporairement
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Autoriser TOUS les endpoints
                );

        return http.build();
    }
}