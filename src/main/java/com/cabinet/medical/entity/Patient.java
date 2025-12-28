package com.cabinet.medical.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Patient - Représente la table "patient" dans PostgreSQL
 * Contient les informations spécifiques aux patients (étend User)
 * Relation: 1 User → 0..1 Patient (OneToOne)
 */
@Entity
@Table(name = "patient")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATION AVEC USER (1 Patient = 1 User)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation OneToOne avec User
     * Un patient est associé à UN SEUL compte utilisateur
     * cascade = ALL: Si on supprime Patient, supprime User aussi
     * fetch = LAZY: User chargé seulement si nécessaire (optimisation)
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // Clé étrangère vers users.id
    private User user;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS MÉDICALES
    // ═══════════════════════════════════════════════════════════

    @Column(name = "date_of_birth") // Date de naissance (optionnelle)
    private LocalDate dateOfBirth;

    @Column(length = 500) // Adresse complète du patient
    private String address;

    @Column(columnDefinition = "TEXT") // Type TEXT pour contenu long
    private String medicalHistory; // Historique médical (allergies, maladies chroniques, etc.)

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Méthode appelée automatiquement AVANT l'insertion en DB
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}