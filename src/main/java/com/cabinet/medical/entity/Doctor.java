package com.cabinet.medical.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité Doctor - Représente la table "doctor" dans PostgreSQL
 *
 * Cette classe contient les informations spécifiques aux médecins.
 * Elle est liée à User par une relation OneToOne (1 User = 1 Doctor).
 *
 * Relations:
 * - OneToOne avec User (userId FK UNIQUE)
 * - OneToMany avec TimeSlot (un médecin a plusieurs créneaux horaires)
 * - OneToMany avec Appointment (un médecin a plusieurs rendez-vous)
 */
@Entity // Indique que cette classe est une entité JPA (table DB)
@Table(name = "doctor") // Nom de la table dans PostgreSQL
@Data // Lombok: génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: constructeur vide (requis par JPA)
@AllArgsConstructor // Lombok: constructeur avec tous les paramètres
@Builder // Lombok: pattern Builder pour créer des instances
public class Doctor {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Clé primaire de la table doctor
     * Générée automatiquement par PostgreSQL (AUTO_INCREMENT)
     */
    @Id // Marque ce champ comme clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment PostgreSQL
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATION AVEC USER (OneToOne)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation OneToOne avec User
     * Un Doctor est obligatoirement lié à UN User
     * userId est une clé étrangère UNIQUE vers la table users
     */
    @OneToOne // Relation 1 à 1 (un User = un Doctor maximum)
    @JoinColumn(name = "user_id", // Nom de la colonne FK dans la table doctor
            nullable = false, // Obligatoire (NOT NULL)
            unique = true // Un User ne peut être qu'UN seul Doctor (UNIQUE)
    )
    private User user; // Référence vers l'objet User

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PROFESSIONNELLES
    // ═══════════════════════════════════════════════════════════

    /**
     * Spécialité du médecin (optionnel)
     * Exemples: "Cardiologue", "Pédiatre", "Généraliste", etc.
     * Utilisé pour l'affichage dans la liste des médecins (app Android)
     */
    @Column(length = 150)
    private String specialty; // Optionnel (nullable = true par défaut)

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Audit)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure de création du profil médecin
     * Automatiquement rempli lors de l'insertion en base
     */
    @Column(nullable = false, updatable = false)
    // updatable = false → Ne peut JAMAIS être modifié après création
    private LocalDateTime createdAt;

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
}
