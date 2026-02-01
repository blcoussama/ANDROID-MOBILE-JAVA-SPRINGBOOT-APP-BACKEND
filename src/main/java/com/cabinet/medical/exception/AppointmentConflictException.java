package com.cabinet.medical.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AppointmentConflictException - Exception levée quand un créneau est déjà
 * réservé
 *
 * UTILISATION:
 * - Prendre RDV sur créneau occupé (UC-P06)
 * - Modifier RDV vers créneau occupé (UC-P07)
 * - Déplacer RDV vers créneau occupé (UC-A12)
 *
 * HTTP STATUS:
 * - 409 CONFLICT
 *
 * RÈGLE MÉTIER:
 * Un médecin ne peut avoir qu'un seul RDV à une date/heure donnée
 *
 * EXEMPLE:
 * throw new AppointmentConflictException("Dr. Martin", "2025-12-30T14:00");
 * → Message: "Le créneau du 2025-12-30 à 14:00 est déjà réservé pour Dr.
 * Martin"
 */
public class AppointmentConflictException extends RuntimeException {

    /**
     * Nom du médecin
     */
    private String doctorName;

    /**
     * Date et heure du créneau
     */
    private LocalDateTime dateTime;

    /**
     * Constructeur avec nom médecin et date/heure
     *
     * @param doctorName Nom du médecin
     * @param dateTime   Date et heure du RDV
     */
    public AppointmentConflictException(String doctorName, LocalDateTime dateTime) {
        super(String.format("Un rendez-vous est déjà réservé le %s à %s pour %s",
                dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                doctorName));
        this.doctorName = doctorName;
        this.dateTime = dateTime;
    }

    /**
     * Constructeur avec message personnalisé
     *
     * @param message Message d'erreur
     */
    public AppointmentConflictException(String message) {
        super(message);
    }

    // Getters

    public String getDoctorName() {
        return doctorName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
