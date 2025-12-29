package com.cabinet.medical.controller;

import com.cabinet.medical.dto.request.CreateTimeSlotRequest;
import com.cabinet.medical.dto.request.UpdateTimeSlotRequest;
import com.cabinet.medical.dto.response.TimeSlotResponse;
import com.cabinet.medical.service.TimeSlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * TimeSlotController - Contrôleur pour la gestion des créneaux horaires
 *
 * ENDPOINTS:
 * - GET /api/timeslots/doctor/{doctorId} : Créneaux d'un médecin (UC-D02,
 * UC-A08)
 * - GET /api/timeslots/available : Créneaux disponibles pour RDV (UC-P05)
 * - POST /api/timeslots : Créer un créneau (UC-D02, UC-A08)
 * - PUT /api/timeslots/{id} : Modifier un créneau (UC-D02, UC-A08)
 * - DELETE /api/timeslots/{id} : Supprimer un créneau (UC-D02, UC-A08)
 *
 * PERMISSIONS (à sécuriser avec JWT):
 * - GET /doctor/{doctorId} : PUBLIC (patients peuvent voir)
 * - GET /available : PUBLIC (patients peuvent voir)
 * - POST : DOCTOR (ses créneaux), ADMIN (tous médecins)
 * - PUT : DOCTOR (ses créneaux), ADMIN (tous médecins)
 * - DELETE : DOCTOR (ses créneaux), ADMIN (tous médecins)
 *
 * RÈGLES MÉTIER:
 * - RG-08: Créneaux ne peuvent pas chevaucher (même doctor, même jour)
 * - UNIQUE(doctorId, dayOfWeek, startTime) en DB
 * - startTime < endTime
 *
 * FORMAT RÉPONSES:
 * - Succès: TimeSlotResponse direct ou List<TimeSlotResponse>
 * - Erreurs: ErrorResponse (géré par GlobalExceptionHandler)
 */
@RestController
@RequestMapping("/api/timeslots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    // ═══════════════════════════════════════════════════════════
    // CONSULTATION CRÉNEAUX
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir tous les créneaux d'un médecin (UC-D02, UC-A08)
     *
     * UTILISATION:
     * - Doctor voir/gérer ses créneaux
     * - Admin voir créneaux d'un médecin
     * - Patient voir créneaux d'un médecin (avant de prendre RDV)
     *
     * PERMISSIONS:
     * - PUBLIC (accessible à tous)
     *
     * EXEMPLE:
     * GET /api/timeslots/doctor/1
     *
     * RÉPONSE 200 OK:
     * [
     * {
     * "id": 1,
     * "doctorId": 1,
     * "doctorName": "Dr. Martin Durand",
     * "doctorSpecialty": "Cardiologue",
     * "dayOfWeek": "MONDAY",
     * "dayOfWeekFr": "Lundi",
     * "startTime": "09:00:00",
     * "endTime": "12:00:00",
     * "durationMinutes": 180
     * },
     * {
     * "id": 2,
     * "doctorId": 1,
     * "dayOfWeek": "MONDAY",
     * "startTime": "14:00:00",
     * "endTime": "18:00:00",
     * "durationMinutes": 240
     * }
     * ]
     *
     * ERREURS:
     * - 404: Médecin non trouvé
     *
     * @param doctorId ID du médecin
     * @return ResponseEntity<List<TimeSlotResponse>>
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<TimeSlotResponse>> getTimeSlotsByDoctor(
            @PathVariable("doctorId") Long doctorId) {

        List<TimeSlotResponse> timeSlots = timeSlotService.getTimeSlotsByDoctor(doctorId);
        return ResponseEntity.ok(timeSlots);
    }

    /**
     * Obtenir les heures disponibles pour un médecin à une date (UC-P05)
     *
     * UTILISATION:
     * - Patient sélectionne un médecin et une date
     * - System affiche les heures disponibles pour prendre RDV
     *
     * PERMISSIONS:
     * - PUBLIC (accessible à tous)
     *
     * EXEMPLE:
     * GET /api/timeslots/available?doctorId=1&date=2025-12-30
     *
     * RÉPONSE 200 OK:
     * [
     * "09:00:00",
     * "09:30:00",
     * "10:00:00",
     * "10:30:00",
     * "11:00:00",
     * "11:30:00",
     * "14:00:00",
     * "14:30:00",
     * "15:00:00",
     * "15:30:00",
     * "16:00:00",
     * "16:30:00",
     * "17:00:00",
     * "17:30:00"
     * ]
     *
     * NOTE:
     * Cette version retourne TOUTES les heures des créneaux.
     * Le filtrage des heures déjà prises sera fait par AppointmentService.
     *
     * ERREURS:
     * - 404: Médecin non trouvé
     *
     * @param doctorId ID du médecin
     * @param date     Date pour laquelle chercher les créneaux (format: yyyy-MM-dd)
     * @return ResponseEntity<List<LocalTime>>
     */
    @GetMapping("/available")
    public ResponseEntity<List<LocalTime>> getAvailableTimesForDate(
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<LocalTime> availableTimes = timeSlotService.getAvailableTimesForDate(doctorId, date);
        return ResponseEntity.ok(availableTimes);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION CRÉNEAUX (DOCTOR, ADMIN)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un créneau horaire (UC-D02, UC-A08)
     *
     * UTILISATION:
     * - Doctor crée ses créneaux horaires
     * - Admin crée créneaux pour n'importe quel médecin
     *
     * PERMISSIONS:
     * - DOCTOR: Peut créer ses propres créneaux (doctorId = JWT userId)
     * - ADMIN: Peut créer pour n'importe quel médecin
     *
     * VALIDATION:
     * - doctorId obligatoire
     * - dayOfWeek obligatoire (MONDAY, TUESDAY, etc.)
     * - startTime < endTime
     * - Pas de chevauchement (RG-08)
     *
     * EXEMPLE:
     * POST /api/timeslots
     * Content-Type: application/json
     *
     * {
     * "doctorId": 1,
     * "dayOfWeek": "MONDAY",
     * "startTime": "09:00:00",
     * "endTime": "12:00:00"
     * }
     *
     * RÉPONSE 201 CREATED:
     * {
     * "id": 1,
     * "doctorId": 1,
     * "doctorName": "Dr. Martin Durand",
     * "doctorSpecialty": "Cardiologue",
     * "dayOfWeek": "MONDAY",
     * "dayOfWeekFr": "Lundi",
     * "startTime": "09:00:00",
     * "endTime": "12:00:00",
     * "durationMinutes": 180
     * }
     *
     * ERREURS:
     * - 400: Validation échouée (champ manquant, startTime >= endTime)
     * - 404: Médecin non trouvé
     * - 409: Créneau chevauche un existant (RG-08)
     *
     * @param request CreateTimeSlotRequest
     * @return ResponseEntity<TimeSlotResponse>
     */
    @PostMapping
    public ResponseEntity<TimeSlotResponse> createTimeSlot(
            @Valid @RequestBody CreateTimeSlotRequest request) {

        TimeSlotResponse timeSlot = timeSlotService.createTimeSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeSlot);
    }

    /**
     * Modifier un créneau horaire (UC-D02, UC-A08)
     *
     * UTILISATION:
     * - Doctor modifie ses créneaux
     * - Admin modifie créneaux de n'importe quel médecin
     *
     * PERMISSIONS:
     * - DOCTOR: Peut modifier ses propres créneaux uniquement
     * - ADMIN: Peut modifier n'importe quel créneau
     *
     * VALIDATION:
     * - Tous les champs obligatoires
     * - startTime < endTime
     * - Pas de chevauchement (en excluant le créneau actuel)
     *
     * EXEMPLE:
     * PUT /api/timeslots/1
     * Content-Type: application/json
     *
     * {
     * "dayOfWeek": "TUESDAY",
     * "startTime": "14:00:00",
     * "endTime": "18:00:00"
     * }
     *
     * RÉPONSE 200 OK:
     * {
     * "id": 1,
     * "doctorId": 1,
     * "doctorName": "Dr. Martin Durand",
     * "doctorSpecialty": "Cardiologue",
     * "dayOfWeek": "TUESDAY",
     * "dayOfWeekFr": "Mardi",
     * "startTime": "14:00:00",
     * "endTime": "18:00:00",
     * "durationMinutes": 240
     * }
     *
     * ERREURS:
     * - 400: Validation échouée
     * - 404: Créneau non trouvé
     * - 409: Nouveau créneau chevauche un existant
     *
     * @param timeSlotId ID du créneau
     * @param request    UpdateTimeSlotRequest
     * @return ResponseEntity<TimeSlotResponse>
     */
    @PutMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(
            @PathVariable("id") Long timeSlotId,
            @Valid @RequestBody UpdateTimeSlotRequest request) {

        TimeSlotResponse timeSlot = timeSlotService.updateTimeSlot(timeSlotId, request);
        return ResponseEntity.ok(timeSlot);
    }

    /**
     * Supprimer un créneau horaire (UC-D02, UC-A08)
     *
     * UTILISATION:
     * - Doctor supprime ses créneaux
     * - Admin supprime n'importe quel créneau
     *
     * PERMISSIONS:
     * - DOCTOR: Peut supprimer ses propres créneaux uniquement
     * - ADMIN: Peut supprimer n'importe quel créneau
     *
     * ATTENTION:
     * Supprimer un créneau peut affecter les RDV existants.
     * Considérer une soft-delete ou vérifier RDV avant suppression.
     *
     * EXEMPLE:
     * DELETE /api/timeslots/1
     *
     * RÉPONSE 204 NO CONTENT:
     * (Pas de body)
     *
     * ERREURS:
     * - 404: Créneau non trouvé
     *
     * @param timeSlotId ID du créneau
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable("id") Long timeSlotId) {

        timeSlotService.deleteTimeSlot(timeSlotId);
        return ResponseEntity.noContent().build();
    }
}