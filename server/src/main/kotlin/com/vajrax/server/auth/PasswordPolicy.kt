package com.vajrax.server.auth

import com.vajrax.contract.Limits

/**
 * Length-first rules (NIST SP 800-63B): 10–128 characters, not the email, not a well-known or
 * trivially patterned password. No forced mix of symbols, which only makes passwords harder to remember.
 */
object PasswordPolicy {
    private val common: Set<String> by lazy {
        PasswordPolicy::class.java.getResourceAsStream("/common-passwords.txt")
            ?.bufferedReader()?.useLines { lines -> lines.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet() }
            .orEmpty()
    }

    /** A sentence for the user, or null when [password] is acceptable. */
    fun problem(password: String, email: String): String? {
        val lower = password.lowercase()
        val local = email.substringBefore('@').lowercase()
        return when {
            password.length < Limits.PASSWORD_MIN -> "Use at least ${Limits.PASSWORD_MIN} characters."
            password.length > Limits.PASSWORD_MAX -> "Use at most ${Limits.PASSWORD_MAX} characters."
            password.isBlank() -> "Use at least ${Limits.PASSWORD_MIN} characters."
            password.toSet().size < MIN_DISTINCT -> "Use a less repetitive password."
            lower == email.lowercase() || (local.length >= MIN_LOCAL && lower.contains(local)) -> "Don't use your email in the password."
            lower in common || isSequence(lower) -> "That password is too common. Try a short phrase you'll remember."
            else -> null
        }
    }

    private fun isSequence(text: String): Boolean {
        val steps = text.zipWithNext { a, b -> b - a }.toSet()
        return steps.size == 1 && steps.first() in setOf(1, -1)
    }

    private const val MIN_DISTINCT = 4
    private const val MIN_LOCAL = 4
}
