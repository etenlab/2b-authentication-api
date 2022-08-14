package org.eten.authentication.common

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: ErrorType,
)

@Serializable
enum class ErrorType {
  AvatarUnavailable,
  AvatarNotFound,
  AvatarTooShort,
  AvatarTooLong,
  EmailNotFound,
  EmailTooLong,
  EmailTooShort,
  EmailInvalid,
  EmailIsBlocked,
  EmailUnavailable,
  InvalidEmailOrPassword,
  NoError,
  PasswordTooLong,
  PasswordTooShort,
  PasswordInvalid,
  Unauthorized,
  UnknownError,
}