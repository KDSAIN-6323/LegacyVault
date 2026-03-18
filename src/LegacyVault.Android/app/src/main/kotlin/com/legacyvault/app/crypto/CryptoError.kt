package com.legacyvault.app.crypto

/**
 * Errors thrown by [CryptoService].
 */
sealed class CryptoError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The GCM authentication tag did not match — wrong password or corrupted data. */
    class AuthenticationFailed(cause: Throwable? = null) :
        CryptoError("Decryption failed: authentication tag mismatch. Wrong password?", cause)

    /** The ciphertext is too short to be valid (less than tag size). */
    class InvalidCiphertext(message: String) : CryptoError(message)

    /** Key derivation or cipher initialization failed for an unexpected reason. */
    class CryptoOperationFailed(message: String, cause: Throwable) : CryptoError(message, cause)
}
