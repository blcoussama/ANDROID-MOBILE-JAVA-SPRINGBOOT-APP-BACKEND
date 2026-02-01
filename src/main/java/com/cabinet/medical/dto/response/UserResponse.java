package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserResponse - DTO pour les informations d'un utilisateur
 *
 * UTILISATION:
 * - UC-A03: Admin liste tous les utilisateurs
 * - UC-A04: Admin crée un utilisateur
 * - UC-A06: Admin modifie un utilisateur
 *
 * SÉCURITÉ:
 * ⚠️ JAMAIS inclure passwordHash dans ce DTO !
 * Password JAMAIS exposé au client !
 *
 * DIFFÉRENCE AVEC LoginResponse:
 * - UserResponse: Infos complètes user (pour admin)
 * - LoginResponse: Infos + JWT token (pour login)
 *
 * EXEMPLE JSON:
 * {
 * "id": 1,
 * "email": "jean@gmail.com",
 * "firstName": "Jean",
 * "lastName": "Dupont",
 * "phone": "0612345678",
 * "role": "PATIENT",
 * "createdAt": "2025-12-29T10:00:00",
 * "lastLoginAt": "2025-12-29T10:05:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * ID de l'utilisateur
     */
    private Long id;

    /**
     * Email de l'utilisateur (login)
     */
    private String email;

    /**
     * Prénom
     */
    private String firstName;

    /**
     * Nom
     */
    private String lastName;

    /**
     * Téléphone (optionnel)
     */
    private String phone;

    /**
     * Rôle de l'utilisateur
     *
     * VALEURS: "PATIENT", "DOCTOR", "ADMIN"
     */
    private String role;

    /**
     * Date de création du compte
     */
    private LocalDateTime createdAt;

    /**
     * Date de dernière connexion
     */
    private LocalDateTime lastLoginAt;

    /**
     * Spécialité (pour les médecins uniquement)
     *
     * VALEURS:
     * - Non null si role = "DOCTOR"
     * - null si role = "PATIENT" ou "ADMIN"
     */
    private String specialty;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODE DE CONVERSION
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertit une entité User en UserResponse DTO
     *
     * UTILISATION:
     * User user = userRepository.findById(1).orElseThrow();
     * UserResponse response = UserResponse.from(user);
     *
     * @param user L'entité User
     * @return UserResponse DTO (sans password!)
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name()) // Enum → String
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
