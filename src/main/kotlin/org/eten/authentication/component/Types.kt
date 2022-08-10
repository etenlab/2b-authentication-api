package org.eten.authentication.component

import kotlinx.serialization.Serializable
import org.eten.authentication.common.ErrorType

// Register
@Serializable
data class UserRegisterRequest(
    val email: String,
    val avatar: String,
    val password: String,
)

@Serializable
data class UserRegisterResponse(
    val error: ErrorType,
    val user_id: Long? = null,
    val token: String? = null,
)