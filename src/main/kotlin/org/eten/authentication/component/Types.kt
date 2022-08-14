package org.eten.authentication.component

import kotlinx.serialization.Serializable
import org.eten.authentication.common.ErrorType

// Register
@Serializable
data class RegisterRequest(
    val email: String,
    val avatar: String,
    val password: String,
)

@Serializable
data class RegisterResponse(
    val error: ErrorType,
    val user_id: Long? = null,
    val token: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val error: ErrorType,
    val avatar: String? = null,
    val url: String? = null,
    val token: String? = null,
)

@Serializable
data class LogoutRequest(
    val token: String,
)

@Serializable
data class LogoutResponse(
    val error: ErrorType,
)