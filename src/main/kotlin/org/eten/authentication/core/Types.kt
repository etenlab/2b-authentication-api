package org.eten.authentication.core

enum class ConfigEnv {
  local,
  test,
  prod,
}

enum class KafkaTopics {
  InstanceInfo,
  Error,
}