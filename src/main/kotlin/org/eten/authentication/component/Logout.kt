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
class Logout(
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

  @PostMapping("/api/authentication/logout")
  @ResponseBody
  fun logout(@RequestBody request: LogoutRequest): LogoutResponse {
    try {
      //language=SQL
      val result = writer_jdbc.update("""
            delete from tokens
            where token = :token
        """.trimIndent(), MapSqlParameterSource()
          .addValue("token", request.token)
      )

      return LogoutResponse(
          error = ErrorType.NoError,
      )
    } catch (e: Exception) {
      kafka.send(KafkaTopics.Error, e.localizedMessage + '\n' + e.stackTrace
          .map { it.toString() }
          .reduce { acc, s -> acc + '\n' + s })
    }

    return LogoutResponse(ErrorType.UnknownError)
  }
}