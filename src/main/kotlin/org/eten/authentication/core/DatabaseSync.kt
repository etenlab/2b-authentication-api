package org.eten.authentication.core

import org.eten.authentication.common.Utility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.util.FileCopyUtils
import java.io.IOException
import java.io.InputStreamReader
import java.io.UncheckedIOException
import javax.sql.DataSource

@Configuration("DatabaseSync")
class DatabaseSync(
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

  init {
    runSqlFile("sql/register.sql")
  }

  private fun asString(resource: ClassPathResource): String? {
    try {
      InputStreamReader(resource.inputStream, Charsets.UTF_8).use { reader ->
        return FileCopyUtils.copyToString(
            reader
        )
      }
    } catch (e: IOException) {
      kafka.send(
          KafkaTopics.Error,
          e.localizedMessage + '\n' + e.stackTrace
              .map { it.toString() }
              .reduce { acc, s -> acc + '\n' + s })
      throw UncheckedIOException(e)
    }
  }

  private fun runSqlFile(fileName: String) {
    val sql = asString(ClassPathResource(fileName))
    if (sql !== null) {
      writer_jdbc.jdbcTemplate.execute(sql)
      kafka.send(KafkaTopics.InstanceInfo, "$fileName successfully run")
    }
  }
}