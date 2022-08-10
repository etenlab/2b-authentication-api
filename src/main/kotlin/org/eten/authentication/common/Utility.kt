package org.eten.authentication.common

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.eten.authentication.AppConfig
import org.eten.authentication.core.KafkaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.sql.DataSource

@Component
class Utility(
    @Autowired
    val app_config: AppConfig,

    @Autowired
    @Qualifier("readerDataSource")
    val writer_ds: DataSource,

    @Autowired
    @Qualifier("readerDataSource")
    val reader_ds: DataSource,

    @Autowired
    val kafka: KafkaService,
) {
  val writer_jdbc = NamedParameterJdbcTemplate(writer_ds)
  val reader_jdbc = NamedParameterJdbcTemplate(reader_ds)
  val algo = Algorithm.HMAC256(app_config.jwt_secret)
  val verifier = JWT
      .require(algo)
      .withIssuer("etenlab")
      .build()

  fun create_jwt(pairs: List<Pair<String, String>>? = null): String {

    val now = LocalDate
        .now()
        .toString()

    val jwt = JWT
        .create()
        .withClaim("random", random_string(16))
        .withClaim("created_at", now)
        .withClaim("confirm", true)
        .withIssuer("etenlab")

    if (pairs != null) {
      for (pair in pairs) {
        jwt.withClaim(pair.first, pair.second)
      }
    }

    return jwt.sign(algo)
  }

  fun verify_jwt(jwt: String): DecodedJWT? {
    try {
      return verifier.verify(jwt)
    } catch (e: Exception) {
      return null
    }
  }

  fun random_string(length: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val token = (1..length)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
    return token
  }

  fun isEmailValid(email: String): Boolean {
    if (email.contains('@') && email.contains('.')) return true
    return false
  }

  fun isEmailSendable(email: String): ErrorType {

    this.writer_ds.connection.use { conn ->

      //language=SQL
      val statement = conn.prepareCall(
          """
        select email 
        from emails_sent 
        where email = ? 
          and (
               response = 'Bounce' 
            or response = 'Complaint'
          );
      """.trimIndent()
      )
      statement.setString(1, email)

      val result = statement.executeQuery()

      if (result.next()) {
        return ErrorType.EmailIsBlocked
      }

      //language=SQL
      val statement2 = conn.prepareCall(
          """
        select email from emails_blocked where email = ?;
      """.trimIndent()
      )
      statement.setString(1, email)

      val result2 = statement.executeQuery()

      if (result2.next()) {
        return ErrorType.EmailIsBlocked
      }
    }

    return ErrorType.NoError
  }
}