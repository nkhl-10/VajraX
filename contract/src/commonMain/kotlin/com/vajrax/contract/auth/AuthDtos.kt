package com.vajrax.contract.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String = "",
    val deviceName: String? = null
)

@Serializable
data class LoginRequest(val email: String, val password: String, val deviceName: String? = null)

/** Mobile apps send the refresh token in the body; web clients rely on the HttpOnly cookie and omit it. */
@Serializable
data class RefreshRequest(val refreshToken: String? = null)

@Serializable
data class LogoutRequest(val refreshToken: String? = null)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val emailVerified: Boolean,
    val createdAt: String
)

@Serializable
data class AuthResponse(
    val user: UserDto,
    val accessToken: String,
    /** Seconds until [accessToken] expires. */
    val accessExpiresIn: Long,
    /** Absent for web clients (it is set as an HttpOnly cookie instead). */
    val refreshToken: String? = null,
    val refreshExpiresIn: Long
)

@Serializable
data class UpdateMeRequest(val displayName: String)

@Serializable
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val token: String, val newPassword: String)

@Serializable
data class DeleteAccountRequest(val password: String)
