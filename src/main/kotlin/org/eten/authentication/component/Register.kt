package org.eten.authentication.component

//import com.amazonaws.auth.AWSStaticCredentialsProvider
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.regions.Regions
//import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
//import com.amazonaws.services.simpleemail.model.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eten.authentication.AppConfig
import org.eten.authentication.common.ErrorResponse
import org.eten.authentication.common.ErrorType
import org.eten.authentication.common.Utility
import org.eten.authentication.core.KafkaService
import org.eten.authentication.core.KafkaTopics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.DependsOn
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

  @Autowired
  val util: Utility,

  @Autowired
  val kafka: KafkaService,

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
  fun register(@RequestBody request_string: String): String {
    try {
      val request_parsed = mapper.decodeFromString<UserRegisterRequest>(request_string)

      if (request_parsed.email.length > 255) {
        return mapper.encodeToString(ErrorResponse(ErrorType.EmailTooLong))
      }

      if (request_parsed.email.length <= 4) {
        return mapper.encodeToString(ErrorResponse(ErrorType.EmailTooShort))
      }

      if (!util.isEmailValid(request_parsed.email)) {
        return mapper.encodeToString(ErrorResponse(ErrorType.EmailInvalid))
      }

      if (request_parsed.password.length < 8) {
        return mapper.encodeToString(ErrorResponse(ErrorType.PasswordTooShort))
      }

      if (request_parsed.password.length > 32) {
        return mapper.encodeToString(ErrorResponse(ErrorType.PasswordTooLong))
      }

      var errorType = util.isEmailSendable(request_parsed.email)

      if (errorType == ErrorType.NoError) {
        val deny_token = util.random_string(64)
        val confirm_token = util.random_string(64)

        val pash = encoder.encode(request_parsed.password)

        var errorType = ErrorType.UnknownError
        var avatar: String? = null
        var avatar_id: Long? = null

        // todo: write to db


        try {

        } catch (e: IllegalArgumentException) {
          kafka.send(
            KafkaTopics.Error,
            e.localizedMessage + '\n' + e.stackTrace.map { it.toString() }.reduce { acc, s -> acc + '\n' + s })
          errorType = ErrorType.UnknownError
        }

      }


    } catch (e: Exception) {
      kafka.send(KafkaTopics.Error,
        e.localizedMessage
            + '\n'
            + e.stackTrace.map { it.toString() }
          .reduce { acc, s -> acc + '\n' + s }
      )
    }

    return mapper.encodeToString(ErrorResponse(ErrorType.UnknownError))
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