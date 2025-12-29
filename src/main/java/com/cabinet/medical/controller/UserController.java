package com.cabinet.medical.controller;

import com.cabinet.medical.dto.request.CreateUserRequest;
import com.cabinet.medical.dto.request.UpdateUserRequest;
import com.cabinet.medical.dto.response.UserResponse;
import com.cabinet.medical.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController - Contrôleur pour la gestion des utilisateurs (ADMIN
 * uniquement)
 *
 * ENDPOINTS:
 * - GET /api/users : Liste tous les utilisateurs (UC-A03)
 * - GET /api/users/{id} : Détails d'un utilisateur
 * - POST /api/users/patient : Créer un patient (UC-A04)
 * - POST /api/users/doctor : Créer un médecin (UC-A05)
 * - PUT /api/users/{id} : Modifier un utilisateur (UC-A06)
 * - DELETE /api/users/{id} : Supprimer un utilisateur (UC-A07)
 *
 * PERMISSIONS:
 * - Tous les endpoints réservés aux ADMIN
 * - À sécuriser avec @PreAuthorize("hasRole('ADMIN')") à l'étape JWT
 *
 * FORMAT RÉPONSES:
 * - Succès: UserResponse direct (pas de wrapper)
 * - Erreurs: ErrorResponse (géré par GlobalExceptionHandler)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lister tous les utilisateurs (UC-A03)
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Obtenir détails d'un utilisateur
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Créer un patient (UC-A04)
     */
    @PostMapping("/patient")
    public ResponseEntity<UserResponse> createPatient(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Créer un médecin (UC-A05)
     */
    @PostMapping("/doctor")
    public ResponseEntity<UserResponse> createDoctor(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Modifier un utilisateur (UC-A06)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Supprimer un utilisateur (UC-A07)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
