package com.cabinet.medical.service;

import com.cabinet.medical.dto.request.CreateUserRequest;
import com.cabinet.medical.dto.request.UpdateUserRequest;
import com.cabinet.medical.dto.response.UserResponse;
import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.TimeSlot;
import com.cabinet.medical.entity.User;
import com.cabinet.medical.exception.EmailAlreadyExistsException;
import com.cabinet.medical.exception.IllegalOperationException;
import com.cabinet.medical.exception.ResourceNotFoundException;
import com.cabinet.medical.repository.AppointmentRepository;
import com.cabinet.medical.repository.DoctorRepository;
import com.cabinet.medical.repository.PatientRepository;
import com.cabinet.medical.repository.TimeSlotRepository;
import com.cabinet.medical.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService - Service de gestion des utilisateurs (ADMIN uniquement)
 *
 * RESPONSABILITÉS:
 * - CRUD complet des utilisateurs (Admin uniquement)
 * - Création de patients avec entité Patient automatique
 * - Création de médecins avec entité Doctor automatique
 * - Modification des utilisateurs
 * - Suppression des utilisateurs
 * - Conversion Entity ↔ DTO
 *
 * USE CASES:
 * - UC-A03: Admin liste tous les utilisateurs
 * - UC-A04: Admin crée un patient
 * - UC-A05: Admin crée un médecin
 * - UC-A06: Admin modifie un utilisateur
 * - UC-A07: Admin supprime un utilisateur
 *
 * PERMISSIONS:
 * - Toutes les méthodes réservées aux ADMIN
 * - Vérification faite dans les Controllers (@PreAuthorize)
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param userRepository        Repository User
     * @param patientRepository     Repository Patient
     * @param doctorRepository      Repository Doctor
     * @param timeSlotRepository    Repository TimeSlot
     * @param appointmentRepository Repository Appointment
     */
    public UserService(UserRepository userRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TimeSlotRepository timeSlotRepository,
            AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Lister tous les utilisateurs (UC-A03)
     *
     * RETOURNE:
     * Liste de TOUS les utilisateurs (patients, doctors, admins)
     *
     * @return List<UserResponse>
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserResponse response = UserResponse.from(user);
                    // Ajouter specialty si c'est un médecin
                    if (user.getRole() == User.Role.DOCTOR) {
                        Doctor doctor = doctorRepository.findByUser(user).orElse(null);
                        if (doctor != null) {
                            response.setSpecialty(doctor.getSpecialty());
                        }
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lister les utilisateurs par rôle
     *
     * UTILISATION:
     * - Filtrer seulement les patients
     * - Filtrer seulement les doctors
     * - Filtrer seulement les admins
     *
     * @param role Rôle à filtrer
     * @return List<UserResponse>
     */
    public List<UserResponse> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(user -> {
                    UserResponse response = UserResponse.from(user);
                    // Ajouter specialty si c'est un médecin
                    if (user.getRole() == User.Role.DOCTOR) {
                        Doctor doctor = doctorRepository.findByUser(user).orElse(null);
                        if (doctor != null) {
                            response.setSpecialty(doctor.getSpecialty());
                        }
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtenir un utilisateur par ID
     *
     * @param userId ID de l'utilisateur
     * @return UserResponse
     * @throws ResourceNotFoundException si user non trouvé
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        UserResponse response = UserResponse.from(user);

        // Ajouter specialty si c'est un médecin
        if (user.getRole() == User.Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                response.setSpecialty(doctor.getSpecialty());
            }
        }

        return response;
    }

    /**
     * Créer un patient (UC-A04)
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=PATIENT, password hashé)
     * 3. Créer Patient (lié au User)
     * 4. Retourner UserResponse
     *
     * @param request CreateUserRequest (sans specialty)
     * @return UserResponse
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public UserResponse createPatient(CreateUserRequest request) {
        // 1. Vérifier email unique (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.PATIENT);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 3. Créer Patient
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setCreatedAt(LocalDateTime.now());

        patientRepository.save(patient);

        // 4. Retourner UserResponse
        return UserResponse.from(savedUser);
    }

    /**
     * Créer un médecin (UC-A05)
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=DOCTOR, password hashé)
     * 3. Créer Doctor (lié au User, avec specialty)
     * 4. Retourner UserResponse
     *
     * @param request CreateUserRequest (avec specialty)
     * @return UserResponse
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public UserResponse createDoctor(CreateUserRequest request) {
        // 1. Vérifier email unique (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.DOCTOR);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 3. Créer Doctor avec specialty
        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecialty(request.getSpecialty()); // Peut être null
        doctor.setCreatedAt(LocalDateTime.now());

        doctorRepository.save(doctor);

        // 4. Retourner UserResponse
        return UserResponse.from(savedUser);
    }

    /**
     * Créer un admin
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=ADMIN, password hashé)
     * 3. Retourner UserResponse
     *
     * NOTE:
     * Admin n'a pas d'entité Patient ou Doctor associée
     *
     * @param request CreateUserRequest (sans specialty)
     * @return UserResponse
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public UserResponse createAdmin(CreateUserRequest request) {
        // 1. Vérifier email unique (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.ADMIN);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 3. Retourner UserResponse
        return UserResponse.from(savedUser);
    }

    /**
     * Modifier un utilisateur (UC-A06)
     *
     * FLOW:
     * 1. Charger User existant
     * 2. Vérifier nouvel email unique si changé (RG-01)
     * 3. Mettre à jour champs (sauf password si non fourni)
     * 4. Sauvegarder
     * 5. Retourner UserResponse
     *
     * RÈGLES:
     * - Email unique si changé
     * - Password hashé si fourni (sinon gardé)
     * - Role ne peut pas être changé (pour simplicité)
     *
     * @param userId  ID de l'utilisateur
     * @param request CreateUserRequest avec nouveaux champs
     * @return UserResponse
     * @throws ResourceNotFoundException   si user non trouvé
     * @throws EmailAlreadyExistsException si nouvel email existe déjà
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        // 1. Charger User existant
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // 🛡️ SÉCURITÉ: Empêcher modification d'un admin
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalOperationException(
                    "Vous ne pouvez pas modifier un administrateur");
        }

        // 2. Vérifier nouvel email unique si changé (RG-01)
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // 3. Mettre à jour champs
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        // 4. Mettre à jour specialty si c'est un médecin
        String updatedSpecialty = null;
        if (user.getRole() == User.Role.DOCTOR && request.getSpecialty() != null) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", "userId", userId));
            doctor.setSpecialty(request.getSpecialty());
            doctorRepository.save(doctor);
            updatedSpecialty = doctor.getSpecialty();
        }

        // 5. Mettre à jour password SEULEMENT si fourni ET non vide
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            // Valider longueur minimum (6 caractères)
            if (request.getPassword().length() < 6) {
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // 6. Sauvegarder
        User updatedUser = userRepository.save(user);

        // 7. Retourner UserResponse avec specialty pour les médecins
        UserResponse response = UserResponse.from(updatedUser);
        response.setSpecialty(updatedSpecialty);
        return response;
    }

    /**
     * Supprimer un utilisateur (UC-A07)
     *
     * FLOW:
     * 1. Vérifier User existe
     * 2. Supprimer entités liées (TimeSlots, Appointments, Patient/Doctor)
     * 3. Supprimer User
     *
     * NOTE:
     * - Pour les médecins: Supprimer d'abord les TimeSlots pour éviter FK violation
     * - Les RDV et autres entités seront supprimés en cascade
     * - Attention: Perte de données définitive
     *
     * @param userId ID de l'utilisateur
     * @throws ResourceNotFoundException si user non trouvé
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 1. Vérifier User existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // 🛡️ SÉCURITÉ: Empêcher suppression d'un admin
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalOperationException(
                    "Vous ne pouvez pas supprimer un administrateur");
        }

        // 2. Pour les médecins: Supprimer les entités liées (FK constraints)
        if (user.getRole() == User.Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                // 2a. Supprimer les Appointments du médecin
                List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);
                appointmentRepository.deleteAll(appointments);

                // 2b. Supprimer les TimeSlots
                List<TimeSlot> timeSlots = timeSlotRepository.findByDoctor(doctor);
                timeSlotRepository.deleteAll(timeSlots);
            }
        }

        // 3. Supprimer User (cascade pour Patient/Doctor)
        userRepository.delete(user);
    }

    /**
     * Vérifier si un email existe
     *
     * UTILISATION:
     * Validation frontend en temps réel
     *
     * @param email Email à vérifier
     * @return true si existe, false sinon
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
