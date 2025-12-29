package com.cabinet.medical.exception;

/**
 * IllegalOperationException - Exception levée pour opération non autorisée
 *
 * UTILISATION:
 * - Tentative de modifier un admin (UC-A06)
 * - Tentative de supprimer un admin (UC-A07)
 * - Toute opération interdite par règles métier
 *
 * HTTP STATUS:
 * - 403 FORBIDDEN
 *
 * EXEMPLE:
 * throw new IllegalOperationException("Vous ne pouvez pas modifier un
 * administrateur");
 * → Message: "Vous ne pouvez pas modifier un administrateur"
 */
public class IllegalOperationException extends RuntimeException {

    /**
     * Constructeur avec message
     *
     * @param message Message d'erreur descriptif
     */
    public IllegalOperationException(String message) {
        super(message);
    }
}