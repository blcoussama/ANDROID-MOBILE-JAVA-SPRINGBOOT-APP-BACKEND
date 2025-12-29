package com.cabinet.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiResponse - Réponse standard de l'API
 * 
 * FORMAT UNIFORME:
 * {
 * "success": true/false,
 * "message": "Message descriptif",
 * "data": {...} ou null
 * }
 * 
 * UTILISATIONS:
 * - Succès avec données: ApiResponse<UserResponse>
 * - Succès sans données: ApiResponse<Void>
 * - Erreurs: GlobalExceptionHandler
 * - Validation: ApiResponse<Map<String, String>>
 * 
 * @param <T> Type de données retournées
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    /**
     * Succès ou échec de la requête
     * - true : Opération réussie (200, 201, 204)
     * - false : Erreur (400, 401, 404, 409, 500)
     */
    private boolean success;

    /**
     * Message descriptif
     * 
     * EXEMPLES:
     * - "Utilisateur créé avec succès"
     * - "Email déjà utilisé"
     * - "Rendez-vous non trouvé"
     */
    private String message;

    /**
     * Données retournées (ou null si erreur)
     * 
     * TYPES POSSIBLES:
     * - UserResponse, DoctorResponse, etc.
     * - List<AppointmentResponse>
     * - Map<String, String> (erreurs validation)
     * - Void (pas de données)
     */
    private T data;

    /**
     * Helper pour créer une réponse de succès avec données
     * 
     * @param message Message de succès
     * @param data    Données à retourner
     * @return ApiResponse<T> avec success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Helper pour créer une réponse de succès sans données
     * 
     * @param message Message de succès
     * @return ApiResponse<Void> avec success=true et data=null
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Helper pour créer une réponse d'erreur
     * 
     * @param message Message d'erreur
     * @return ApiResponse<Void> avec success=false et data=null
     */
    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Helper pour créer une réponse d'erreur avec données
     * 
     * @param message Message d'erreur
     * @param data    Données d'erreur (ex: erreurs validation)
     * @return ApiResponse<T> avec success=false
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}