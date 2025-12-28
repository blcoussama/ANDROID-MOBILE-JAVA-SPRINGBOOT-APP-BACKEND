package com.cabinet.medical.service;

import com.cabinet.medical.dto.request.LoginRequest;
import com.cabinet.medical.dto.request.RegisterRequest;
import com.cabinet.medical.dto.response.AuthResponse;
import com.cabinet.medical.entity.User;
import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.repository.UserRepository;
import com.cabinet.medical.repository.PatientRepository;
import com.cabinet.medical.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Service AuthService
 * Gestion authentification: login, register, JWT
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // Injection dépendances (via @RequiredArgsConstructor)
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // À créer prochainement

    // ═══════════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════════

    /**
     * Authentifier un utilisateur
     *
     * @param request LoginRequest (email + password)
     * @return AuthResponse avec JWT token
     * @throws RuntimeException si credentials invalides
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Chercher user par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // 2. Vérifier compte actif
        if (!user.getIsActive()) {
            throw new RuntimeException("Compte désactivé. Contactez l'administrateur.");
        }

        // 3. Vérifier mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // 4. Mettre à jour lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Générer JWT token
        String token = jwtService.generateToken(user);

        // 6. Construire réponse
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    // REGISTER
    // ═══════════════════════════════════════════════════════════

    /**
     * Inscrire un nouvel utilisateur
     * Crée User + Patient ou Doctor selon le rôle
     *
     * @param request RegisterRequest
     * @return AuthResponse avec JWT token
     * @throws RuntimeException si validation échoue
     */
    @Transactional // Transaction pour User + Patient/Doctor
    public AuthResponse register(RegisterRequest request) {
        // 1. Vérifier que les mots de passe correspondent
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        // 2. Vérifier que l'email n'existe pas déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // 3. Créer User
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // 4. Créer Patient ou Doctor selon le rôle
        if (request.getRole() == User.Role.PATIENT) {
            createPatientProfile(user);
        } else if (request.getRole() == User.Role.DOCTOR) {
            createDoctorProfile(user);
        }
        // ADMIN n'a pas de profil supplémentaire

        // 5. Générer JWT token
        String token = jwtService.generateToken(user);

        // 6. Construire réponse
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer profil Patient (après création User)
     * Tous les champs médicaux sont optionnels (complétés plus tard)
     */
    private void createPatientProfile(User user) {
        Patient patient = Patient.builder()
                .user(user)
                .dateOfBirth(null) // Optionnel
                .address(null) // Optionnel
                .medicalHistory(null) // Optionnel
                .build();

        patientRepository.save(patient);
    }

    /**
     * Créer profil Doctor (après création User)
     * Tous les champs professionnels sont optionnels (complétés plus tard)
     * Note: licenseNumber devrait être renseigné rapidement (validation métier)
     */
    private void createDoctorProfile(User user) {
        Doctor doctor = Doctor.builder()
                .user(user)
                .specialty(null) // À renseigner
                .licenseNumber(null) // À renseigner (CRITIQUE)
                .officeAddress(null) // Optionnel
                .consultationFee(null) // Optionnel
                .bio(null) // Optionnel
                .yearsExperience(null) // Optionnel
                .build();

        doctorRepository.save(doctor);
    }
}
