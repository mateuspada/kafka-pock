package com.example.kafkapock.config

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.annotation.PostConstruct

@ConfigurationProperties(prefix = "kafka.consumer.kafkapock")
class KafkaConsumerTopicProperties(var group: String = "",
                                   var topic: String = "",
                                   var partitions: Int = 0,
                                   var replication: Short = 0) {
    @PostConstruct
    fun postConstruct() {
        if ((this.group.isEmpty()) or (this.topic.isEmpty()) or
            (this.partitions <= 0) or (this.replication <= 0))  throw RuntimeException("Valor não encontrado")
    }
}
