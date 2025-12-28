package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité Appointment - Représente la table "appointment" dans PostgreSQL
 * Représente un rendez-vous spécifique entre un patient et un médecin
 * Relations: N Appointments → 1 Patient, N Appointments → 1 Doctor
 */
@Entity
@Table(name = "appointment", uniqueConstraints = @UniqueConstraint(columnNames = { "doctor_id", "date_time" }))
// Un médecin ne peut avoir 2 RDV au même moment
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATIONS (Patient et Doctor)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation ManyToOne avec Patient
     * Plusieurs RDV peuvent appartenir au même patient
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Le patient est obligatoire")
    private Patient patient;

    /**
     * Relation ManyToOne avec Doctor
     * Plusieurs RDV peuvent appartenir au même médecin
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @NotNull(message = "Le médecin est obligatoire")
    private Doctor doctor;

    // ═══════════════════════════════════════════════════════════
    // DÉTAILS DU RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure SPÉCIFIQUE du RDV (ex: 2025-01-15T10:30:00)
     * Pas récurrent comme TimeSlot!
     */
    @Column(name = "date_time", nullable = false)
    @NotNull(message = "La date et l'heure sont obligatoires")
    @Future(message = "La date du rendez-vous doit être dans le futur")
    private LocalDateTime dateTime;

    /**
     * Durée du RDV en minutes (généralement issue de TimeSlot.duration)
     */
    @Builder.Default
    @Column(nullable = false)
    @Min(value = 15, message = "La durée minimale est 15 minutes")
    @Max(value = 120, message = "La durée maximale est 120 minutes")
    private Integer duration = 30;

    /**
     * Motif de consultation (optionnel mais recommandé)
     */
    @Column(length = 500)
    @Size(max = 500, message = "Le motif ne peut dépasser 500 caractères")
    private String reason;

    // ═══════════════════════════════════════════════════════════
    // STATUT DU RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Statut actuel du RDV (PENDING, CONFIRMED, CANCELLED, etc.)
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // ═══════════════════════════════════════════════════════════
    // GESTION ANNULATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Qui a annulé le RDV? (Patient, Doctor ou Admin)
     * Null si pas annulé
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by", length = 20)
    private CancelledBy cancelledBy;

    /**
     * Raison de l'annulation (optionnel)
     */
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Audit Trail)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date de création du RDV
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Dernière modification du RDV
     * Utile pour tracer modifications/annulations
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Méthode appelée AVANT insertion en DB
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Méthode appelée AVANT mise à jour en DB
     * Met à jour automatiquement updatedAt
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════
    // ÉNUMÉRATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Enum AppointmentStatus - Statuts possibles d'un RDV
     */
    public enum AppointmentStatus {
        PENDING, // En attente de confirmation
        CONFIRMED, // Confirmé par le patient ou médecin
        CANCELLED, // Annulé
        COMPLETED, // Terminé (consultation effectuée)
        NO_SHOW // Patient absent (no-show)
    }

    /**
     * Enum CancelledBy - Qui a annulé le RDV?
     */
    public enum CancelledBy {
        PATIENT, // Annulé par le patient
        DOCTOR, // Annulé par le médecin
        ADMIN // Annulé par l'administrateur
    }
}