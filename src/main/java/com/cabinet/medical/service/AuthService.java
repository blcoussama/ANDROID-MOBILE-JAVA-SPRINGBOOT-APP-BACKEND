package com.cabinet.medical.service;

import com.cabinet.medical.dto.request.LoginRequest;
import com.cabinet.medical.dto.request.RegisterRequest;
import com.cabinet.medical.dto.response.LoginResponse;
import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.User;
import com.cabinet.medical.exception.EmailAlreadyExistsException;
import com.cabinet.medical.exception.InvalidCredentialsException;
import com.cabinet.medical.repository.PatientRepository;
import com.cabinet.medical.repository.DoctorRepository;
import com.cabinet.medical.repository.UserRepository;
import com.cabinet.medical.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AuthService - Service d'authentification et inscription (AVEC JWT)
 *
 * RESPONSABILITÉS:
 * - Inscription des patients (UC-P01)
 * - Connexion (login) de tous les utilisateurs (UC-P02, UC-D01, UC-A01)
 * - Génération de tokens JWT
 * - Hashage sécurisé des mots de passe (BCrypt)
 * - Création automatique de l'entité Patient lors inscription
 * - Validation des identifiants
 *
 * USE CASES:
 * - UC-P01: Patient crée un compte
 * - UC-P02: Patient se connecte
 * - UC-D01: Doctor se connecte
 * - UC-A01: Admin se connecte
 *
 * SÉCURITÉ:
 * - Passwords hashés avec BCrypt (coût: 10)
 * - JAMAIS de password en clair en base
 * - Vérification hash lors login
 * - JWT token généré et signé
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; //

    /**
     * Constructeur avec injection de dépendances
     *
     * @param userRepository    Repository User
     * @param patientRepository Repository Patient
     * @param jwtUtil           Utilitaire JWT
     */
    public AuthService(UserRepository userRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            JwtUtil jwtUtil) { // paramètre
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil; //
    }

    /**
     * Inscription d'un nouveau patient (UC-P01) ⭐ MODIFIÉ AVEC JWT
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=PATIENT, password hashé)
     * 3. Créer Patient (lié au User)
     * 4. Générer token JWT
     * 5. Retourner LoginResponse avec token
     *
     * RÈGLES MÉTIER:
     * - RG-01: Email doit être unique
     * - Password hashé en BCrypt
     * - Patient créé automatiquement
     * - Token JWT généré automatiquement
     *
     * @param request RegisterRequest (email, password, firstName, lastName, phone)
     * @return LoginResponse avec infos user + patientId + token JWT
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 1. Vérifier si email existe déjà (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User avec password hashé
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash BCrypt
        user.setRole(User.Role.PATIENT); // Inscription = toujours PATIENT
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        // Sauvegarder User
        User savedUser = userRepository.save(user);

        // 3. Créer Patient lié au User (OneToOne)
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setCreatedAt(LocalDateTime.now());

        // Sauvegarder Patient
        Patient savedPatient = patientRepository.save(patient);

        // ⭐ 4. NOUVEAU : Générer token JWT
        String token = jwtUtil.generateToken(
                savedUser.getEmail(), // Subject (email unique)
                savedUser.getRole().name() // Claim "role" (PATIENT)
        );

        // 5. Retourner LoginResponse avec token
        return LoginResponse.builder()
                .token(token) // ⭐ Token JWT réel
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().name())
                .patientId(savedPatient.getId())
                .doctorId(null) // Pas de doctorId pour un patient
                .build();
    }

    /**
     * Connexion (login) pour tous les utilisateurs (UC-P02, UC-D01, UC-A01) ⭐
     * MODIFIÉ AVEC JWT
     *
     * FLOW:
     * 1. Chercher User par email
     * 2. Vérifier password (BCrypt)
     * 3. Mettre à jour lastLoginAt
     * 4. Charger Patient ou Doctor si applicable
     * 5. Générer token JWT
     * 6. Retourner LoginResponse avec token
     *
     * SÉCURITÉ:
     * - Vérification BCrypt du password
     * - Message d'erreur générique (sécurité)
     * - Token JWT signé et valide 1h
     *
     * @param request LoginRequest (email, password)
     * @return LoginResponse avec infos user + patientId/doctorId + token JWT
     * @throws InvalidCredentialsException si email ou password incorrect
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Chercher User par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());

        // 2. Vérifier password avec BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // 3. Mettre à jour lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Charger Patient ou Doctor selon le rôle
        Long patientId = null;
        Long doctorId = null;

        switch (user.getRole()) {
            case PATIENT:
                // Chercher Patient lié à ce User
                patientId = patientRepository.findByUser(user)
                        .map(Patient::getId)
                        .orElse(null);
                break;

            case DOCTOR:
                // ✅ IMPLÉMENTÉ : Chercher Doctor lié à ce User
                doctorId = doctorRepository.findByUser(user)
                        .map(Doctor::getId)
                        .orElse(null);
                break;

            case ADMIN:
                // Admin n'a ni patientId ni doctorId
                break;
        }

        // ⭐ 5. NOUVEAU : Générer token JWT
        String token = jwtUtil.generateToken(
                user.getEmail(), // Subject (email unique)
                user.getRole().name() // Claim "role" (PATIENT, DOCTOR, ADMIN)
        );

        // 6. Retourner LoginResponse avec token
        return LoginResponse.builder()
                .token(token) // ⭐ Token JWT réel
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .patientId(patientId)
                .doctorId(doctorId)
                .build();
    }

    /**
     * Vérifier si un email existe déjà
     *
     * UTILISATION:
     * Peut être utilisé par le frontend pour validation en temps réel
     *
     * @param email Email à vérifier
     * @return true si email existe, false sinon
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}