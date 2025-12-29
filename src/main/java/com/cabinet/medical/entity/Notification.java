package com.cabinet.medical.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité Notification - Représente la table "notification" dans PostgreSQL
 *
 * Cette classe contient les notifications et rappels envoyés aux utilisateurs.
 * Deux types de notifications:
 * - CONFIRMATION: Envoyée immédiatement lors de la création d'un RDV
 * - REMINDER: Envoyée automatiquement avant le RDV (ex: 24h avant)
 *
 * Relations:
 * - ManyToOne avec Appointment (plusieurs notifications pour un RDV)
 * - ManyToOne avec User (plusieurs notifications pour un utilisateur)
 *
 * Système de rappels:
 * - Job automatique (Cron) cherche notifications avec sentAt = NULL
 * - Si dateTime du RDV proche → Envoie notification (e-mail/SMS)
 * - Update sentAt = now pour marquer comme envoyée
 */
@Entity // Indique que cette classe est une entité JPA (table DB)
@Table(name = "notification") // Nom de la table dans PostgreSQL
@Data // Lombok: génère getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: constructeur vide (requis par JPA)
@AllArgsConstructor // Lombok: constructeur avec tous les paramètres
@Builder // Lombok: pattern Builder pour créer des instances
public class Notification {

    // ═══════════════════════════════════════════════════════════
    // IDENTIFIANT UNIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Clé primaire de la table notification
     * Générée automatiquement par PostgreSQL (AUTO_INCREMENT)
     */
    @Id // Marque ce champ comme clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment PostgreSQL
    private Long id;

    // ═══════════════════════════════════════════════════════════
    // RELATIONS (ManyToOne)
    // ═══════════════════════════════════════════════════════════

    /**
     * Relation ManyToOne avec Appointment
     * Plusieurs notifications appartiennent à UN rendez-vous
     * appointment_id est une clé étrangère vers la table appointment
     *
     * Exemple: Un RDV génère 2-3 notifications
     * - 1 CONFIRMATION (immédiate)
     * - 1-2 REMINDER (avant RDV)
     */
    @ManyToOne // Relation N à 1 (plusieurs Notifications → un Appointment)
    @JoinColumn(name = "appointment_id", // Nom de la colonne FK dans la table notification
            nullable = false // Obligatoire (NOT NULL) - Une notification DOIT être liée à un RDV
    )
    @NotNull(message = "Le rendez-vous est obligatoire")
    private Appointment appointment; // Référence vers l'objet Appointment

    /**
     * Relation ManyToOne avec User
     * Plusieurs notifications appartiennent à UN utilisateur
     * user_id est une clé étrangère vers la table users
     *
     * Destinataire de la notification (Patient ou Doctor)
     */
    @ManyToOne // Relation N à 1 (plusieurs Notifications → un User)
    @JoinColumn(name = "user_id", // Nom de la colonne FK dans la table notification
            nullable = false // Obligatoire (NOT NULL) - Une notification DOIT avoir un destinataire
    )
    @NotNull(message = "L'utilisateur est obligatoire")
    private User user; // Référence vers l'objet User (destinataire)

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS DE LA NOTIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Type de notification
     * - CONFIRMATION: Envoyée immédiatement lors de la création du RDV
     * Exemple: "Votre rendez-vous avec Dr. Martin le 30/12 à 14h est confirmé"
     *
     * - REMINDER: Rappel automatique avant le RDV
     * Exemple: "Rappel: Rendez-vous demain avec Dr. Martin à 14h"
     */
    @Enumerated(EnumType.STRING) // Stocke "CONFIRMATION", "REMINDER"
    @Column(nullable = false, length = 20)
    @NotNull(message = "Le type de notification est obligatoire")
    private NotificationType type;

    /**
     * Message de la notification
     * Contenu texte envoyé par e-mail/SMS
     *
     * Exemples:
     * - CONFIRMATION: "Votre RDV avec Dr. Martin le 30/12/2025 à 14h00 est
     * confirmé."
     * - REMINDER: "Rappel: Rendez-vous demain à 14h00 avec Dr. Martin
     * (Cardiologue)."
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Le message est obligatoire")
    private String message;

    /**
     * Date et heure d'envoi de la notification
     *
     * - NULL: Notification pas encore envoyée (en attente)
     * - NOT NULL: Notification envoyée (timestamp de l'envoi)
     *
     * Utilisé par le job automatique pour savoir quelles notifications envoyer:
     * SELECT * FROM notification WHERE sent_at IS NULL AND type = 'REMINDER'
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt; // Optionnel (NULL = pas encore envoyée)

    // ═══════════════════════════════════════════════════════════
    // HORODATAGE (Audit)
    // ═══════════════════════════════════════════════════════════

    /**
     * Date et heure de création de la notification
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
    // ÉNUMÉRATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Enum NotificationType - Types de notifications
     *
     * CONFIRMATION:
     * - Envoyée immédiatement lors création RDV
     * - sentAt = NOW (envoi immédiat)
     * - Informe patient/médecin du nouveau RDV
     *
     * REMINDER:
     * - Envoyée automatiquement avant RDV
     * - sentAt = NULL au début (job l'enverra plus tard)
     * - Rappelle RDV à venir (ex: 24h avant, 1h avant)
     */
    public enum NotificationType {
        CONFIRMATION, // Confirmation immédiate du RDV
        REMINDER // Rappel avant le RDV
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si la notification a été envoyée
     *
     * @return true si envoyée (sentAt != null), false sinon
     */
    public boolean isSent() {
        return sentAt != null;
    }

    /**
     * Vérifie si la notification est en attente d'envoi
     *
     * @return true si en attente (sentAt == null), false sinon
     */
    public boolean isPending() {
        return sentAt == null;
    }

    /**
     * Marque la notification comme envoyée
     * Met à jour sentAt avec l'heure actuelle
     */
    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Vérifie si la notification doit être envoyée maintenant
     * Utilisé par le job automatique pour les REMINDER
     *
     * Logique:
     * - Type = REMINDER
     * - Pas encore envoyée (sentAt == null)
     * - RDV dans moins de 24h
     *
     * @return true si doit être envoyée, false sinon
     */
    public boolean shouldBeSentNow() {
        if (type != NotificationType.REMINDER || isSent()) {
            return false;
        }

        // Vérifier si RDV dans les prochaines 24h
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appointmentTime = appointment.getDateTime();
        LocalDateTime reminderThreshold = appointmentTime.minusHours(24);

        return now.isAfter(reminderThreshold) && now.isBefore(appointmentTime);
    }
}
