package com.cabinet.medical.service;

import com.cabinet.medical.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service JwtService
 * Génération et validation tokens JWT
 *
 * JWT = JSON Web Token
 * Format: header.payload.signature
 * Ex:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
 */
@Service
public class JwtService {

    /**
     * Clé secrète pour signer les tokens
     * DOIT être dans application.properties (pas en dur!)
     * Longueur minimum: 256 bits (32 caractères)
     */
    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong1234567890}")
    private String secretKey;

    /**
     * Durée de validité du token (en millisecondes)
     * Par défaut: 24 heures (86400000 ms)
     */
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    // ═══════════════════════════════════════════════════════════
    // GÉNÉRATION TOKEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Générer un token JWT pour un utilisateur
     *
     * @param user User entity
     * @return JWT token (String)
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        // Claims personnalisés (payload du JWT)
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().toString());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        return createToken(claims, user.getEmail());
    }

    /**
     * Créer le token JWT
     *
     * @param claims  Données à encoder dans le token
     * @param subject Subject du token (généralement email)
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims) // Payload personnalisé
                .setSubject(subject) // Subject (email)
                .setIssuedAt(now) // Date émission
                .setExpiration(expiryDate) // Date expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signature
                .compact(); // Génère le token final
    }

    // ═══════════════════════════════════════════════════════════
    // VALIDATION TOKEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Extraire l'email (subject) du token
     *
     * @param token JWT token
     * @return Email de l'utilisateur
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extraire l'userId du token
     *
     * @param token JWT token
     * @return ID de l'utilisateur
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extraire le rôle du token
     *
     * @param token JWT token
     * @return Rôle (String)
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extraire la date d'expiration du token
     *
     * @param token JWT token
     * @return Date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extraire un claim spécifique du token
     *
     * @param token          JWT token
     * @param claimsResolver Function pour extraire le claim
     * @return Claim extrait
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extraire tous les claims du token
     *
     * @param token JWT token
     * @return Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Vérifier si le token est expiré
     *
     * @param token JWT token
     * @return true si expiré, false sinon
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valider le token
     * Vérifie que le token est valide pour l'utilisateur donné
     *
     * @param token JWT token
     * @param user  User entity
     * @return true si valide, false sinon
     */
    public Boolean validateToken(String token, User user) {
        final String email = extractEmail(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    // ═══════════════════════════════════════════════════════════
    // CLÉS DE SIGNATURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir la clé de signature
     * Convertit la secretKey (String) en Key
     *
     * @return Clé de signature
     */
    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
