package org.eten.authentication

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@Configuration
@ConfigurationPropertiesScan
@EnableScheduling
class AuthenticationApplication

fun main(args: Array<String>) {
  runApplication<AuthenticationApplication>(*args)
}
