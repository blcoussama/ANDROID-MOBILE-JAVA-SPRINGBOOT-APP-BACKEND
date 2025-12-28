package com.cabinet.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration PasswordEncoder
 *
 * BCrypt pour hashing passwords sécurisé
 * Coût: 10 (par défaut, ~100ms par hash)
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean PasswordEncoder (BCrypt)
     * Utilisé par AuthService pour hasher/vérifier passwords
     *
     * BCrypt génère automatiquement salt unique
     * Irreversible (pas de decrypt possible)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
