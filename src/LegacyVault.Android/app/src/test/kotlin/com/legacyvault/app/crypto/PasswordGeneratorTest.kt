package com.legacyvault.app.crypto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PasswordGeneratorTest {

    private val generator = PasswordGenerator()

    // ── Character set ─────────────────────────────────────────────────────

    @Test
    fun `charset is exactly 88 characters`() {
        assertEquals(88, PasswordGenerator.CHARSET.length)
    }

    @Test
    fun `charset contains all expected character classes`() {
        val charset = PasswordGenerator.CHARSET
        assertTrue(charset.any { it.isLowerCase() },  "Must contain lowercase")
        assertTrue(charset.any { it.isUpperCase() },  "Must contain uppercase")
        assertTrue(charset.any { it.isDigit() },      "Must contain digits")
        assertTrue(charset.any { !it.isLetterOrDigit() }, "Must contain symbols")
    }

    @Test
    fun `rejection limit eliminates modulo bias`() {
        // limit = 256 - (256 % 88) = 256 - 80 = 176
        assertEquals(176, PasswordGenerator.REJECTION_LIMIT)
    }

    // ── generate ──────────────────────────────────────────────────────────

    @Test
    fun `generate returns default length of 20`() {
        val pw = generator.generate()
        assertEquals(20, pw.length)
    }

    @Test
    fun `generate returns specified length`() {
        assertEquals(8,  generator.generate(8).length)
        assertEquals(32, generator.generate(32).length)
        assertEquals(64, generator.generate(64).length)
    }

    @Test
    fun `generate uses only charset characters`() {
        repeat(10) {
            val pw = generator.generate(50)
            for (ch in pw) {
                assertTrue(PasswordGenerator.CHARSET.contains(ch),
                    "Unexpected character '$ch' (code ${ch.code}) in password")
            }
        }
    }

    @Test
    fun `generate produces different passwords each call`() {
        val passwords = (1..20).map { generator.generate() }.toSet()
        assertTrue(passwords.size > 1, "Passwords should not all be identical")
    }

    @Test
    fun `generate throws on non-positive length`() {
        assertThrows<IllegalArgumentException> { generator.generate(0) }
        assertThrows<IllegalArgumentException> { generator.generate(-1) }
    }

    // ── strength ─────────────────────────────────────────────────────────

    @Test
    fun `empty password has score 0 and empty label`() {
        val s = generator.strength("")
        assertEquals(0, s.score)
        assertEquals("", s.label)
        assertEquals(PasswordGenerator.Level.Weak, s.level)
    }

    @Test
    fun `short lowercase-only is Weak`() {
        val s = generator.strength("abc")
        assertEquals(PasswordGenerator.Level.Weak, s.level)
    }

    @Test
    fun `generated password scores Very strong`() {
        // A generated password is always 20 chars with mixed charset — should be Very strong
        repeat(5) {
            val pw = generator.generate()
            val s = generator.strength(pw)
            assertEquals(PasswordGenerator.Level.VeryStrong, s.level,
                "Expected very-strong for generated password: $pw")
        }
    }

    @Test
    fun `strength score matches TypeScript algorithm exactly`() {
        // Known inputs → known scores (ported from PasswordEditor.tsx)
        // "Password1!" → length>=8(+20), length<16, lower(+10), upper(+10), digit(+10), symbol(+20) = 70 → Strong
        val s = generator.strength("Password1!")
        assertEquals(70, s.score)
        assertEquals(PasswordGenerator.Level.Strong, s.level)
        assertEquals("Strong", s.label)
    }

    @Test
    fun `Very strong threshold is 80`() {
        // "Password1!AAAAAAA" = 17 chars: >=8(+20), >=16(+20), lower(+10), upper(+10), digit(+10), symbol(+20) = 90
        val s = generator.strength("Password1!AAAAAAA")
        assertEquals(PasswordGenerator.Level.VeryStrong, s.level)
    }
}
