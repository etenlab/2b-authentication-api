package org.eten.authentication.core

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

//@Configuration("KafkaTopicConfig")
//class KafkaTopicConfig(
//    @Autowired
//    val app_config: AppConfig,
//
//    @Autowired
//    kafkaTemplate: KafkaTemplate<String, String>,
//
//    ) {
//
//  init {
//    kafkaTemplate.send(KafkaTopics.InstanceInfo.name,
//                       "server started with ip: ${app_config.ip}")
//  }
//
//  @Bean
//  fun kafkaAdmin(): KafkaAdmin {
//    val configs: MutableMap<String, Any?> = HashMap()
//    configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = app_config.kafka_bootstrap_address
//    return KafkaAdmin(configs)
//  }
//
//  @Bean
//  fun topic_instance_info(): NewTopic {
//    return NewTopic(KafkaTopics.InstanceInfo.name, 1, 1.toShort())
//  }
//
//  @Bean
//  fun topic_errors(): NewTopic {
//    return NewTopic(KafkaTopics.Error.name, 1, 1.toShort())
//  }
//
//}
//
//@Configuration
//class KafkaProducerConfig(
//    @Autowired
//    val app_config: AppConfig,
//
//    ) {
//
//  @Bean
//  fun producerFactory(): ProducerFactory<String, String> {
//    val configProps: MutableMap<String, Any> = HashMap()
//    configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = app_config.kafka_bootstrap_address
//    configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//    configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//    return DefaultKafkaProducerFactory(configProps)
//  }
//
//  @Bean
//  fun kafkaTemplate(): KafkaTemplate<String, String> {
//    return KafkaTemplate(producerFactory())
//  }
//}