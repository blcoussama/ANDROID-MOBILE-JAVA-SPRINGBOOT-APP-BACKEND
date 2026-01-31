package com.cabinet.medical.controller;

import com.cabinet.medical.dto.request.CancelAppointmentRequest;
import com.cabinet.medical.dto.request.CreateAppointmentRequest;
import com.cabinet.medical.dto.response.AppointmentResponse;
import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AppointmentController - Contrôleur pour la gestion des rendez-vous
 *
 * ENDPOINTS:
 * - GET /api/appointments/patient/{patientId} : RDV d'un patient (UC-P03)
 * - GET /api/appointments/doctor/{doctorId} : RDV d'un médecin (UC-D03)
 * - GET /api/appointments : TOUS les RDV (UC-A09, Admin uniquement)
 * - GET /api/appointments/{id} : Détails RDV (UC-P03, UC-D04)
 * - GET /api/appointments/available : Heures disponibles (UC-P05)
 * - POST /api/appointments : Créer RDV (UC-P06)
 * - POST /api/appointments/{id}/confirm : Confirmer RDV (UC-D05)
 * - DELETE /api/appointments/{id} : Annuler RDV (UC-P08, UC-D06, UC-A11)
 *
 * PERMISSIONS (à sécuriser avec JWT):
 * - GET /patient/{id} : PATIENT (ses RDV), ADMIN (tous patients)
 * - GET /doctor/{id} : DOCTOR (ses RDV), ADMIN (tous médecins)
 * - GET / : ADMIN uniquement
 * - GET /{id} : PATIENT (son RDV), DOCTOR (ses RDV), ADMIN (tous)
 * - GET /available : PUBLIC
 * - POST / : PATIENT, ADMIN
 * - POST /{id}/confirm : DOCTOR (ses RDV), ADMIN (tous)
 * - DELETE /{id} : PATIENT (son RDV), DOCTOR (ses RDV), ADMIN (tous)
 *
 * RÈGLES MÉTIER:
 * - RG-02: Un seul RDV par créneau médecin (UNIQUE constraint)
 * - RG-03: Patient peut annuler/modifier ses RDV
 * - RG-04: Doctor peut annuler/modifier ses RDV
 * - RG-05: Admin peut tout faire
 * - RG-06: Notification CONFIRMATION envoyée lors création RDV
 * - RG-07: Notifications REMINDER envoyées avant RDV
 *
 * FORMAT RÉPONSES:
 * - Succès: AppointmentResponse direct ou List<AppointmentResponse>
 * - Erreurs: ErrorResponse (géré par GlobalExceptionHandler)
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ═══════════════════════════════════════════════════════════
    // CONSULTATION RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir tous les RDV d'un patient (UC-P03)
     *
     * UTILISATION:
     * - Patient consulte son historique de RDV
     * - Admin consulte RDV d'un patient
     *
     * PERMISSIONS:
     * - PATIENT: Peut voir ses propres RDV (patientId = JWT userId)
     * - ADMIN: Peut voir RDV de n'importe quel patient
     *
     * EXEMPLE:
     * GET /api/appointments/patient/1
     *
     * RÉPONSE 200 OK:
     * [
     * {
     * "id": 1,
     * "patientId": 1,
     * "patientName": "Jean Dupont",
     * "doctorId": 1,
     * "doctorName": "Dr. Pierre Dupont",
     * "doctorSpecialty": "Pédiatre",
     * "dateTime": "2025-12-30T14:00:00",
     * "reason": "Consultation",
     * "status": "PENDING",
     * "cancelledBy": null,
     * "cancellationReason": null
     * }
     * ]
     *
     * ERREURS:
     * - 404: Patient non trouvé
     *
     * @param patientId ID du patient
     * @return ResponseEntity<List<AppointmentResponse>>
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(
            @PathVariable("patientId") Long patientId) {

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Obtenir tous les RDV d'un médecin (UC-D03)
     *
     * UTILISATION:
     * - Doctor consulte ses RDV programmés
     * - Admin consulte RDV d'un médecin
     *
     * PERMISSIONS:
     * - DOCTOR: Peut voir ses propres RDV (doctorId = JWT userId)
     * - ADMIN: Peut voir RDV de n'importe quel médecin
     *
     * EXEMPLE:
     * GET /api/appointments/doctor/1
     *
     * RÉPONSE 200 OK:
     * [
     * {
     * "id": 1,
     * "patientId": 1,
     * "patientName": "Jean Dupont",
     * "patientEmail": "jean@example.com",
     * "patientPhone": "0612345678",
     * "doctorId": 1,
     * "doctorName": "Dr. Pierre Dupont",
     * "doctorSpecialty": "Pédiatre",
     * "dateTime": "2025-12-30T14:00:00",
     * "reason": "Consultation pédiatrique",
     * "status": "PENDING"
     * }
     * ]
     *
     * ERREURS:
     * - 404: Médecin non trouvé
     *
     * @param doctorId ID du médecin
     * @return ResponseEntity<List<AppointmentResponse>>
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(
            @PathVariable("doctorId") Long doctorId) {

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Obtenir TOUS les RDV (UC-A09 - Admin uniquement)
     *
     * UTILISATION:
     * - Admin voit tous les RDV du cabinet
     * - Dashboard admin
     *
     * PERMISSIONS:
     * - ADMIN: Uniquement
     *
     * EXEMPLE:
     * GET /api/appointments
     *
     * RÉPONSE 200 OK:
     * [
     * {
     * "id": 1,
     * "patientId": 1,
     * "patientName": "Jean Dupont",
     * "doctorId": 1,
     * "doctorName": "Dr. Pierre Dupont",
     * "dateTime": "2025-12-30T14:00:00",
     * "status": "PENDING"
     * },
     * {
     * "id": 2,
     * "patientId": 2,
     * "patientName": "Marie Martin",
     * "doctorId": 1,
     * "doctorName": "Dr. Pierre Dupont",
     * "dateTime": "2025-12-30T15:00:00",
     * "status": "CONFIRMED"
     * }
     * ]
     *
     * @return ResponseEntity<List<AppointmentResponse>>
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {

        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION RENDEZ-VOUS (PATIENT, DOCTOR, ADMIN)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un rendez-vous (UC-P06)
     *
     * UTILISATION:
     * - Patient prend un RDV
     * - Admin crée un RDV pour un patient
     *
     * PERMISSIONS:
     * - PATIENT: Peut créer ses propres RDV (patientId = JWT userId)
     * - ADMIN: Peut créer RDV pour n'importe quel patient
     *
     * VALIDATION:
     * - doctorId obligatoire et existe
     * - dateTime obligatoire et dans le futur
     * - Créneau disponible (RG-02)
     *
     * FLOW:
     * 1. Valider doctorId existe
     * 2. Valider patientId existe
     * 3. Vérifier créneau disponible (RG-02)
     * 4. Créer Appointment (status=PENDING)
     * 5. Créer Notification CONFIRMATION (RG-06)
     * 6. Créer Notification REMINDER (RG-07)
     * 7. Retourner AppointmentResponse
     *
     * EXEMPLE:
     * POST /api/appointments?patientId=1
     * Content-Type: application/json
     *
     * {
     * "doctorId": 1,
     * "dateTime": "2025-12-30T14:00:00",
     * "reason": "Consultation pédiatrique"
     * }
     *
     * RÉPONSE 201 CREATED:
     * {
     * "id": 1,
     * "patientId": 1,
     * "patientName": "Jean Dupont",
     * "doctorId": 1,
     * "doctorName": "Dr. Pierre Dupont",
     * "doctorSpecialty": "Pédiatre",
     * "dateTime": "2025-12-30T14:00:00",
     * "reason": "Consultation pédiatrique",
     * "status": "PENDING"
     * }
     *
     * ERREURS:
     * - 400: Validation échouée (champ manquant, dateTime passé)
     * - 404: Médecin ou patient non trouvé
     * - 409: Créneau déjà réservé (RG-02)
     *
     * @param patientId ID du patient qui prend RDV (query param)
     * @param request   CreateAppointmentRequest
     * @return ResponseEntity<AppointmentResponse>
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestParam("patientId") Long patientId,
            @Valid @RequestBody CreateAppointmentRequest request) {

        AppointmentResponse appointment = appointmentService.createAppointment(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }


    /**
     * Confirmer un rendez-vous (UC-D05 - Médecin confirme un RDV)
     *
     * UTILISATION:
     * - Médecin confirme un RDV en attente
     * - Change status: PENDING → CONFIRMED
     *
     * PERMISSIONS:
     * - DOCTOR: Peut confirmer ses propres RDV uniquement
     * - ADMIN: Peut confirmer n'importe quel RDV
     *
     * FLOW:
     * 1. Vérifier que RDV existe
     * 2. Vérifier que status = PENDING
     * 3. Update status = CONFIRMED
     * 4. Créer notification pour le patient
     * 5. Retourner AppointmentResponse mis à jour
     *
     * EXEMPLE:
     * POST /api/appointments/1/confirm
     *
     * RÉPONSE 200 OK:
     * {
     * "id": 1,
     * "patientId": 1,
     * "patientName": "Jean Dupont",
     * "doctorId": 1,
     * "doctorName": "Dr. Pierre Dupont",
     * "dateTime": "2026-02-03T14:00:00",
     * "reason": "Consultation",
     * "status": "CONFIRMED",
     * "createdAt": "2026-01-31T10:00:00",
     * "updatedAt": "2026-01-31T18:00:00"
     * }
     *
     * ERREURS:
     * - 404: RDV non trouvé
     * - 400: RDV déjà confirmé ou annulé
     *
     * @param appointmentId ID du RDV
     * @return ResponseEntity<AppointmentResponse>
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable("id") Long appointmentId) {

        AppointmentResponse appointment = appointmentService.confirmAppointment(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Annuler un rendez-vous (UC-P08, UC-D06, UC-A11)
     *
     * UTILISATION:
     * - Patient annule son RDV
     * - Doctor annule un RDV
     * - Admin annule n'importe quel RDV
     *
     * PERMISSIONS:
     * - PATIENT: Peut annuler ses propres RDV uniquement
     * - DOCTOR: Peut annuler ses propres RDV uniquement
     * - ADMIN: Peut annuler n'importe quel RDV
     *
     * FLOW:
     * 1. Charger Appointment
     * 2. Déterminer cancelledBy via JWT token (PATIENT/DOCTOR/ADMIN)
     * 3. Update status = CANCELLED
     * 4. Update cancelledBy
     * 5. Update cancellationReason (si fourni)
     * 6. Créer notification annulation
     * 7. Sauvegarder
     *
     * EXEMPLE:
     * DELETE /api/appointments/1?cancelledBy=PATIENT
     * Content-Type: application/json
     *
     * {
     * "cancellationReason": "Empêchement de dernière minute"
     * }
     *
     * RÉPONSE 204 NO CONTENT:
     * (Pas de body)
     *
     * NOTE IMPORTANTE:
     * Le paramètre cancelledBy est fourni temporairement en query param
     * pour les tests. En production, il sera déterminé automatiquement
     * via le JWT token (role de l'utilisateur).
     *
     * ERREURS:
     * - 404: RDV non trouvé
     *
     * @param appointmentId ID du RDV
     * @param cancelledBy   Qui annule (PATIENT/DOCTOR/ADMIN) - temporaire pour
     *                      tests
     * @param request       CancelAppointmentRequest (optionnel, peut être vide)
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable("id") Long appointmentId,
            @RequestParam("cancelledBy") Appointment.CancelledBy cancelledBy,
            @RequestBody(required = false) CancelAppointmentRequest request) {

        // Si request est null, créer un objet vide
        if (request == null) {
            request = new CancelAppointmentRequest();
        }

        appointmentService.cancelAppointment(appointmentId, request, cancelledBy);
        return ResponseEntity.noContent().build();
    }

}