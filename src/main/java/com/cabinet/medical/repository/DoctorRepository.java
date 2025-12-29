package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DoctorRepository - Interface pour gérer l'accès aux données des médecins
 *
 * RESPONSABILITÉS:
 * - CRUD sur la table doctor
 * - Recherche par User (relation OneToOne)
 * - Recherche par specialty (filtrage liste médecins)
 * - Utilisé par DoctorService, AppointmentService, TimeSlotService
 *
 * RELATION:
 * - Doctor (1) ↔ User (1) : OneToOne
 * - Doctor (1) → TimeSlot (*) : OneToMany
 * - Doctor (1) → Appointment (*) : OneToMany
 *
 * MÉTHODES GRATUITES (JpaRepository):
 * - save(doctor) : Créer/Modifier médecin
 * - findById(id) : Trouver par ID
 * - findAll() : Liste tous les médecins (UC-P04)
 * - deleteById(id) : Supprimer par ID
 * - count() : Compter médecins
 *
 * MÉTHODES CUSTOM:
 * - findByUser(user) : Trouver médecin par son User
 * - findBySpecialty(specialty) : Filtrer par spécialité
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR RELATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un médecin par son User associé
     * Utilisé après login pour récupérer les infos médecin (UC-D01)
     *
     * SQL généré: SELECT * FROM doctor WHERE user_id = ?
     *
     * Exemple d'utilisation:
     * User user = userRepository.findByEmail("martin@doc.com");
     * Doctor doctor = doctorRepository.findByUser(user).orElseThrow();
     *
     * @param user L'utilisateur associé
     * @return Optional<Doctor> (vide si non trouvé)
     */
    Optional<Doctor> findByUser(User user);

    /**
     * Vérifie si un médecin existe pour un User donné
     * Utilisé pour validation (éviter doublons)
     *
     * SQL généré: SELECT COUNT(*) > 0 FROM doctor WHERE user_id = ?
     *
     * @param user L'utilisateur à vérifier
     * @return true si le médecin existe, false sinon
     */
    boolean existsByUser(User user);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR SPECIALTY
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve tous les médecins d'une spécialité donnée
     * Utilisé pour filtrer la liste des médecins (UC-P04)
     *
     * SQL généré: SELECT * FROM doctor WHERE specialty = ?
     *
     * Exemple d'utilisation:
     * List<Doctor> cardiologues = doctorRepository.findBySpecialty("Cardiologue");
     *
     * @param specialty La spécialité à filtrer
     * @return Liste des médecins avec cette spécialité
     */
    List<Doctor> findBySpecialty(String specialty);

    /**
     * Trouve tous les médecins dont la spécialité contient le mot-clé
     * Utilisé pour recherche flexible (ex: "cardio" trouve "Cardiologue")
     *
     * SQL généré: SELECT * FROM doctor WHERE specialty LIKE %keyword%
     *
     * @param keyword Mot-clé de recherche
     * @return Liste des médecins correspondants
     */
    List<Doctor> findBySpecialtyContainingIgnoreCase(String keyword);
}
