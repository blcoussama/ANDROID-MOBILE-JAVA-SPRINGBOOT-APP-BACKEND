package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AppointmentRepository - Interface pour gérer l'accès aux rendez-vous
 *
 * RESPONSABILITÉS:
 * - CRUD sur la table appointment
 * - Recherche RDV par patient (UC-P03)
 * - Recherche RDV par médecin (UC-D03)
 * - Recherche RDV par status (UC-A02)
 * - Vérification disponibilité créneau (UC-P06)
 * - Statistiques dashboard (UC-A02)
 * - Utilisé par AppointmentService, DashboardService
 *
 * RELATION:
 * - Appointment (*) → Patient (1) : ManyToOne
 * - Appointment (*) → Doctor (1) : ManyToOne
 * - Appointment (1) → Notification (*) : OneToMany
 *
 * CONTRAINTES:
 * - UNIQUE(doctor_id, date_time) : Un médecin = un RDV à un instant donné
 *
 * MÉTHODES GRATUITES (JpaRepository):
 * - save(appointment) : Créer/Modifier RDV
 * - findById(id) : Trouver par ID
 * - findAll() : Liste tous les RDV (UC-A09)
 * - deleteById(id) : Supprimer par ID
 * - count() : Compter RDV
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR PATIENT
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve tous les rendez-vous d'un patient
     * Utilisé pour historique RDV (UC-P03)
     *
     * SQL généré: SELECT * FROM appointment WHERE patient_id = ?
     * ORDER BY date_time DESC
     *
     * @param patient Le patient
     * @return Liste de ses RDV (du plus récent au plus ancien)
     */
    List<Appointment> findByPatientOrderByDateTimeDesc(Patient patient);

    /**
     * Trouve les RDV futurs d'un patient
     * Utilisé pour afficher les RDV à venir (UC-P03)
     *
     * SQL généré: SELECT * FROM appointment
     * WHERE patient_id = ? AND date_time > NOW()
     * ORDER BY date_time ASC
     *
     * @param patient Le patient
     * @param now     La date/heure actuelle
     * @return Liste des RDV futurs
     */
    List<Appointment> findByPatientAndDateTimeAfterOrderByDateTimeAsc(
            Patient patient,
            LocalDateTime now);

    /**
     * Trouve les RDV passés d'un patient
     * Utilisé pour historique (UC-P03)
     *
     * SQL généré: SELECT * FROM appointment
     * WHERE patient_id = ? AND date_time < NOW()
     * ORDER BY date_time DESC
     *
     * @param patient Le patient
     * @param now     La date/heure actuelle
     * @return Liste des RDV passés
     */
    List<Appointment> findByPatientAndDateTimeBeforeOrderByDateTimeDesc(
            Patient patient,
            LocalDateTime now);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR MÉDECIN
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve tous les rendez-vous d'un médecin
     * Utilisé pour consulter les RDV programmés (UC-D03)
     *
     * SQL généré: SELECT * FROM appointment WHERE doctor_id = ?
     * ORDER BY date_time DESC
     *
     * @param doctor Le médecin
     * @return Liste de ses RDV
     */
    List<Appointment> findByDoctorOrderByDateTimeDesc(Doctor doctor);

    /**
     * Trouve les RDV futurs d'un médecin
     * Utilisé pour afficher le planning (UC-D03)
     *
     * SQL généré: SELECT * FROM appointment
     * WHERE doctor_id = ? AND date_time > NOW()
     * ORDER BY date_time ASC
     *
     * @param doctor Le médecin
     * @param now    La date/heure actuelle
     * @return Liste des RDV futurs
     */
    List<Appointment> findByDoctorAndDateTimeAfterOrderByDateTimeAsc(
            Doctor doctor,
            LocalDateTime now);

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE PAR STATUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve les RDV par status
     * Utilisé pour dashboard admin (UC-A02)
     *
     * SQL généré: SELECT * FROM appointment WHERE status = ?
     *
     * @param status Le status (PENDING, CONFIRMED, CANCELLED)
     * @return Liste des RDV avec ce status
     */
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    /**
     * Compte les RDV par status
     * Utilisé pour statistiques dashboard (UC-A02)
     *
     * SQL généré: SELECT COUNT(*) FROM appointment WHERE status = ?
     *
     * @param status Le status
     * @return Nombre de RDV avec ce status
     */
    long countByStatus(Appointment.AppointmentStatus status);

    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATION DISPONIBILITÉ
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si un médecin a déjà un RDV à une date/heure précise
     * Utilisé pour valider disponibilité (UC-P06)
     *
     * SQL généré: SELECT COUNT(*) > 0 FROM appointment
     * WHERE doctor_id = ? AND date_time = ?
     * AND status != 'CANCELLED'
     *
     * @param doctor   Le médecin
     * @param dateTime La date/heure du RDV
     * @return true si créneau occupé, false si disponible
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.doctor = :doctor " +
            "AND a.dateTime = :dateTime " +
            "AND a.status != 'CANCELLED'")
    boolean existsByDoctorAndDateTimeAndNotCancelled(
            @Param("doctor") Doctor doctor,
            @Param("dateTime") LocalDateTime dateTime);

    /**
     * Trouve les RDV d'un médecin pour une date spécifique
     * Utilisé pour calculer les créneaux disponibles (UC-P05)
     *
     * SQL généré: SELECT * FROM appointment
     * WHERE doctor_id = ?
     * AND DATE(date_time) = DATE(?)
     * AND status != 'CANCELLED'
     *
     * @param doctor     Le médecin
     * @param startOfDay Début de la journée (00:00:00)
     * @param endOfDay   Fin de la journée (23:59:59)
     * @return Liste des RDV de ce jour
     */
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.doctor = :doctor " +
            "AND a.dateTime BETWEEN :startOfDay AND :endOfDay " +
            "AND a.status != 'CANCELLED'")
    List<Appointment> findByDoctorAndDate(
            @Param("doctor") Doctor doctor,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES DASHBOARD
    // ═══════════════════════════════════════════════════════════

    /**
     * Compte les RDV aujourd'hui
     * Utilisé pour dashboard admin (UC-A02)
     *
     * SQL généré: SELECT COUNT(*) FROM appointment
     * WHERE DATE(date_time) = CURRENT_DATE
     *
     * @param startOfDay Début de la journée
     * @param endOfDay   Fin de la journée
     * @return Nombre de RDV aujourd'hui
     */
    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.dateTime BETWEEN :startOfDay AND :endOfDay")
    long countAppointmentsToday(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Compte les RDV cette semaine
     * Utilisé pour dashboard admin (UC-A02)
     *
     * @param startOfWeek Début de la semaine
     * @param endOfWeek   Fin de la semaine
     * @return Nombre de RDV cette semaine
     */
    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.dateTime BETWEEN :startOfWeek AND :endOfWeek")
    long countAppointmentsThisWeek(
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek);
}
