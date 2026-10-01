package com.vajrax.domain.usecase

/** Validation for the on-device profile, shared by the edit dialog and the save path. */
object ProfileRules {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    /** Email is optional; when given it must look like an address. */
    fun emailError(email: String): String? {
        val e = email.trim()
        return if (e.isNotEmpty() && !EMAIL.matches(e)) "Enter a valid email or leave it empty." else null
    }
}
