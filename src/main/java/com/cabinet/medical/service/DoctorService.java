package com.cabinet.medical.service;

import com.cabinet.medical.dto.response.DoctorResponse;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.exception.ResourceNotFoundException;
import com.cabinet.medical.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DoctorService - Service de gestion des médecins
 *
 * RESPONSABILITÉS:
 * - Lister tous les médecins (public - pour patients)
 * - Filtrer médecins par spécialité
 * - Obtenir détails d'un médecin
 * - Conversion Doctor Entity → DoctorResponse DTO
 *
 * USE CASES:
 * - UC-P04: Patient voir liste médecins
 * - UC-P05: Patient voir créneaux disponibles médecin (liste des médecins)
 *
 * PERMISSIONS:
 * - Toutes les méthodes sont publiques (accessible à tous)
 * - Patients ont besoin de voir les médecins pour prendre RDV
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param doctorRepository Repository Doctor
     */
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /**
     * Lister tous les médecins (UC-P04)
     *
     * UTILISATION:
     * - Patient cherche un médecin pour prendre RDV
     * - Affichage liste complète des médecins du cabinet
     *
     * RETOURNE:
     * Liste de tous les médecins avec:
     * - Nom complet (Dr. Prénom Nom)
     * - Spécialité
     * - Email, téléphone
     *
     * @return List<DoctorResponse>
     */
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(DoctorResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir un médecin par ID
     *
     * UTILISATION:
     * - Détails d'un médecin spécifique
     * - Afficher infos médecin avant prise de RDV
     * - Vérifier existence médecin lors création RDV
     *
     * @param doctorId ID du médecin
     * @return DoctorResponse
     * @throws ResourceNotFoundException si médecin non trouvé
     */
    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Médecin", "id", doctorId));

        return DoctorResponse.from(doctor);
    }

    /**
     * Obtenir l'entité Doctor par ID (usage interne)
     *
     * UTILISATION:
     * Utilisé par d'autres services (AppointmentService, TimeSlotService)
     * pour obtenir l'entité Doctor directement
     *
     * @param doctorId ID du médecin
     * @return Doctor entity
     * @throws ResourceNotFoundException si médecin non trouvé
     */
    public Doctor getDoctorEntityById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Médecin", "id", doctorId));
    }

    /**
     * Filtrer médecins par spécialité
     *
     * UTILISATION:
     * - Patient cherche un cardiologue spécifiquement
     * - Filtrage par type de spécialité
     *
     * EXEMPLES:
     * - "Cardiologue" → Tous les cardiologues
     * - "Pédiatre" → Tous les pédiatres
     * - "Généraliste" → Tous les généralistes
     *
     * @param specialty Spécialité à filtrer (ex: "Cardiologue")
     * @return List<DoctorResponse>
     */
    public List<DoctorResponse> getDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialty(specialty)
                .stream()
                .map(DoctorResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher médecins par mot-clé dans la spécialité
     *
     * UTILISATION:
     * - Recherche flexible par spécialité
     * - Recherche partielle (ex: "cardio" trouve "Cardiologue")
     *
     * EXEMPLES:
     * - "cardio" → Trouve "Cardiologue"
     * - "péd" → Trouve "Pédiatre"
     * - Case insensitive
     *
     * @param keyword Mot-clé à rechercher
     * @return List<DoctorResponse>
     */
    public List<DoctorResponse> searchDoctorsBySpecialty(String keyword) {
        return doctorRepository.findBySpecialtyContainingIgnoreCase(keyword)
                .stream()
                .map(DoctorResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Vérifier si un médecin existe
     *
     * UTILISATION:
     * Validation rapide sans lever d'exception
     *
     * @param doctorId ID du médecin
     * @return true si existe, false sinon
     */
    public boolean doctorExists(Long doctorId) {
        return doctorRepository.existsById(doctorId);
    }

    /**
     * Compter le nombre total de médecins
     *
     * UTILISATION:
     * - Dashboard admin (statistiques)
     * - Métriques du cabinet
     *
     * @return Nombre total de médecins
     */
    public long countDoctors() {
        return doctorRepository.count();
    }
}
