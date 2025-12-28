package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité User - Représente la table "users" dans PostgreSQL
 * Contient les informations communes à tous les utilisateurs (Patient, Médecin,
 * Admin)
 */
@Entity // Indique que cette classe est une entité JPA (mappée à une table DB)
@Table(name = "users") // Nom de la table dans PostgreSQL
@Data // Lombok: génère automatiquement getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: génère un constructeur sans paramètres (requis par JPA)
@AllArgsConstructor // Lombok: génère un constructeur avec tous les paramètres
@Builder // Lombok: pattern Builder pour créer des objets facilement
public class User {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    @Id // Clé primaire de la table
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment par PostgreSQL
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, unique = true, length = 255) // Email doit être unique et obligatoire
    @Email // Validation: format email valide
    @NotBlank // Validation: ne peut pas être vide ou null
    private String email;

    @Column(nullable = false, length = 255) // Obligatoire
    @NotBlank // Ne peut pas être vide
    private String passwordHash; // Mot de passe hashé (JAMAIS en clair!)

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PERSONNELLES
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 100)
    @NotBlank
    private String firstName; // Prénom

    @Column(nullable = false, length = 100)
    @NotBlank
    private String lastName; // Nom de famille

    @Column(length = 20) // Optionnel (nullable = true par défaut)
    private String phone; // Numéro de téléphone

    // ═══════════════════════════════════════════════════════════
    // RÔLE ET STATUT
    // ═══════════════════════════════════════════════════════════

    @Enumerated(EnumType.STRING) // Stocke le nom de l'enum (ex: "PATIENT") au lieu du numéro
    @Column(nullable = false, length = 20)
    private Role role; // PATIENT, DOCTOR ou ADMIN

    @Column(nullable = false)
    @Builder.Default // Indique à Lombok d'utiliser la valeur par défaut
    private Boolean isActive = true; // Utilisateur actif ou désactivé (actif par défaut)

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Timestamps)
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, updatable = false) // Ne peut jamais être modifié après création
    private LocalDateTime createdAt; // Date de création du compte

    private LocalDateTime lastLoginAt; // Dernière connexion (peut être null au début)

    /**
     * Méthode appelée automatiquement AVANT l'insertion en DB
     * Initialise createdAt avec la date/heure actuelle
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════
    // ÉNUMÉRATION DES RÔLES
    // ═══════════════════════════════════════════════════════════

    /**
     * Enum Role - Définit les 3 types d'utilisateurs du système
     */
    public enum Role {
        PATIENT, // Utilisateur patient (prend RDV)
        DOCTOR, // Médecin (gère disponibilités et RDV)
        ADMIN // Administrateur (gestion globale)
    }

}