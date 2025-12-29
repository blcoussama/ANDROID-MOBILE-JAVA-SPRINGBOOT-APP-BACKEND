package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entité TimeSlot - Représente la table "timeslot" dans PostgreSQL
 *
 * Cette classe contient les créneaux horaires récurrents hebdomadaires des
 * médecins.
 * Un créneau définit une plage de disponibilité pour un jour de la semaine
 * spécifique.
 *
 * Exemple: Dr. Martin disponible tous les Lundis de 9h00 à 12h00
 *
 * Relations:
 * - ManyToOne avec Doctor (plusieurs créneaux appartiennent à un médecin)
 *
 * Contraintes:
 * - UNIQUE(doctorId, dayOfWeek, startTime) : Pas de doublons de créneaux
 * - Pas de chevauchement pour même médecin, même jour
 */
@Entity // Indique que cette classe est une entité JPA (table DB)
@Table(name = "timeslot", // Nom de la table dans PostgreSQL
        uniqueConstraints = {
                // Contrainte UNIQUE composite: un médecin ne peut pas avoir
                // deux créneaux identiques (même jour, même heure de début)
                @UniqueConstraint(name = "uk_doctor_day_starttime", columnNames = { "doctor_id", "day_of_week",
                        "start_time" })
        })
@Data // Lombok: génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: constructeur vide (requis par JPA)
@AllArgsConstructor // Lombok: constructeur avec tous les paramètres
@Builder // Lombok: pattern Builder pour créer des instances
public class TimeSlot {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Clé primaire de la table timeslot
     * Générée automatiquement par PostgreSQL (AUTO_INCREMENT)
     */
    @Id // Marque ce champ comme clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment PostgreSQL
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATION AVEC DOCTOR (ManyToOne)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation ManyToOne avec Doctor
     * Plusieurs créneaux appartiennent à UN médecin
     * doctor_id est une clé étrangère vers la table doctor
     */
    @ManyToOne // Relation N à 1 (plusieurs TimeSlots → un Doctor)
    @JoinColumn(name = "doctor_id", // Nom de la colonne FK dans la table timeslot
            nullable = false // Obligatoire (NOT NULL) - Un créneau DOIT avoir un médecin
    )
    @NotNull(message = "Le médecin est obligatoire")
    private Doctor doctor; // Référence vers l'objet Doctor

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS DU CRÉNEAU
    // ═══════════════════════════════════════════════════════════

    /**
     * Jour de la semaine du créneau récurrent
     * Enum Java DayOfWeek: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY,
     * SUNDAY
     * Stocké en base comme STRING pour lisibilité et stabilité
     */
    @Enumerated(EnumType.STRING) // Stocke "MONDAY", "TUESDAY", etc. (pas 0, 1, 2...)
    @Column(name = "day_of_week", nullable = false, length = 20)
    @NotNull(message = "Le jour de la semaine est obligatoire")
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY, etc.

    /**
     * Heure de début du créneau
     * Format: HH:mm (exemple: 09:00, 14:30)
     * Stocké en base comme TIME
     */
    @Column(name = "start_time", nullable = false)
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;

    /**
     * Heure de fin du créneau
     * Format: HH:mm (exemple: 12:00, 18:00)
     * Stocké en base comme TIME
     */
    @Column(name = "end_time", nullable = false)
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Audit)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure de création du créneau
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

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si ce créneau chevauche un autre créneau
     * Utilisé pour validation métier (éviter conflits)
     *
     * @param other L'autre créneau à comparer
     * @return true si chevauchement, false sinon
     */
    public boolean overlapsWith(TimeSlot other) {
        // Même jour ET chevauchement horaire
        return this.dayOfWeek == other.dayOfWeek &&
                (this.startTime.isBefore(other.endTime) &&
                        this.endTime.isAfter(other.startTime));
    }

    /**
     * Calcule la durée du créneau en minutes
     *
     * @return Durée en minutes
     */
    public long getDurationInMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
}
