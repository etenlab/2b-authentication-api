package org.eten.authentication.component

//import com.amazonaws.auth.AWSStaticCredentialsProvider
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Regions
//import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
//import com.amazonaws.services.simpleemail.model.*
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
import org.springframework.web.bind.annotation.*
import javax.sql.DataSource

@RestController
@DependsOn("DatabaseSync")
class Register(
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
//  val awsCreds = BasicAWSCredentials(app_config.awsAccessKeyId, app_config.awsSecretAccessKey)
//  val sesClient = AmazonSimpleEmailServiceClientBuilder.standard()
//    .withCredentials(AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.US_EAST_2).build()

  @PostMapping("/api/authentication/register")
  @ResponseBody
  fun register(@RequestBody request: RegisterRequest): RegisterResponse {
    try {
      if (request.email.length > 255) return RegisterResponse(ErrorType.EmailTooLong)
      if (request.email.length <= 4) return RegisterResponse(ErrorType.EmailTooShort)
      if (!util.isEmailValid(request.email)) return RegisterResponse(ErrorType.EmailInvalid)
      if (request.avatar.isEmpty()) return RegisterResponse(ErrorType.AvatarTooShort)
      if (request.avatar.length > 64) return RegisterResponse(ErrorType.AvatarTooLong)
      if (request.password.length < 8) return RegisterResponse(ErrorType.PasswordTooShort)
      if (request.password.length > 32) return RegisterResponse(ErrorType.PasswordTooLong)

      var errorType = util.isEmailSendable(request.email)

      if (errorType == ErrorType.NoError) {
        val user_token = util.create_jwt()
        val deny_token = util.random_string(64)
        val confirm_token = util.random_string(64)

        val pash = encoder.encode(request.password)

        var errorType = ErrorType.UnknownError
        var avatar: String? = null
        var avatar_id: Long? = null

        try {

          //language=SQL
          val result = writer_jdbc.queryForRowSet("""
          call authentication_register(:email, :avatar, :password, :token, 0, '');
        """.trimIndent(), MapSqlParameterSource()
              .addValue("email", request.email)
              .addValue("avatar", request.avatar)
              .addValue("password", pash)
              .addValue("token", user_token)
          )

          if (result.next()) {
            var error: String? = result.getString("p_error_type")
            if (result.wasNull()) error = null

            if (error == null) {
              return RegisterResponse(ErrorType.UnknownError)
            }

            val error_type = ErrorType.valueOf(error)
            if (error_type != ErrorType.NoError) {
              return RegisterResponse(error_type)
            }

            var user_id: Long? = result.getLong("p_user_id")
            if (result.wasNull()) user_id = null
            if (user_id == null) {
              return RegisterResponse(ErrorType.UnknownError)
            }

            return RegisterResponse(
                error = ErrorType.NoError,
                user_id = user_id,
                token = user_token,
            )
          }

        } catch (e: IllegalArgumentException) {
          kafka.send(KafkaTopics.Error, e.localizedMessage + '\n' + e.stackTrace
              .map { it.toString() }
              .reduce { acc, s -> acc + '\n' + s })
          errorType = ErrorType.UnknownError
        }

      }

    } catch (e: Exception) {
      kafka.send(KafkaTopics.Error, e.localizedMessage + '\n' + e.stackTrace
          .map { it.toString() }
          .reduce { acc, s -> acc + '\n' + s })
    }

    return RegisterResponse(ErrorType.UnknownError)
  }

//  fun sendRegistrationEmail(email: String, emailToken: String, rejectToken: String) {
//
//    val emailFrom = "no-reply@eten.bible"
//    val emailSubject = "Please Confirm Email Address"
//
//    val emailHtmlBody = """
//      <h1>Hello!</h1>
//      <p>To CONFIRM your email address, click <a href="${app_config.emailServer}/email/${emailToken}">here</a>.</p>
//      <p>If you didn't request this email address to be added to this username, please click <a href="${app_config.emailServer}/email/${rejectToken}">this</a> link and we will BLOCK your address from receiving any more emails.</p>
//    """.trimIndent()
//
//    val emailTextBody = """
//      Hello!
//
//      To CONFIRM your email address, click this link:
//
//      ${app_config.emailServer}/email/${emailToken}
//
//      If you didn't request this email address to be added to this username, please click this link to REJECT:
//
//      ${app_config.emailServer}/email/${rejectToken}
//
//      and we will block your address from receiving any more emails..
//
//    """.trimIndent()
//
//    val emailRequest = SendEmailRequest()
//      .withDestination(Destination().withToAddresses(email))
//      .withMessage(
//        Message()
//          .withBody(
//            Body()
//              .withHtml(
//                Content().withCharset("UTF-8")
//                  .withData(emailHtmlBody)
//              )
//              .withText(
//                Content().withCharset("UTF-8")
//                  .withData(emailTextBody)
//              )
//          )
//          .withSubject(
//            Content().withCharset("UTF-8").withData(emailSubject)
//          )
//      )
//      .withSource(emailFrom)
//
//    try {
//
//      val result = sesClient.sendEmail(emailRequest)
//
//      this.writer_jdbc.jdbcTemplate.dataSource!!.connection.use { conn ->
//        //language=SQL
//        val statement = conn.prepareStatement(
//          """
//          insert into emails_sent("email", "message_id", "type") values (?, ?, 'Register');
//        """.trimIndent()
//        )
//
//        statement.setString(1, email)
//        statement.setString(2, result.messageId)
//
//        statement.execute()
//      }
//
//    } catch (e: Exception) {
//      kafka.send(KafkaTopics.Error, "SES Exception: ${e.localizedMessage}")
//    }
//  }
}