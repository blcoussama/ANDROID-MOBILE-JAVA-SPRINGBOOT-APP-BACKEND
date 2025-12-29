package com.cabinet.medical.exception;

import com.cabinet.medical.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * GlobalExceptionHandler - Gestionnaire global des exceptions
 *
 * RESPONSABILITÉS:
 * - Intercepter toutes les exceptions de l'application
 * - Convertir exceptions → réponses HTTP appropriées
 * - Uniformiser le format des erreurs (ErrorResponse)
 * - Gérer les erreurs de validation (@Valid)
 * - Logger les erreurs pour débogage
 *
 * ANNOTATIONS:
 * - @RestControllerAdvice : Intercepte exceptions de tous les @RestController
 * - @ExceptionHandler : Méthode qui gère un type d'exception spécifique
 * - @ResponseStatus : Code HTTP à retourner
 *
 * FORMAT ERREUR (ErrorResponse):
 * {
 * "timestamp": "2025-12-29T10:00:00",
 * "status": 404,
 * "error": "Not Found",
 * "message": "Utilisateur non trouvé",
 * "path": "/api/users/999",
 * "errors": null
 * }
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
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 404,
     * "error": "Not Found",
     * "message": "Utilisateur non trouvé avec id: 123",
     * "path": "/api/users/123",
     * "errors": null
     * }
     *
     * @param ex      ResourceNotFoundException
     * @param request HttpServletRequest pour obtenir le path
     * @return ResponseEntity<ErrorResponse> avec status 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                404,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
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
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 409,
     * "error": "Conflict",
     * "message": "Un compte existe déjà avec l'email: jean@gmail.com",
     * "path": "/api/auth/register",
     * "errors": null
     * }
     *
     * @param ex      EmailAlreadyExistsException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 409
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 409,
     * "error": "Conflict",
     * "message": "Un créneau existe déjà pour Lundi entre 09:00 et 12:00",
     * "path": "/api/timeslots",
     * "errors": null
     * }
     *
     * @param ex      TimeSlotConflictException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 409
     */
    @ExceptionHandler(TimeSlotConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleTimeSlotConflictException(
            TimeSlotConflictException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 409,
     * "error": "Conflict",
     * "message": "Le créneau du 2025-12-30T14:00 est déjà réservé pour Dr. Martin",
     * "path": "/api/appointments",
     * "errors": null
     * }
     *
     * @param ex      AppointmentConflictException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 409
     */
    @ExceptionHandler(AppointmentConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleAppointmentConflictException(
            AppointmentConflictException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Gérer InvalidCredentialsException (401 UNAUTHORIZED)
     *
     * DÉCLENCHÉE PAR:
     * - Login avec email ou mot de passe incorrect (UC-P02, D01, A01)
     *
     * RÉPONSE:
     * {
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 401,
     * "error": "Unauthorized",
     * "message": "Email ou mot de passe incorrect",
     * "path": "/api/auth/login",
     * "errors": null
     * }
     *
     * @param ex      InvalidCredentialsException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 401
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                401,
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Gérer erreurs de validation @Valid (400 BAD REQUEST)
     *
     * DÉCLENCHÉE PAR:
     * - Validation échouée sur @Valid @RequestBody
     *
     * EXEMPLES:
     * - Email invalide: "Format d'email invalide"
     * - Champ vide: "Le prénom est obligatoire"
     * - Mot de passe trop court: "Le mot de passe doit contenir au moins 6
     * caractères"
     *
     * RÉPONSE:
     * {
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 400,
     * "error": "Bad Request",
     * "message": "Erreur de validation",
     * "path": "/api/auth/register",
     * "errors": [
     * "L'email est obligatoire",
     * "Le mot de passe doit contenir au moins 6 caractères"
     * ]
     * }
     *
     * @param ex      MethodArgumentNotValidException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extraire les erreurs de validation dans une liste
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(fieldName + ": " + errorMessage);
        });

        ErrorResponse error = ErrorResponse.of(
                400,
                "Bad Request",
                "Erreur de validation",
                request.getRequestURI(),
                errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Gérer IllegalOperationException (403 FORBIDDEN)
     *
     * DÉCLENCHÉE PAR:
     * - Tentative de modifier un admin (UC-A06)
     * - Tentative de supprimer un admin (UC-A07)
     * - Opération interdite par règles métier
     *
     * RÉPONSE:
     * {
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 403,
     * "error": "Forbidden",
     * "message": "Vous ne pouvez pas modifier un administrateur",
     * "path": "/api/users/5",
     * "errors": null
     * }
     *
     * @param ex      IllegalOperationException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 403
     */
    @ExceptionHandler(IllegalOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleIllegalOperationException(
            IllegalOperationException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                403,
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Gérer IllegalArgumentException (400 BAD REQUEST)
     *
     * DÉCLENCHÉE PAR:
     * - startTime >= endTime (TimeSlotService)
     * - Validation métier échouée
     *
     * RÉPONSE:
     * {
     * "timestamp": "2025-12-29T20:16:14",
     * "status": 400,
     * "error": "Bad Request",
     * "message": "L'heure de début doit être avant l'heure de fin",
     * "path": "/api/timeslots",
     * "errors": null
     * }
     *
     * @param ex      IllegalArgumentException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
     * "timestamp": "2025-12-29T10:00:00",
     * "status": 500,
     * "error": "Internal Server Error",
     * "message": "Une erreur inattendue s'est produite: [message]",
     * "path": "/api/...",
     * "errors": null
     * }
     *
     * @param ex      Exception
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse> avec status 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        // Logger l'erreur pour débogage
        System.err.println("ERREUR INATTENDUE: " + ex.getClass().getName());
        ex.printStackTrace();

        ErrorResponse error = ErrorResponse.of(
                500,
                "Internal Server Error",
                "Une erreur inattendue s'est produite: " + ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}