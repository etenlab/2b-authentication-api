package org.eten.authentication

import org.eten.authentication.core.ConfigEnv
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
@EnableConfigurationProperties
class AppConfig(

  @Value("\${server.hostname}")
  val hostname: String,

  @Value("\${server.jwtSecret}")
  val jwt_secret: String,

  @Value("\${awsAccessKeyId}")
  val awsAccessKeyId: String,

  @Value("\${awsSecretAccessKey}")
  val awsSecretAccessKey: String,

  @Value("\${email.server}")
  val emailServer: String,

  @Value("\${env}")
  val env: ConfigEnv,

  @Value("\${s3_url_prefix}")
  val s3_url_prefix: String,

  @Value("\${spring.kafka.bootstrap-servers}")
  val kafka_bootstrap_address: String,

  ) {

  val ip = InetAddress.getLocalHost().hostAddress

}
