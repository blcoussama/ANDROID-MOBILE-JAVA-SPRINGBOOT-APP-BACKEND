package com.cabinet.medical.controller;

import com.cabinet.medical.dto.response.DashboardResponse;
import com.cabinet.medical.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DashboardController - Contrôleur pour les statistiques du tableau de bord
 * admin
 *
 * ENDPOINT:
 * - GET /api/admin/dashboard : Statistiques globales cabinet
 *
 * USE CASE:
 * - UC-A02: Voir tableau de bord état rendez-vous
 *
 * PERMISSIONS:
 * - ADMIN uniquement (à implémenter avec Spring Security)
 * - En production: @PreAuthorize("hasRole('ADMIN')")
 *
 * ÉCRAN ANDROID:
 * - SCR-A01: Dashboard Admin
 *
 * ARCHITECTURE:
 * DashboardController → DashboardService → Repositories
 * ↓
 * DashboardResponse
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param dashboardService Service dashboard
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ═══════════════════════════════════════════════════════════
    // ENDPOINT STATISTIQUES
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir statistiques globales du cabinet (UC-A02)
     *
     * UTILISATION:
     * - Admin consulte dashboard à la connexion
     * - Rafraîchissement régulier des stats
     * - Vue d'ensemble activité cabinet
     *
     * PERMISSIONS:
     * - ADMIN uniquement
     * - ✅ Protégé par @PreAuthorize("hasRole('ADMIN')")
     *
     * SÉCURITÉ:
     * - Seul ADMIN peut accéder
     * - PATIENT/DOCTOR → 403 FORBIDDEN
     *
     * EXEMPLE:
     * GET /api/admin/dashboard
     *
     * RÉPONSE 200 OK:
     * {
     * "totalAppointmentsToday": 5,
     * "totalAppointmentsWeek": 18,
     * "appointmentsByStatus": {
     * "PENDING": 3,
     * "CONFIRMED": 10,
     * "CANCELLED": 5
     * },
     * "totalPatients": 25,
     * "totalDoctors": 4,
     * "totalUsers": 148,
     * "recentAppointments": [
     * {
     * "id": 3,
     * "patientId": 1,
     * "patientName": "Jean Dupont",
     * "patientEmail": "jean.dupont@gmail.com",
     * "patientPhone": "0612345678",
     * "doctorId": 1,
     * "doctorName": "Dr. Pierre Dupont",
     * "doctorSpecialty": "Pédiatre",
     * "dateTime": "2025-12-31T15:00:00",
     * "reason": "Consultation avant déplacement",
     * "status": "PENDING",
     * "cancelledBy": null,
     * "cancellationReason": null,
     * "createdAt": "2025-12-29T20:51:48.240536",
     * "updatedAt": "2025-12-29T20:51:52.42507"
     * },
     * ...
     * ],
     * "totalTimeSlots": null,
     * "pendingNotifications": null,
     * "confirmationRate": 74.71,
     * "cancellationRate": 8.05
     * }
     *
     * STATISTIQUES FOURNIES:
     * 1. totalAppointmentsToday (Long)
     * └── RDV aujourd'hui (00:00 - 23:59)
     *
     * 2. totalAppointmentsWeek (Long)
     * └── RDV semaine courante (Lundi - Dimanche)
     *
     * 3. appointmentsByStatus (Map<String, Long>)
     * └── Répartition PENDING, CONFIRMED, CANCELLED
     *
     * 4. totalPatients (Long)
     * └── Nombre total patients inscrits
     *
     * 5. totalDoctors (Long)
     * └── Nombre total médecins du cabinet
     *
     * 6. totalUsers (Long)
     * └── Nombre total utilisateurs (tous rôles)
     *
     * 7. recentAppointments (List<AppointmentResponse>)
     * └── 10 derniers RDV créés (triés par createdAt DESC)
     *
     * 8. confirmationRate (Double) - OPTIONNEL
     * └── Taux de confirmation en %
     *
     * 9. cancellationRate (Double) - OPTIONNEL
     * └── Taux d'annulation en %
     *
     * PERFORMANCE:
     * - Temps réponse: ~50-100ms
     * - Plusieurs requêtes DB optimisées
     *
     * CACHE:
     * - Possibilité d'ajouter @Cacheable("dashboard")
     * - Durée cache: 1-5 minutes recommandé
     * - Invalidation sur nouveaux RDV
     *
     * UTILISATION APP ANDROID:
     * - Appel lors ouverture SCR-A01 (Dashboard Admin)
     * - Rafraîchissement pull-to-refresh
     * - Affichage cards et charts
     *
     * @return ResponseEntity<DashboardResponse>
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse dashboard = dashboardService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * NOTE: SÉCURITÉ À AJOUTER EN PRODUCTION
     *
     * Version avec Spring Security:
     *
     * @PreAuthorize("hasRole('ADMIN')")
     * 
     * @GetMapping
     *             public ResponseEntity<DashboardResponse> getDashboard() {
     *             // ...
     *             }
     *
     *             ALTERNATIVE: Annotation au niveau classe
     *
     * @RestController
     *                 @RequestMapping("/api/admin/dashboard")
     *                 @PreAuthorize("hasRole('ADMIN')")
     *                 public class DashboardController {
     *                 // Tous les endpoints protégés ADMIN
     *                 }
     */
}