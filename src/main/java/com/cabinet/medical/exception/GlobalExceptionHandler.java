package com.cabinet.medical.exception;

import com.cabinet.medical.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Gestionnaire global des exceptions
 *
 * RESPONSABILITÉS:
 * - Intercepter toutes les exceptions de l'application
 * - Convertir exceptions → réponses HTTP appropriées
 * - Uniformiser le format des erreurs (ApiResponse)
 * - Gérer les erreurs de validation (@Valid)
 * - Logger les erreurs pour débogage
 *
 * ANNOTATIONS:
 * - @RestControllerAdvice : Intercepte exceptions de tous les @RestController
 * - @ExceptionHandler : Méthode qui gère un type d'exception spécifique
 * - @ResponseStatus : Code HTTP à retourner
 *
 * EXCEPTIONS GÉRÉES:
 * - ResourceNotFoundException → 404 NOT FOUND
 * - EmailAlreadyExistsException → 409 CONFLICT
 * - TimeSlotConflictException → 409 CONFLICT
 * - AppointmentConflictException → 409 CONFLICT
 * - InvalidCredentialsException → 401 UNAUTHORIZED
 * - MethodArgumentNotValidException → 400 BAD REQUEST
 * - Exception (générique) → 500 INTERNAL SERVER ERROR
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gérer ResourceNotFoundException (404 NOT FOUND)
     *
     * DÉCLENCHÉE PAR:
     * - User/Patient/Doctor/Appointment/TimeSlot non trouvé
     *
     * EXEMPLE:
     * User user = userRepository.findById(id)
     * .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
     *
     * RÉPONSE:
     * {
     * "success": false,
     * "message": "User not found with id: 123",
     * "data": null
     * }
     *
     * @param ex ResourceNotFoundException
     * @return ResponseEntity<ApiResponse<Void>> avec status 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Gérer EmailAlreadyExistsException (409 CONFLICT)
     *
     * DÉCLENCHÉE PAR:
     * - Inscription avec email déjà utilisé (UC-P01)
     * - Création user avec email existant (UC-A04, A05)
     * - Modification user avec nouvel email existant (UC-A06)
     *
     * RÈGLE MÉTIER:
     * RG-01: Email unique
     *
     * RÉPONSE:
     * {
     * "success": false,
     * "message": "Email already exists: jean@gmail.com",
     * "data": null
     * }
     *
     * @param ex EmailAlreadyExistsException
     * @return ResponseEntity<ApiResponse<Void>> avec status 409
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Gérer TimeSlotConflictException (409 CONFLICT)
     *
     * DÉCLENCHÉE PAR:
     * - Création créneau qui chevauche un existant (UC-D02, A08)
     * - Modification créneau qui crée un chevauchement (UC-D02, A08)
     *
     * RÈGLE MÉTIER:
     * RG-08: Créneaux ne peuvent pas chevaucher (même doctor, même jour)
     *
     * RÉPONSE:
     * {
     * "success": false,
     * "message": "Time slot conflict: MONDAY 09:00-12:00 overlaps with existing
     * slot",
     * "data": null
     * }
     *
     * @param ex TimeSlotConflictException
     * @return ResponseEntity<ApiResponse<Void>> avec status 409
     */
    @ExceptionHandler(TimeSlotConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleTimeSlotConflictException(
            TimeSlotConflictException ex) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Gérer AppointmentConflictException (409 CONFLICT)
     *
     * DÉCLENCHÉE PAR:
     * - Création RDV sur créneau déjà pris (UC-P06)
     * - Modification RDV vers créneau occupé (UC-P07, D05, A10)
     * - Déplacement RDV vers créneau occupé (UC-A12)
     *
     * RÈGLE MÉTIER:
     * RG-02: Un seul RDV par créneau médecin (UNIQUE doctor_id + date_time)
     *
     * RÉPONSE:
     * {
     * "success": false,
     * "message": "Appointment conflict: Dr. Martin already has appointment at
     * 2025-12-30 14:00",
     * "data": null
     * }
     *
     * @param ex AppointmentConflictException
     * @return ResponseEntity<ApiResponse<Void>> avec status 409
     */
    @ExceptionHandler(AppointmentConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleAppointmentConflictException(
            AppointmentConflictException ex) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Gérer InvalidCredentialsException (401 UNAUTHORIZED)
     *
     * DÉCLENCHÉE PAR:
     * - Login avec email ou mot de passe incorrect (UC-P02, D01, A01)
     *
     * RÉPONSE:
     * {
     * "success": false,
     * "message": "Invalid email or password",
     * "data": null
     * }
     *
     * @param ex InvalidCredentialsException
     * @return ResponseEntity<ApiResponse<Void>> avec status 401
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentialsException(
            InvalidCredentialsException ex) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gérer erreurs de validation @Valid (400 BAD REQUEST)
     *
     * DÉCLENCHÉE PAR:
     * - Validation échouée sur @Valid @RequestBody
     *
     * EXEMPLES:
     * - Email invalide: "Email invalide"
     * - Champ vide: "Le prénom est obligatoire"
     * - Mot de passe trop court: "Le mot de passe doit contenir au moins 8
     * caractères"
     *
     * RÉPONSE:
     * {
     * "success": false,
     * "message": "Validation failed",
     * "data": {
     * "email": "Email invalide",
     * "password": "Le mot de passe doit contenir au moins 8 caractères"
     * }
     * }
     *
     * @param ex MethodArgumentNotValidException
     * @return ResponseEntity<ApiResponse<Map<String, String>>> avec status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        // Extraire les erreurs de validation
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gérer toutes les autres exceptions non gérées (500 INTERNAL SERVER ERROR)
     *
     * DÉCLENCHÉE PAR:
     * - Erreur inattendue (bug dans le code)
     * - Erreur base de données
     * - Erreur réseau
     *
     * RÉPONSE:
     * {
     * "success": false,
     * "message": "An unexpected error occurred: [message]",
     * "data": null
     * }
     *
     * @param ex Exception
     * @return ResponseEntity<ApiResponse<Void>> avec status 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {

        // Logger l'erreur pour débogage
        System.err.println("ERREUR INATTENDUE: " + ex.getClass().getName());
        ex.printStackTrace();

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred: " + ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
