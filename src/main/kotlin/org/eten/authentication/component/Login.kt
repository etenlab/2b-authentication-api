package org.eten.authentication.component

import kotlinx.serialization.json.Json
import org.eten.authentication.core.AppConfig
import org.eten.authentication.common.ErrorType
import org.eten.authentication.common.Utility
import org.eten.authentication.core.KafkaService
import org.eten.authentication.core.KafkaTopics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.DependsOn
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

@RestController
@DependsOn("DatabaseSync")
class Login(
    @Autowired
    val app_config: AppConfig,

    @Autowired
    @Qualifier("readerDataSource")
    val writer_ds: DataSource,

    @Autowired
    @Qualifier("readerDataSource")
    val reader_ds: DataSource,

    @Autowired val util: Utility,

    @Autowired val kafka: KafkaService,
) {
  val writer_jdbc = NamedParameterJdbcTemplate(writer_ds)
  val reader_jdbc = NamedParameterJdbcTemplate(reader_ds)
  val mapper = Json { ignoreUnknownKeys = true; encodeDefaults = true }
  val encoder = Argon2PasswordEncoder(16, 32, 1, 4096, 3)

  @PostMapping("/api/authentication/login")
  @ResponseBody
  fun login(@RequestBody request: LoginRequest): LoginResponse {
    try {
      if (!util.isEmailValid(request.email)) return LoginResponse(ErrorType.EmailInvalid)
      if (request.email.length > 255) return LoginResponse(ErrorType.EmailTooLong)
      if (request.email.length <= 4) return LoginResponse(ErrorType.EmailTooShort)
      if (request.password.length < 8) return LoginResponse(ErrorType.PasswordTooShort)
      if (request.password.length > 32) return LoginResponse(ErrorType.PasswordTooLong)

      //language=SQL
      val result = writer_jdbc.queryForRowSet("""
            select user_id, password
            from users
            where email = :email
                and active = true;
        """.trimIndent(), MapSqlParameterSource()
          .addValue("email", request.email)
      )

      var user_id: Long? = null
      var pash: String? = null

      if (result.next()) {
        pash = result.getString("password")
        if (result.wasNull()) pash = null

        user_id = result.getLong("user_id")
        if (result.wasNull()) user_id = null
      }

      if (pash == null || user_id == null) return LoginResponse(ErrorType.EmailNotFound)
      val matches = encoder.matches(request.password, pash)
      if (!matches) return LoginResponse(ErrorType.InvalidEmailOrPassword)

      //language=SQL
      val avatar_result = reader_jdbc.queryForRowSet("""
        select avatar, url
        from avatars
        where user_id = :user_id
      """.trimIndent(),
          MapSqlParameterSource()
              .addValue("user_id", user_id)
      )

      var avatar: String? = null
      var url: String? = null

      if (avatar_result.next()) {
        avatar = avatar_result.getString("avatar")
        if (avatar_result.wasNull()) avatar = null

        url = avatar_result.getString("url")
        if (avatar_result.wasNull()) url = null

      } else {
        return LoginResponse(ErrorType.AvatarNotFound)
      }

      if (avatar == null) return LoginResponse(ErrorType.AvatarNotFound)

      val token = util.create_jwt()

      return LoginResponse(
          error = ErrorType.NoError,
          avatar = avatar,
          url = url,
          token = token,
      )
    } catch (e: Exception) {
      kafka.send(KafkaTopics.Error, e.localizedMessage + '\n' + e.stackTrace
          .map { it.toString() }
          .reduce { acc, s -> acc + '\n' + s })
    }

    return LoginResponse(ErrorType.UnknownError)
  }
}