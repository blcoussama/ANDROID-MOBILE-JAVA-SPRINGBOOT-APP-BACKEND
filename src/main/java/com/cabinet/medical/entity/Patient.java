package com.cabinet.medical.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité Patient - Représente la table "patient" dans PostgreSQL
 *
 * Cette classe contient les informations spécifiques aux patients.
 * Elle est liée à User par une relation OneToOne (1 User = 1 Patient).
 *
 * Relations:
 * - OneToOne avec User (userId FK UNIQUE)
 * - OneToMany avec Appointment (un patient peut avoir plusieurs RDV)
 */
@Entity // Indique que cette classe est une entité JPA (table DB)
@Table(name = "patient") // Nom de la table dans PostgreSQL
@Data // Lombok: génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: constructeur vide (requis par JPA)
@AllArgsConstructor // Lombok: constructeur avec tous les paramètres
@Builder // Lombok: pattern Builder pour créer des instances
public class Patient {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Clé primaire de la table patient
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
     * Un Patient est obligatoirement lié à UN User
     * userId est une clé étrangère UNIQUE vers la table users
     */
    @OneToOne // Relation 1 à 1 (un User = un Patient maximum)
    @JoinColumn(name = "user_id", // Nom de la colonne FK dans la table patient
            nullable = false, // Obligatoire (NOT NULL)
            unique = true // Un User ne peut être qu'UN seul Patient (UNIQUE)
    )
    private User user; // Référence vers l'objet User

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Audit)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure de création du profil patient
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
