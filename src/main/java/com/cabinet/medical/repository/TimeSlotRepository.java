package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * TimeSlotRepository - Interface pour gérer l'accès aux créneaux horaires
 *
 * RESPONSABILITÉS:
 * - CRUD sur la table timeslot
 * - Recherche créneaux par médecin (UC-D02, UC-P05)
 * - Recherche créneaux par jour de la semaine
 * - Vérification chevauchements
 * - Utilisé par TimeSlotService, AppointmentService
 *
 * RELATION:
 * - TimeSlot (*) → Doctor (1) : ManyToOne
 *
 * CONTRAINTES:
 * - UNIQUE(doctor_id, day_of_week, start_time)
 * - Pas de chevauchements pour même médecin, même jour
 *
 * MÉTHODES GRATUITES (JpaRepository):
 * - save(timeSlot) : Créer/Modifier créneau
 * - findById(id) : Trouver par ID
 * - findAll() : Liste tous les créneaux (UC-A08)
 * - deleteById(id) : Supprimer par ID
 * - count() : Compter créneaux
 *
 * MÉTHODES CUSTOM:
 * - findByDoctor(doctor) : Créneaux d'un médecin
 * - findByDoctorAndDayOfWeek() : Créneaux d'un médecin un jour spécifique
 * - findOverlappingTimeSlots() : Vérifier chevauchements
 */
@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR MÉDECIN
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve tous les créneaux d'un médecin
     * Utilisé pour afficher/gérer les créneaux d'un médecin (UC-D02)
     *
     * SQL généré: SELECT * FROM timeslot WHERE doctor_id = ?
     *
     * @param doctor Le médecin
     * @return Liste de tous ses créneaux
     */
    List<TimeSlot> findByDoctor(Doctor doctor);

    /**
     * Trouve tous les créneaux d'un médecin pour un jour spécifique
     * Utilisé pour calculer disponibilités (UC-P05)
     *
     * SQL généré: SELECT * FROM timeslot WHERE doctor_id = ? AND day_of_week = ?
     *
     * Exemple d'utilisation:
     * List<TimeSlot> creneauxLundi = timeSlotRepository
     * .findByDoctorAndDayOfWeek(doctor, DayOfWeek.MONDAY);
     *
     * @param doctor    Le médecin
     * @param dayOfWeek Le jour de la semaine (MONDAY, TUESDAY, etc.)
     * @return Liste des créneaux pour ce jour
     */
    List<TimeSlot> findByDoctorAndDayOfWeek(Doctor doctor, DayOfWeek dayOfWeek);

    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATION CHEVAUCHEMENTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve les créneaux qui chevauchent un nouveau créneau
     * Utilisé pour valider qu'un nouveau créneau ne chevauche pas (UC-D02)
     *
     * LOGIQUE:
     * Deux créneaux chevauchent si:
     * - Même médecin
     * - Même jour
     * - startTime < other.endTime ET endTime > other.startTime
     *
     * SQL généré avec @Query personnalisée (voir ci-dessous)
     *
     * @param doctor    Le médecin
     * @param dayOfWeek Le jour de la semaine
     * @param startTime Heure de début du nouveau créneau
     * @param endTime   Heure de fin du nouveau créneau
     * @param excludeId ID à exclure (pour modification, null pour création)
     * @return Liste des créneaux qui chevauchent
     */
    @Query("SELECT t FROM TimeSlot t WHERE t.doctor = :doctor " +
            "AND t.dayOfWeek = :dayOfWeek " +
            "AND t.startTime < :endTime " +
            "AND t.endTime > :startTime " +
            "AND (:excludeId IS NULL OR t.id != :excludeId)")
    List<TimeSlot> findOverlappingTimeSlots(
            @Param("doctor") Doctor doctor,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE SPÉCIFIQUE
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un créneau spécifique par médecin, jour et heure de début
     * Utilisé pour vérifier unicité (contrainte DB)
     *
     * SQL généré: SELECT * FROM timeslot
     * WHERE doctor_id = ? AND day_of_week = ? AND start_time = ?
     *
     * @param doctor    Le médecin
     * @param dayOfWeek Le jour
     * @param startTime L'heure de début
     * @return Optional<TimeSlot> (vide si non trouvé)
     */
    Optional<TimeSlot> findByDoctorAndDayOfWeekAndStartTime(
            Doctor doctor,
            DayOfWeek dayOfWeek,
            LocalTime startTime);
}
