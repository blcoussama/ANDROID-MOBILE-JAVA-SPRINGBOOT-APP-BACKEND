package com.cabinet.medical.controller;

import com.cabinet.medical.dto.response.AppointmentResponse;
import com.cabinet.medical.dto.response.DoctorResponse;
import com.cabinet.medical.dto.response.TimeSlotResponse;
import com.cabinet.medical.service.AppointmentService;
import com.cabinet.medical.service.DoctorService;
import com.cabinet.medical.service.TimeSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final TimeSlotService timeSlotService;

    public DoctorController(DoctorService doctorService,
            AppointmentService appointmentService,
            TimeSlotService timeSlotService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.timeSlotService = timeSlotService;
    }

    // ═══════════════════════════════════════════════════════════
    // CONSULTATION MÉDECINS
    // ═══════════════════════════════════════════════════════════

    /**
     * Lister tous les médecins (UC-P04)
     */
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        List<DoctorResponse> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    /**
     * Obtenir détails d'un médecin
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(
            @PathVariable("id") Long doctorId) {

        DoctorResponse doctor = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(doctor);
    }

    /**
     * Rechercher médecins par spécialité
     */
    @GetMapping("/search")
    public ResponseEntity<List<DoctorResponse>> searchDoctors(
            @RequestParam(value = "specialty", required = false) String specialty) {

        List<DoctorResponse> doctors;

        if (specialty == null || specialty.trim().isEmpty()) {
            doctors = doctorService.getAllDoctors();
        } else {
            doctors = doctorService.searchDoctorsBySpecialty(specialty);
        }

        return ResponseEntity.ok(doctors);
    }

    // ═══════════════════════════════════════════════════════════
    // ALIAS VERS AUTRES CONTROLLERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir les rendez-vous d'un médecin
     * ALIAS VERS: AppointmentController.getAppointmentsByDoctor()
     */
    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @PathVariable("id") Long doctorId) {

        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Obtenir les créneaux horaires d'un médecin
     * ALIAS VERS: TimeSlotController.getTimeSlotsByDoctor()
     */
    @GetMapping("/{id}/timeslots")
    public ResponseEntity<List<TimeSlotResponse>> getDoctorTimeSlots(
            @PathVariable("id") Long doctorId) {

        List<TimeSlotResponse> timeSlots = timeSlotService.getTimeSlotsByDoctor(doctorId);
        return ResponseEntity.ok(timeSlots);
    }
}
