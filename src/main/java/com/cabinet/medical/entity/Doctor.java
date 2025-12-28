package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Doctor - Représente la table "doctor" dans PostgreSQL
 * Contient les informations spécifiques aux médecins (étend User)
 * Relation: 1 User → 0..1 Doctor (OneToOne)
 */
@Entity
@Table(name = "doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATION AVEC USER (1 Doctor = 1 User)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation OneToOne avec User
     * Un médecin est associé à UN SEUL compte utilisateur
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PROFESSIONNELLES
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, length = 150)
    @NotBlank(message = "La spécialité est obligatoire")
    private String specialty; // Spécialité médicale (ex: Cardiologue, Pédiatre)

    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Le numéro de licence est obligatoire")
    private String licenseNumber; // Numéro d'ordre des médecins (unique)

    @Column(name = "office_address", length = 500)
    private String officeAddress; // Adresse du cabinet médical

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee; // Tarif consultation (ex: 50.00 €)

    @Column(columnDefinition = "TEXT")
    private String bio; // Biographie/présentation du médecin

    @Column(name = "years_experience")
    @Min(value = 0, message = "L'expérience ne peut pas être négative")
    private Integer yearsExperience; // Années d'expérience

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