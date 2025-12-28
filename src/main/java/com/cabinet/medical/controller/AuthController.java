package com.cabinet.medical.controller;

import com.cabinet.medical.dto.request.LoginRequest;
import com.cabinet.medical.dto.request.RegisterRequest;
import com.cabinet.medical.dto.response.AuthResponse;
import com.cabinet.medical.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller AuthController
 * Endpoints authentification: login, register
 * Routes publiques (pas de JWT requis)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS pour Android
public class AuthController {

    private final AuthService authService;

    // ═══════════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /api/auth/login
     * Authentifier un utilisateur
     *
     * @param request LoginRequest (email + password)
     * @return AuthResponse avec JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Ne pas exposer détails erreur (sécurité)
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTER
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /api/auth/register
     * Inscrire un nouvel utilisateur
     *
     * @param request RegisterRequest
     * @return AuthResponse avec JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {
            // Retourner message d'erreur (pour UX)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ERROR RESPONSE (classe interne)
    // ═══════════════════════════════════════════════════════════

    /**
     * Classe pour réponses d'erreur
     * Sera remplacée par une gestion centralisée plus tard
     */
    public record ErrorResponse(String message) {
    }
}
