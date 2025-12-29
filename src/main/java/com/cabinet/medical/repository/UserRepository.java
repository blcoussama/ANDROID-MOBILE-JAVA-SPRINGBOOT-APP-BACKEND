package com.cabinet.medical.repository;

import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Interface pour gérer l'accès aux données des utilisateurs
 *
 * RESPONSABILITÉS:
 * - CRUD sur la table users
 * - Recherche par email (login)
 * - Recherche par rôle (PATIENT, DOCTOR, ADMIN)
 * - Utilisé par AuthService, UserService, AdminController
 *
 * MÉTHODES GRATUITES (JpaRepository):
 * - save(user) : Créer/Modifier utilisateur
 * - findById(id) : Trouver par ID
 * - findAll() : Liste tous les users
 * - deleteById(id) : Supprimer par ID
 * - count() : Compter users
 * - existsById(id) : Vérifier existence
 *
 * MÉTHODES CUSTOM (générées automatiquement par Spring):
 * - findByEmail(email) : Login - UC-P02, UC-D01, UC-A01
 * - findByRole(role) : Liste par rôle - UC-A03
 * - existsByEmail(email) : Vérifier email unique - UC-P01
 */
@Repository // Marque cette interface comme un bean Spring
public interface UserRepository extends JpaRepository<User, Long> {

    // ═══════════════════════════════════════════════════════════
    // AUTHENTICATION (Login)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un utilisateur par son email
     * Utilisé pour le login (UC-P02, UC-D01, UC-A01)
     *
     * SQL généré: SELECT * FROM users WHERE email = ?
     *
     * @param email L'email de l'utilisateur
     * @return Optional<User> (vide si non trouvé)
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà en base
     * Utilisé pour valider l'unicité lors de l'inscription (UC-P01)
     *
     * SQL généré: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     *
     * @param email L'email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);

    // ═══════════════════════════════════════════════════════════
    // GESTION ADMIN
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve tous les utilisateurs d'un rôle spécifique
     * Utilisé par Admin pour lister patients, doctors ou admins (UC-A03)
     *
     * SQL généré: SELECT * FROM users WHERE role = ?
     *
     * @param role Le rôle à filtrer (PATIENT, DOCTOR, ADMIN)
     * @return Liste des users avec ce rôle
     */
    List<User> findByRole(User.Role role);
}
