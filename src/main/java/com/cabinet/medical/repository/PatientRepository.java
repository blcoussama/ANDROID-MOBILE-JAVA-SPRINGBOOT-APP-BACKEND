package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PatientRepository - Interface pour gérer l'accès aux données des patients
 *
 * RESPONSABILITÉS:
 * - CRUD sur la table patient
 * - Recherche par User (relation OneToOne)
 * - Utilisé par PatientService, AppointmentService
 *
 * RELATION:
 * - Patient (1) ↔ User (1) : OneToOne
 * - Patient (1) → Appointment (*) : OneToMany
 *
 * MÉTHODES GRATUITES (JpaRepository):
 * - save(patient) : Créer/Modifier patient
 * - findById(id) : Trouver par ID
 * - findAll() : Liste tous les patients
 * - deleteById(id) : Supprimer par ID
 * - count() : Compter patients
 *
 * MÉTHODES CUSTOM:
 * - findByUser(user) : Trouver patient par son User
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR RELATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un patient par son User associé
     * Utilisé après login pour récupérer les infos patient (UC-P02)
     *
     * SQL généré: SELECT * FROM patient WHERE user_id = ?
     *
     * Exemple d'utilisation:
     * User user = userRepository.findByEmail("jean@gmail.com");
     * Patient patient = patientRepository.findByUser(user).orElseThrow();
     *
     * @param user L'utilisateur associé
     * @return Optional<Patient> (vide si non trouvé)
     */
    Optional<Patient> findByUser(User user);

    /**
     * Vérifie si un patient existe pour un User donné
     * Utilisé pour validation (éviter doublons)
     *
     * SQL généré: SELECT COUNT(*) > 0 FROM patient WHERE user_id = ?
     *
     * @param user L'utilisateur à vérifier
     * @return true si le patient existe, false sinon
     */
    boolean existsByUser(User user);
}
