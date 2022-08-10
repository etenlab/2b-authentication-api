package org.eten.authentication

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DataSourceConfiguration {
  @Bean("writerDataSource")
  @ConfigurationProperties("spring.writer-datasource")
  fun writerDataSource(): DataSource {
    return DataSourceBuilder
        .create()
        .build()
  }
}

@Configuration
class ReaderDataSourceConfiguration {
  @Bean("readerDataSource")
  @ConfigurationProperties("spring.reader-datasource")
  fun readerDataSource(): DataSource {
    return DataSourceBuilder
        .create()
        .build()
  }
}