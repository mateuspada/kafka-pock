package com.example.kafkapock.config

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.annotation.PostConstruct

@ConfigurationProperties(prefix = "spring.kafka")
class KafkaProperties(var bootstrapServers: String = ""){
    @PostConstruct
    fun postConstruct() {
        if (this.bootstrapServers.isEmpty())  throw RuntimeException("Valor não encontrado")
    }
}