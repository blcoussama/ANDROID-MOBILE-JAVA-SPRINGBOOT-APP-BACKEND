package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité User - Représente la table "users" dans PostgreSQL
 *
 * Cette classe contient les informations communes à tous les utilisateurs
 * du système (Patient, Médecin, Administrateur).
 *
 * Relations:
 * - OneToOne avec Patient (un User peut être un Patient)
 * - OneToOne avec Doctor (un User peut être un Doctor)
 */
@Entity // Indique à JPA que cette classe est une entité (table DB)
@Table(name = "users") // Nom de la table dans PostgreSQL (évite mot réservé "user")
@Data // Lombok: génère getters, setters, toString, equals, hashCode automatiquement
@NoArgsConstructor // Lombok: constructeur vide (requis par JPA)
@AllArgsConstructor // Lombok: constructeur avec tous les paramètres
@Builder // Lombok: pattern Builder pour créer des instances facilement
public class User {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Clé primaire de la table
     * Générée automatiquement par PostgreSQL (AUTO_INCREMENT)
     */
    @Id // Marque ce champ comme clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment géré par PostgreSQL
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Email de l'utilisateur - Utilisé pour la connexion
     * Doit être unique dans toute la base de données
     */
    @Column(nullable = false, unique = true, length = 255)
    // nullable = false → NOT NULL en SQL
    // unique = true → Contrainte UNIQUE en SQL
    // length = 255 → VARCHAR(255) en SQL
    @Email(message = "Email doit être valide") // Validation: format email
    @NotBlank(message = "Email est obligatoire") // Validation: ne peut pas être vide
    private String email;

    /**
     * Mot de passe hashé avec BCrypt
     * JAMAIS stocké en clair pour des raisons de sécurité
     */
    @Column(nullable = false, length = 255)
    @NotBlank(message = "Password est obligatoire")
    private String passwordHash;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PERSONNELLES
    // ═══════════════════════════════════════════════════════════

    /**
     * Prénom de l'utilisateur
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Prénom est obligatoire")
    private String firstName;

    /**
     * Nom de famille de l'utilisateur
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nom est obligatoire")
    private String lastName;

    /**
     * Numéro de téléphone (optionnel)
     */
    @Column(length = 20)
    private String phone;

    // ═══════════════════════════════════════════════════════════
    // RÔLE ET PERMISSIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Rôle de l'utilisateur dans le système
     * Détermine les permissions et fonctionnalités accessibles
     */
    @Enumerated(EnumType.STRING) // Stocke "PATIENT", "DOCTOR", "ADMIN" au lieu de 0, 1, 2
    @Column(nullable = false, length = 20)
    private Role role;

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Audit)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure de création du compte
     * Automatiquement rempli lors de l'insertion en base
     */
    @Column(nullable = false, updatable = false)
    // updatable = false → Ne peut JAMAIS être modifié après création
    private LocalDateTime createdAt;

    /**
     * Date et heure de la dernière connexion
     * Mis à jour à chaque login réussi
     */
    private LocalDateTime lastLoginAt;

    // ═══════════════════════════════════════════════════════════
    // CALLBACK JPA (Lifecycle)
    // ═══════════════════════════════════════════════════════════

    /**
     * Méthode appelée automatiquement AVANT l'insertion en base
     * Initialise createdAt avec la date/heure actuelle
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════
    // ÉNUMÉRATION ROLE
    // ═══════════════════════════════════════════════════════════

    /**
     * Enum Role - Définit les 3 types d'utilisateurs du système
     *
     * PATIENT : Utilisateur patient (prend rendez-vous)
     * DOCTOR : Médecin (gère ses disponibilités et rendez-vous)
     * ADMIN : Administrateur/Secrétaire (gestion globale du cabinet)
     */
    public enum Role {
        PATIENT, // Utilisateur patient
        DOCTOR, // Médecin
        ADMIN // Administrateur
    }
}
