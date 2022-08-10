package org.eten.authentication.common

import kotlinx.serialization.Serializable


@Serializable
data class ErrorResponse(
  val error: ErrorType,
)

@Serializable
enum class ErrorType {
  EmailNotFound,
  EmailTooLong,
  EmailTooShort,
  EmailInvalid,
  EmailIsBlocked,
  NoError,
  PasswordTooLong,
  PasswordTooShort,
  PasswordInvalid,
  Unauthorized,
  UnknownError,
}