package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Entité TimeSlot - Représente la table "timeslot" dans PostgreSQL
 * Définit les créneaux horaires de disponibilité des médecins
 * Relation: 1 Doctor → N TimeSlots (OneToMany)
 */
@Entity
@Table(name = "timeslot", uniqueConstraints = @UniqueConstraint(columnNames = { "doctor_id", "day_of_week",
        "start_time" }) // Un médecin ne peut avoir 2 créneaux identiques le même jour
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATION AVEC DOCTOR (N TimeSlots → 1 Doctor)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation ManyToOne avec Doctor
     * Plusieurs créneaux appartiennent à UN médecin
     * fetch = LAZY: Doctor chargé seulement si nécessaire
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // ═══════════════════════════════════════════════════════════
    // DÉFINITION CRÉNEAU HORAIRE
    // ═══════════════════════════════════════════════════════════

    /**
     * Jour de la semaine (MONDAY, TUESDAY, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    @NotNull(message = "Le jour de la semaine est obligatoire")
    private DayOfWeek dayOfWeek;

    /**
     * Heure de début du créneau (ex: 09:00)
     */
    @Column(name = "start_time", nullable = false)
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;

    /**
     * Heure de fin du créneau (ex: 17:00)
     */
    @Column(name = "end_time", nullable = false)
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;

    /**
     * Durée d'une consultation en minutes (ex: 30)
     * Permet de diviser le créneau en plusieurs RDV
     */
    @Builder.Default
    @Column(nullable = false)
    @Min(value = 15, message = "La durée minimale est 15 minutes")
    @Max(value = 120, message = "La durée maximale est 120 minutes")
    private Integer duration = 30; // Par défaut: 30 minutes

    /**
     * Créneau actif ou désactivé (soft delete)
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE
    // ═══════════════════════════════════════════════════════════

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════
    // ÉNUMÉRATION JOURS DE LA SEMAINE
    // ═══════════════════════════════════════════════════════════

    /**
     * Enum DayOfWeek - Jours de la semaine
     */
    public enum DayOfWeek {
        MONDAY, // Lundi
        TUESDAY, // Mardi
        WEDNESDAY, // Mercredi
        THURSDAY, // Jeudi
        FRIDAY, // Vendredi
        SATURDAY, // Samedi
        SUNDAY // Dimanche
    }
}