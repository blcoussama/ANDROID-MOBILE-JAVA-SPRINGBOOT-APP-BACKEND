package com.cabinet.medical.controller;

import com.cabinet.medical.dto.request.LoginRequest;
import com.cabinet.medical.dto.request.RegisterRequest;
import com.cabinet.medical.dto.response.LoginResponse;
import com.cabinet.medical.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthController - Contrôleur pour l'authentification
 *
 * ENDPOINTS:
 * - POST /api/auth/register : Inscription patient (UC-P01)
 * - POST /api/auth/login : Connexion (UC-P02, UC-D01, UC-A01)
 * - GET /api/auth/email-exists : Vérifier email disponible
 *
 * PERMISSIONS:
 * - register : PUBLIC (tout le monde peut créer un compte patient)
 * - login : PUBLIC (tout le monde peut se connecter)
 * - email-exists : PUBLIC (validation frontend)
 *
 * FORMAT RÉPONSES:
 * - SUCCÈS : DTOs directs (LoginResponse, etc.)
 * - ERREURS : ErrorResponse (géré par GlobalExceptionHandler)
 *
 * ANNOTATIONS:
 * - @RestController : Contrôleur REST (retourne JSON)
 * - @RequestMapping : Préfixe d'URL pour tous les endpoints
 * - @PostMapping : Endpoint POST
 * - @GetMapping : Endpoint GET
 * - @Valid : Validation automatique des DTOs
 * - @RequestBody : Corps de la requête (JSON → objet Java)
 * - @RequestParam : Paramètre de query string
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param authService Service d'authentification
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscription d'un nouveau patient (UC-P01)
     *
     * ENDPOINT: POST /api/auth/register
     *
     * REQUEST BODY:
     * {
     * "email": "jean@gmail.com",
     * "password": "password123",
     * "firstName": "Jean",
     * "lastName": "Dupont",
     * "phone": "0612345678"
     * }
     *
     * RESPONSE 201 CREATED:
     * {
     * "token": "TODO_JWT_TOKEN",
     * "userId": 1,
     * "email": "jean@gmail.com",
     * "firstName": "Jean",
     * "lastName": "Dupont",
     * "role": "PATIENT",
     * "patientId": 1,
     * "doctorId": null
     * }
     *
     * ERREURS (ErrorResponse):
     * - 400 BAD REQUEST : Validation échouée
     * {
     * "timestamp": "2025-12-29T16:30:45",
     * "status": 400,
     * "error": "Bad Request",
     * "message": "Erreur de validation",
     * "path": "/api/auth/register",
     * "errors": ["email: Format d'email invalide"]
     * }
     *
     * - 409 CONFLICT : Email déjà utilisé
     * {
     * "timestamp": "2025-12-29T16:30:45",
     * "status": 409,
     * "error": "Conflict",
     * "message": "Un compte existe déjà avec l'email: jean@gmail.com",
     * "path": "/api/auth/register",
     * "errors": null
     * }
     *
     * @param request RegisterRequest (validé automatiquement par @Valid)
     * @return ResponseEntity<LoginResponse> avec status 201 CREATED
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion utilisateur (UC-P02, UC-D01, UC-A01)
     *
     * ENDPOINT: POST /api/auth/login
     *
     * REQUEST BODY:
     * {
     * "email": "jean@gmail.com",
     * "password": "password123"
     * }
     *
     * RESPONSE 200 OK (Patient):
     * {
     * "token": "TODO_JWT_TOKEN",
     * "userId": 1,
     * "email": "jean@gmail.com",
     * "firstName": "Jean",
     * "lastName": "Dupont",
     * "role": "PATIENT",
     * "patientId": 1,
     * "doctorId": null
     * }
     *
     * RESPONSE 200 OK (Doctor):
     * {
     * "token": "TODO_JWT_TOKEN",
     * "userId": 2,
     * "email": "martin@doc.com",
     * "firstName": "Martin",
     * "lastName": "Durand",
     * "role": "DOCTOR",
     * "patientId": null,
     * "doctorId": 1
     * }
     *
     * ERREURS (ErrorResponse):
     * - 400 BAD REQUEST : Validation échouée
     * {
     * "timestamp": "2025-12-29T16:30:45",
     * "status": 400,
     * "error": "Bad Request",
     * "message": "Erreur de validation",
     * "path": "/api/auth/login",
     * "errors": ["email: L'email est obligatoire"]
     * }
     *
     * - 401 UNAUTHORIZED : Email ou mot de passe incorrect
     * {
     * "timestamp": "2025-12-29T16:30:45",
     * "status": 401,
     * "error": "Unauthorized",
     * "message": "Email ou mot de passe incorrect",
     * "path": "/api/auth/login",
     * "errors": null
     * }
     *
     * @param request LoginRequest (validé automatiquement par @Valid)
     * @return ResponseEntity<LoginResponse> avec status 200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier si un email existe déjà
     *
     * ENDPOINT: GET /api/auth/email-exists?email=test@example.com
     *
     * UTILISATION:
     * Validation frontend en temps réel lors de l'inscription
     *
     * EXEMPLE REQUÊTE:
     * GET /api/auth/email-exists?email=jean@gmail.com
     *
     * RESPONSE 200 OK (email existe):
     * {
     * "exists": true,
     * "message": "Email déjà utilisé"
     * }
     *
     * RESPONSE 200 OK (email disponible):
     * {
     * "exists": false,
     * "message": "Email disponible"
     * }
     *
     * UTILISATION FRONTEND:
     * - Lors de la saisie email dans formulaire inscription
     * - Afficher message "Email déjà utilisé" en temps réel
     * - Désactiver bouton "S'inscrire" si email existe
     *
     * FORMAT:
     * Retourne un simple JSON avec 2 champs (pas de wrapper)
     * Compatible avec le standard REST (pas de wrapper inutile)
     *
     * @param email Email à vérifier (query parameter)
     * @return ResponseEntity<Map<String, Object>> avec status 200 OK
     */
    @GetMapping("/email-exists")
    public ResponseEntity<Map<String, Object>> emailExists(@RequestParam String email) {
        boolean exists = authService.emailExists(email);

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("message", exists ? "Email déjà utilisé" : "Email disponible");

        return ResponseEntity.ok(response);
    }

    /**
     * Valider le token JWT (UC-VALIDATE)
     *
     * ENDPOINT: GET /api/auth/validate
     *
     * SÉCURITÉ:
     * - Endpoint protégé par Spring Security (ligne 130 SecurityConfig)
     * - JwtAuthenticationFilter valide automatiquement le token
     * - Si token invalide/expiré → 401 UNAUTHORIZED (automatique)
     * - Si token valide → Ce controller est appelé → 200 OK
     *
     * FLOW:
     * 1. Client envoie: Header "Authorization: Bearer {token}"
     * 2. JwtAuthenticationFilter intercepte la requête
     * 3. JwtAuthenticationFilter valide le token (signature + expiration)
     * 4. Si token INVALIDE → 401 automatique (ne passe PAS au controller)
     * 5. Si token VALIDE → Controller appelé et retourne 200 OK
     *
     * UTILISATION ANDROID (SplashActivity):
     * - Au démarrage de l'app
     * - Vérifier si le token stocké est encore valide
     * - Si 200 OK → MainActivity (utilisateur connecté)
     * - Si 401 UNAUTHORIZED → LoginActivity (token expiré)
     * - Si erreur réseau → LoginActivity (sécurité)
     *
     * EXEMPLE REQUÊTE:
     * GET /api/auth/validate
     * Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * RESPONSE 200 OK (token valide):
     * {
     *   "valid": true,
     *   "message": "Token valide"
     * }
     *
     * RESPONSE 401 UNAUTHORIZED (token expiré/invalide):
     * {
     *   "timestamp": "2025-01-31T10:00:00",
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "Token invalide ou expiré",
     *   "path": "/api/auth/validate",
     *   "errors": null
     * }
     *
     * NOTE IMPORTANTE:
     * Si cette méthode est exécutée, le token est FORCÉMENT valide
     * (sinon Spring Security aurait retourné 401 avant d'arriver ici)
     *
     * @return ResponseEntity<Map<String, Object>> avec status 200 OK
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken() {
        // Si on arrive ici, le token est valide (vérifié par JwtAuthenticationFilter)
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("message", "Token valide");

        return ResponseEntity.ok(response);
    }
}