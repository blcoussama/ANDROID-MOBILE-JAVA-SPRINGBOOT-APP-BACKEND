package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité Appointment - Représente la table "appointment" dans PostgreSQL
 *
 * Cette classe contient les rendez-vous spécifiques entre patients et médecins.
 * Un rendez-vous est une instance précise (date + heure) d'une consultation.
 *
 * Relations:
 * - ManyToOne avec Patient (plusieurs RDV appartiennent à un patient)
 * - ManyToOne avec Doctor (plusieurs RDV appartiennent à un médecin)
 * - OneToMany avec Notification (un RDV génère plusieurs notifications)
 *
 * Contraintes:
 * - UNIQUE(doctorId, dateTime) : Un médecin ne peut avoir qu'un RDV à un
 * instant donné
 * - Règle métier : dateTime doit correspondre à un TimeSlot valide
 */
@Entity // Indique que cette classe est une entité JPA (table DB)
@Table(name = "appointment", // Nom de la table dans PostgreSQL
        uniqueConstraints = {
                // Contrainte UNIQUE: Un médecin ne peut avoir qu'un seul RDV à un instant donné
                // Empêche les doubles réservations
                @UniqueConstraint(name = "uk_doctor_datetime", columnNames = { "doctor_id", "date_time" })
        })
@Data // Lombok: génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: constructeur vide (requis par JPA)
@AllArgsConstructor // Lombok: constructeur avec tous les paramètres
@Builder // Lombok: pattern Builder pour créer des instances
public class Appointment {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Clé primaire de la table appointment
     * Générée automatiquement par PostgreSQL (AUTO_INCREMENT)
     */
    @Id // Marque ce champ comme clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment PostgreSQL
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATIONS (ManyToOne)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation ManyToOne avec Patient
     * Plusieurs rendez-vous appartiennent à UN patient
     * patient_id est une clé étrangère vers la table patient
     */
    @ManyToOne // Relation N à 1 (plusieurs Appointments → un Patient)
    @JoinColumn(name = "patient_id", // Nom de la colonne FK dans la table appointment
            nullable = false // Obligatoire (NOT NULL) - Un RDV DOIT avoir un patient
    )
    @NotNull(message = "Le patient est obligatoire")
    private Patient patient; // Référence vers l'objet Patient

    /**
     * Relation ManyToOne avec Doctor
     * Plusieurs rendez-vous appartiennent à UN médecin
     * doctor_id est une clé étrangère vers la table doctor
     */
    @ManyToOne // Relation N à 1 (plusieurs Appointments → un Doctor)
    @JoinColumn(name = "doctor_id", // Nom de la colonne FK dans la table appointment
            nullable = false // Obligatoire (NOT NULL) - Un RDV DOIT avoir un médecin
    )
    @NotNull(message = "Le médecin est obligatoire")
    private Doctor doctor; // Référence vers l'objet Doctor

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS DU RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure PRÉCISE du rendez-vous
     * Format: YYYY-MM-DD HH:mm:ss
     * Exemple: 2025-12-30 14:00:00
     *
     * DIFFÉRENCE avec TimeSlot:
     * - TimeSlot: Récurrent (tous les lundis à 9h)
     * - Appointment: Spécifique (lundi 30 décembre 2025 à 14h)
     */
    @Column(name = "date_time", nullable = false)
    @NotNull(message = "La date et heure sont obligatoires")
    private LocalDateTime dateTime;

    /**
     * Motif de la consultation (optionnel)
     * Exemples: "Consultation générale", "Suivi médical", "Urgence", etc.
     * Visible par le médecin pour préparer la consultation
     */
    @Column(length = 500)
    @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
    private String reason; // Optionnel (nullable = true par défaut)

    // ═══════════════════════════════════════════════════════════
    // STATUT DU RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Statut du rendez-vous
     * - PENDING: Créé, en attente de confirmation
     * - CONFIRMED: Confirmé (après envoi notification)
     * - CANCELLED: Annulé
     */
    @Enumerated(EnumType.STRING) // Stocke "PENDING", "CONFIRMED", "CANCELLED"
    @Column(nullable = false, length = 20)
    @Builder.Default // Valeur par défaut avec Lombok Builder
    private AppointmentStatus status = AppointmentStatus.PENDING;

    /**
     * Qui a annulé le rendez-vous (seulement si status = CANCELLED)
     * - PATIENT: Annulé par le patient
     * - DOCTOR: Annulé par le médecin
     * - ADMIN: Annulé par l'administrateur
     */
    @Enumerated(EnumType.STRING) // Stocke "PATIENT", "DOCTOR", "ADMIN"
    @Column(name = "cancelled_by", length = 20)
    private CancelledBy cancelledBy; // Optionnel (null si pas annulé)

    /**
     * Raison de l'annulation (optionnel)
     * Rempli uniquement si status = CANCELLED
     * Exemples: "Patient malade", "Médecin absent", "Urgence", etc.
     */
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason; // Optionnel

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Audit)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure de création du rendez-vous
     * Automatiquement rempli lors de l'insertion en base
     */
    @Column(nullable = false, updatable = false)
    // updatable = false → Ne peut JAMAIS être modifié après création
    private LocalDateTime createdAt;

    /**
     * Date et heure de la dernière modification
     * Automatiquement mis à jour lors de chaque UPDATE
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // CALLBACKS JPA (Lifecycle)
    // ═══════════════════════════════════════════════════════════

    /**
     * Méthode appelée automatiquement AVANT l'insertion en base
     * Initialise createdAt et updatedAt avec la date/heure actuelle
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Méthode appelée automatiquement AVANT chaque mise à jour en base
     * Met à jour updatedAt avec la date/heure actuelle
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════
    // ÉNUMÉRATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Enum AppointmentStatus - Statuts possibles d'un rendez-vous
     */
    public enum AppointmentStatus {
        PENDING, // En attente de confirmation
        CONFIRMED, // Confirmé
        CANCELLED // Annulé
    }

    /**
     * Enum CancelledBy - Qui a annulé le rendez-vous
     */
    public enum CancelledBy {
        PATIENT, // Annulé par le patient
        DOCTOR, // Annulé par le médecin
        ADMIN // Annulé par l'administrateur
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si le rendez-vous est dans le passé
     *
     * @return true si dateTime < maintenant, false sinon
     */
    public boolean isPast() {
        return dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Vérifie si le rendez-vous est dans le futur
     *
     * @return true si dateTime > maintenant, false sinon
     */
    public boolean isFuture() {
        return dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Vérifie si le rendez-vous peut être annulé
     * Un RDV peut être annulé seulement s'il n'est pas déjà annulé et s'il est dans
     * le futur
     *
     * @return true si annulable, false sinon
     */
    public boolean isCancellable() {
        return status != AppointmentStatus.CANCELLED && isFuture();
    }

    /**
     * Annule le rendez-vous
     *
     * @param by     Qui annule (PATIENT, DOCTOR, ADMIN)
     * @param reason Raison de l'annulation (optionnel)
     */
    public void cancel(CancelledBy by, String reason) {
        if (!isCancellable()) {
            throw new IllegalStateException(
                    "Ce rendez-vous ne peut pas être annulé");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledBy = by;
        this.cancellationReason = reason;
    }
}
