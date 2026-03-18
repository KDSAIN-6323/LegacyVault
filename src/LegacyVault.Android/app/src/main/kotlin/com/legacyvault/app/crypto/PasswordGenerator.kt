package com.legacyvault.app.crypto

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cryptographically secure password generator.
 *
 * Ports the rejection-sampling algorithm from PasswordEditor.tsx exactly,
 * using the same character set so that generated passwords have identical
 * entropy characteristics on all clients.
 *
 * Character set (88 chars):
 *   lower   : abcdefghijklmnopqrstuvwxyz         (26)
 *   upper   : ABCDEFGHIJKLMNOPQRSTUVWXYZ         (26)
 *   digits  : 0123456789                         (10)
 *   symbols : !@#$%^&*()-_=+[]{}|;:,.<>?        (26)
 *
 * Rejection sampling: bytes ≥ LIMIT (= 256 - 256%88 = 176) are discarded,
 * eliminating modulo bias so every character has equal probability.
 */
@Singleton
class PasswordGenerator @Inject constructor() {

    private val secureRandom = SecureRandom()

    companion object {
        private const val CHARSET_LOWER   = "abcdefghijklmnopqrstuvwxyz"
        private const val CHARSET_UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val CHARSET_DIGITS  = "0123456789"
        private const val CHARSET_SYMBOLS = "!@#\$%^&*()-_=+[]{}|;:,.<>?"

        val CHARSET: String = CHARSET_LOWER + CHARSET_UPPER + CHARSET_DIGITS + CHARSET_SYMBOLS

        // Largest multiple of CHARSET.length that fits in 0..255
        val REJECTION_LIMIT: Int = 256 - (256 % CHARSET.length)

        const val DEFAULT_LENGTH = 20
    }

    /**
     * Generate a random password of [length] characters drawn uniformly from [CHARSET].
     *
     * @param length Number of characters (default 20, matching the web client).
     * @return The generated password string.
     */
    fun generate(length: Int = DEFAULT_LENGTH): String {
        require(length > 0) { "Password length must be positive" }

        val result = StringBuilder(length)
        // Over-request bytes to reduce the number of refill rounds
        val bufferSize = length * 2

        while (result.length < length) {
            val bytes = ByteArray(bufferSize)
            secureRandom.nextBytes(bytes)
            for (b in bytes) {
                if (result.length >= length) break
                // Convert signed byte to unsigned int 0-255
                val unsigned = b.toInt() and 0xFF
                if (unsigned < REJECTION_LIMIT) {
                    result.append(CHARSET[unsigned % CHARSET.length])
                }
                // Bytes >= REJECTION_LIMIT are silently discarded (rejection sampling)
            }
        }

        return result.toString()
    }

    /**
     * Evaluate the strength of [password].
     * Mirrors getPasswordStrength() in PasswordEditor.tsx exactly.
     */
    fun strength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength(0, Level.Weak, "")

        var score = 0
        if (password.length >= 8)  score += 20
        if (password.length >= 16) score += 20
        if (password.length >= 20) score += 10
        if (password.any { it.isLowerCase() }) score += 10
        if (password.any { it.isUpperCase() }) score += 10
        if (password.any { it.isDigit() })     score += 10
        if (password.any { !it.isLetterOrDigit() }) score += 20

        return when {
            score < 40 -> PasswordStrength(score, Level.Weak,       "Weak")
            score < 60 -> PasswordStrength(score, Level.Fair,       "Fair")
            score < 80 -> PasswordStrength(score, Level.Strong,     "Strong")
            else       -> PasswordStrength(score, Level.VeryStrong, "Very strong")
        }
    }

    enum class Level { Weak, Fair, Strong, VeryStrong }

    data class PasswordStrength(
        val score: Int,     // 0–100
        val level: Level,
        val label: String
    )
}
