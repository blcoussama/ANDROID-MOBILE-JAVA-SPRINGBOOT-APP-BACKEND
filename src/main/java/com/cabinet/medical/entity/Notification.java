package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité Notification - Représente la table "notification" dans PostgreSQL
 * Gère les notifications/rappels pour les rendez-vous
 * Relations: N Notifications → 1 Appointment, N Notifications → 1 User
 */
@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation ManyToOne avec Appointment
     * Plusieurs notifications peuvent être liées au même RDV
     * (ex: rappel 24h + rappel 1h)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    @NotNull(message = "Le rendez-vous associé est obligatoire")
    private Appointment appointment;

    /**
     * Relation ManyToOne avec User
     * Destinataire de la notification (patient ou médecin)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Le destinataire est obligatoire")
    private User user;

    // ═══════════════════════════════════════════════════════════
    // CONTENU DE LA NOTIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Type de notification (CONFIRMATION, REMINDER_24H, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Le type de notification est obligatoire")
    private NotificationType type;

    /**
     * Message de la notification
     * Ex: "Rappel: RDV demain à 10h30 avec Dr. Martin"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Le message est obligatoire")
    private String message;

    /**
     * Date/heure programmée pour envoi de la notification
     * Ex: RDV à 10h30 le 15/01 → scheduledFor = 14/01 10:30 (24h avant)
     */
    @Column(name = "scheduled_for", nullable = false)
    @NotNull(message = "La date d'envoi programmée est obligatoire")
    private LocalDateTime scheduledFor;

    /**
     * Date/heure réelle d'envoi (null si pas encore envoyée)
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Statut de la notification (PENDING, SENT, FAILED)
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Message d'erreur si échec d'envoi (optionnel)
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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
    // MÉTHODE UTILITAIRE
    // ═══════════════════════════════════════════════════════════

    /**
     * Marque la notification comme envoyée
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Marque la notification comme échouée
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    // ═══════════════════════════════════════════════════════════
    // ÉNUMÉRATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Enum NotificationType - Types de notifications
     */
    public enum NotificationType {
        CONFIRMATION, // Confirmation RDV pris
        REMINDER_24H, // Rappel 24h avant
        REMINDER_1H, // Rappel 1h avant
        REMINDER_15MIN, // Rappel 15 min avant (optionnel)
        CANCELLATION, // Notification annulation RDV
        MODIFICATION // Notification modification RDV
    }

    /**
     * Enum NotificationStatus - Statuts possibles
     */
    public enum NotificationStatus {
        PENDING, // En attente d'envoi
        SENT, // Envoyée avec succès
        FAILED // Échec d'envoi
    }
}