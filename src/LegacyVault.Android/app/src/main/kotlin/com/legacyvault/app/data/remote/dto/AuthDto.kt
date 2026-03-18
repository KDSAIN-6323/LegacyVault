package com.legacyvault.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class ResetPasswordRequest(
    val username: String,
    val email: String,
    val newPassword: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String
)

@Serializable
data class HealthResponse(
    val status: String = "ok"
)
