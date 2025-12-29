package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.entity.Notification;
import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationRepository - Interface pour gérer l'accès aux notifications
 *
 * RESPONSABILITÉS:
 * - CRUD sur la table notification
 * - Recherche notifications par utilisateur (UC-P09)
 * - Recherche notifications par rendez-vous
 * - Recherche notifications REMINDER en attente (job automatique)
 * - Utilisé par NotificationService, NotificationScheduler
 *
 * RELATION:
 * - Notification (*) → Appointment (1) : ManyToOne
 * - Notification (*) → User (1) : ManyToOne
 *
 * TYPES:
 * - CONFIRMATION: Envoyée immédiatement lors création RDV
 * - REMINDER: Envoyée automatiquement avant RDV (sentAt = NULL au début)
 *
 * MÉTHODES GRATUITES (JpaRepository):
 * - save(notification) : Créer/Modifier notification
 * - findById(id) : Trouver par ID
 * - findAll() : Liste toutes les notifications
 * - deleteById(id) : Supprimer par ID
 * - count() : Compter notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR UTILISATEUR
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve toutes les notifications d'un utilisateur
     * Utilisé pour historique notifications (UC-P09)
     *
     * SQL généré: SELECT * FROM notification WHERE user_id = ?
     * ORDER BY created_at DESC
     *
     * @param user L'utilisateur
     * @return Liste de ses notifications (plus récentes en premier)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Trouve les notifications non envoyées d'un utilisateur
     * Utilisé pour afficher notifications en attente
     *
     * SQL généré: SELECT * FROM notification
     * WHERE user_id = ? AND sent_at IS NULL
     * ORDER BY created_at DESC
     *
     * @param user L'utilisateur
     * @return Liste des notifications en attente
     */
    List<Notification> findByUserAndSentAtIsNullOrderByCreatedAtDesc(User user);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve toutes les notifications d'un rendez-vous
     * Utilisé pour voir historique notifications d'un RDV
     *
     * SQL généré: SELECT * FROM notification WHERE appointment_id = ?
     *
     * @param appointment Le rendez-vous
     * @return Liste des notifications liées à ce RDV
     */
    List<Notification> findByAppointment(Appointment appointment);

    /**
     * Trouve les notifications d'un RDV par type
     * Utilisé pour vérifier si notification déjà envoyée
     *
     * SQL généré: SELECT * FROM notification
     * WHERE appointment_id = ? AND type = ?
     *
     * @param appointment Le rendez-vous
     * @param type        Le type (CONFIRMATION, REMINDER)
     * @return Liste des notifications de ce type
     */
    List<Notification> findByAppointmentAndType(
            Appointment appointment,
            Notification.NotificationType type);

    // ═══════════════════════════════════════════════════════════
    // JOB AUTOMATIQUE (REMINDER)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve les notifications REMINDER en attente d'envoi
     * Utilisé par le job automatique (Cron) pour envoyer les rappels
     *
     * SQL généré: SELECT * FROM notification
     * WHERE type = 'REMINDER' AND sent_at IS NULL
     *
     * @param type Le type (REMINDER)
     * @return Liste des notifications REMINDER non envoyées
     */
    List<Notification> findByTypeAndSentAtIsNull(Notification.NotificationType type);

    /**
     * Trouve les REMINDER à envoyer maintenant (RDV dans les 24h)
     * Utilisé par le job pour filtrer les notifications à envoyer
     *
     * LOGIQUE:
     * - Type = REMINDER
     * - Pas encore envoyée (sentAt = NULL)
     * - RDV entre maintenant et dans 24h
     *
     * SQL généré avec @Query personnalisée (voir ci-dessous)
     *
     * @param type              Le type (REMINDER)
     * @param now               La date/heure actuelle
     * @param reminderThreshold 24h dans le futur
     * @return Liste des REMINDER à envoyer maintenant
     */
    @Query("SELECT n FROM Notification n " +
            "WHERE n.type = :type " +
            "AND n.sentAt IS NULL " +
            "AND n.appointment.dateTime BETWEEN :now AND :reminderThreshold")
    List<Notification> findRemindersToSend(
            @Param("type") Notification.NotificationType type,
            @Param("now") LocalDateTime now,
            @Param("reminderThreshold") LocalDateTime reminderThreshold);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Compte les notifications envoyées
     * Utilisé pour statistiques
     *
     * SQL généré: SELECT COUNT(*) FROM notification WHERE sent_at IS NOT NULL
     *
     * @return Nombre de notifications envoyées
     */
    long countBySentAtIsNotNull();

    /**
     * Compte les notifications en attente
     * Utilisé pour statistiques
     *
     * SQL généré: SELECT COUNT(*) FROM notification WHERE sent_at IS NULL
     *
     * @return Nombre de notifications en attente
     */
    long countBySentAtIsNull();

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PAR ID (POUR NOTIFICATIONSERVICE)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve toutes les notifications d'un utilisateur (par ID)
     * Version par ID pour NotificationService
     *
     * @param userId ID de l'utilisateur
     * @return Liste notifications (plus récente en premier)
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Trouve toutes les notifications d'un RDV (par ID)
     * Version par ID pour NotificationService
     *
     * @param appointmentId ID du rendez-vous
     * @return Liste notifications du RDV
     */
    List<Notification> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);

    /**
     * Compte les notifications d'un utilisateur (par ID)
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications
     */
    long countByUserId(Long userId);

    /**
     * Compte les notifications non envoyées d'un utilisateur (par ID)
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications en attente
     */
    long countByUserIdAndSentAtIsNull(Long userId);
}
