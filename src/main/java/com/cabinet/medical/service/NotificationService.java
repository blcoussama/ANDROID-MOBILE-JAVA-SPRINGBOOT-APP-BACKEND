package com.cabinet.medical.service;

import com.cabinet.medical.dto.response.NotificationResponse;
import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.entity.Notification;
import com.cabinet.medical.entity.User;
import com.cabinet.medical.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NotificationService - Service de gestion des notifications
 *
 * RESPONSABILITÉS:
 * - Créer notifications (CONFIRMATION, REMINDER)
 * - Envoyer notifications en attente (job Cron)
 * - Lister notifications par utilisateur
 * - Marquer notifications comme envoyées
 *
 * USE CASES:
 * - UC-P09: Patient recevoir notifications/rappels
 * - Système automatique de rappels (RG-07)
 *
 * RÈGLES MÉTIER:
 * - RG-06: Notification CONFIRMATION envoyée lors création RDV
 * - RG-07: Notifications REMINDER envoyées avant RDV
 *
 * SYSTÈME DE RAPPELS:
 * Job Cron exécuté toutes les heures:
 * 1. Cherche notifications REMINDER avec sentAt = NULL
 * 2. Vérifie si RDV dans les prochaines 24h
 * 3. Envoie notification (e-mail/SMS)
 * 4. Update sentAt = NOW
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param notificationRepository Repository Notification
     */
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Créer une notification de confirmation (RG-06)
     *
     * TYPE: CONFIRMATION
     * ENVOI: Immédiat (sentAt = NOW)
     *
     * UTILISATION:
     * Appelée automatiquement par AppointmentService lors:
     * - Création RDV
     * - Modification RDV
     * - Annulation RDV
     *
     * @param appointment Rendez-vous concerné
     * @param user        Destinataire de la notification
     * @param message     Contenu du message
     * @return Notification créée
     */
    @Transactional
    public Notification createConfirmationNotification(Appointment appointment,
            User user,
            String message) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setUser(user);
        notification.setType(Notification.NotificationType.CONFIRMATION);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now()); // Envoi immédiat

        return notificationRepository.save(notification);
    }

    /**
     * Créer une notification de rappel (RG-07)
     *
     * TYPE: REMINDER
     * ENVOI: Différé (sentAt = NULL, envoyé par job Cron)
     *
     * UTILISATION:
     * Appelée automatiquement par AppointmentService lors création RDV
     *
     * @param appointment Rendez-vous concerné
     * @param user        Destinataire de la notification
     * @param message     Contenu du message
     * @return Notification créée
     */
    @Transactional
    public Notification createReminderNotification(Appointment appointment,
            User user,
            String message) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setUser(user);
        notification.setType(Notification.NotificationType.REMINDER);
        notification.setMessage(message);
        notification.setSentAt(null); // Sera envoyé plus tard par job

        return notificationRepository.save(notification);
    }

    /**
     * Obtenir toutes les notifications d'un utilisateur (UC-P09)
     *
     * @param userId ID de l'utilisateur
     * @return Liste des notifications
     */
    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les notifications d'un rendez-vous
     *
     * UTILISATION:
     * Voir historique notifications pour un RDV spécifique
     *
     * @param appointmentId ID du rendez-vous
     * @return Liste des notifications
     */
    public List<NotificationResponse> getNotificationsByAppointment(Long appointmentId) {
        return notificationRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les notifications non envoyées
     *
     * UTILISATION:
     * Job Cron pour envoyer les rappels en attente
     *
     * @return Liste des notifications en attente (sentAt = NULL)
     */
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByTypeAndSentAtIsNull(
                Notification.NotificationType.REMINDER);
    }

    /**
     * Marquer une notification comme envoyée
     *
     * @param notificationId ID de la notification
     */
    @Transactional
    public void markAsSent(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    notification.setSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }

    /**
     * Job automatique pour envoyer les rappels (RG-07)
     *
     * PLANIFICATION:
     * Exécuté toutes les heures (cron: 0 0 * * * *)
     *
     * FLOW:
     * 1. Cherche notifications REMINDER non envoyées (sentAt = NULL)
     * 2. Pour chaque notification:
     * - Vérifie si RDV dans les prochaines 24h
     * - Si oui: Envoie notification (e-mail/SMS)
     * - Update sentAt = NOW
     *
     * NOTE:
     * L'envoi réel (e-mail/SMS) est simulé ici.
     * En production, utiliser un service comme:
     * - SendGrid (e-mail)
     * - Twilio (SMS)
     * - Firebase Cloud Messaging
     */
    @Scheduled(cron = "0 0 * * * *") // Toutes les heures à la minute 0
    @Transactional
    public void sendPendingReminders() {
        List<Notification> pendingNotifications = getPendingNotifications();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        for (Notification notification : pendingNotifications) {
            Appointment appointment = notification.getAppointment();
            LocalDateTime appointmentTime = appointment.getDateTime();

            // Vérifier si RDV dans les prochaines 24h
            if (appointmentTime.isAfter(now) && appointmentTime.isBefore(in24Hours)) {
                // TODO: Envoyer réellement la notification (e-mail/SMS)
                // Exemple: emailService.send(notification.getUser().getEmail(),
                // notification.getMessage())
                // Exemple: smsService.send(notification.getUser().getPhone(),
                // notification.getMessage())

                System.out.println("📧 Envoi REMINDER: " + notification.getMessage());

                // Marquer comme envoyée
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        }
    }

    /**
     * Compter les notifications d'un utilisateur
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications
     */
    public long countNotificationsByUser(Long userId) {
        return notificationRepository.countByUserId(userId);
    }

    /**
     * Compter les notifications non lues (non envoyées)
     *
     * UTILISATION:
     * Badge de notifications dans l'app mobile
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications en attente
     */
    public long countPendingNotificationsByUser(Long userId) {
        return notificationRepository.countByUserIdAndSentAtIsNull(userId);
    }
}
