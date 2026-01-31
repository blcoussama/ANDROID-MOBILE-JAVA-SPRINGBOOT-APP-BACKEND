package com.cabinet.medical.controller;

import com.cabinet.medical.dto.response.DoctorResponse;
import com.cabinet.medical.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
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
}
