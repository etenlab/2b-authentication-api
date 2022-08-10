package org.eten.authentication.component

import kotlinx.serialization.Serializable

// Register
@Serializable
data class UserRegisterRequest (
  val email: String,
  val password: String,
)