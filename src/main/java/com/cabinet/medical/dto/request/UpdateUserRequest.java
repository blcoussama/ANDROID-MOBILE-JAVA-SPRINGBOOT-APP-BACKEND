package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateUserRequest - DTO pour modifier un utilisateur (Admin uniquement)
 *
 * UTILISATION:
 * - UC-A06: Admin modifie un utilisateur
 *
 * ENDPOINT:
 * PUT /api/users/{id}
 *
 * DIFFÉRENCE AVEC CreateUserRequest:
 * - Password OPTIONNEL (peut être vide = ne pas modifier)
 * - Si password fourni (non vide) → il sera mis à jour
 * - Si password vide ou null → password actuel conservé
 *
 * VALIDATION:
 * - Email unique si changé (vérifié par Service)
 * - Password minimum 6 caractères SI FOURNI
 *
 * EXEMPLE JSON (modifier email et téléphone, garder password):
 * {
 * "email": "nouveau@gmail.com",
 * "password": "",
 * "firstName": "Jean",
 * "lastName": "Dupont",
 * "phone": "0612345679"
 * }
 *
 * EXEMPLE JSON (modifier password aussi):
 * {
 * "email": "jean@gmail.com",
 * "password": "nouveaupass123",
 * "firstName": "Jean",
 * "lastName": "Dupont",
 * "phone": "0612345678"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    /**
     * Email de l'utilisateur (login)
     *
     * VALIDATION:
     * - @NotBlank: Obligatoire
     * - @Email: Format email valide
     * - Unicité vérifiée par le Service (si changé)
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Mot de passe (OPTIONNEL)
     * 
     * VALIDATION:
     * - Aucune validation ici (fait dans le Service)
     * 
     * LOGIQUE:
     * - Si vide ("") ou null → Password actuel conservé
     * - Si fourni → Password mis à jour (hashé en BCrypt)
     * - Validation longueur (min 6) dans UserService
     */
    private String password;

    /**
     * Prénom de l'utilisateur
     *
     * VALIDATION:
     * - @NotBlank: Obligatoire
     */
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    /**
     * Nom de famille de l'utilisateur
     *
     * VALIDATION:
     * - @NotBlank: Obligatoire
     */
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    /**
     * Numéro de téléphone (optionnel)
     *
     * VALIDATION:
     * - @Pattern: Si fourni, doit être 10 chiffres
     */
    @Pattern(regexp = "^[0-9]{10}$", message = "Le téléphone doit contenir 10 chiffres")
    private String phone;

    /**
     * Spécialité (pour les médecins uniquement)
     *
     * VALIDATION:
     * - Optionnel pour patients (null ou vide)
     * - Obligatoire pour médecins (vérifié dans Service)
     */
    private String specialty;
}