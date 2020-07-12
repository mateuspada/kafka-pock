package com.example.kafkapock.config

import org.springframework.boot.context.properties.ConfigurationProperties
import javax.annotation.PostConstruct

@ConfigurationProperties(prefix = "kafka")
class KafkaProperties(
    var clusters: List<KafkaCluster> = emptyList(),
    var group: String = "",
    var topics: List<KafkaTopics> = emptyList()
) {
    @PostConstruct
    fun postConstruct() {
        if (this.clusters.isEmpty()) throw RuntimeException("Cluster não encontrado")
        if (this.group.isEmpty()) throw RuntimeException("Grupo não encontrado")
        if (this.topics.isEmpty()) throw RuntimeException("Nenhum tópico encontrado")
    }
}

data class KafkaCluster(
    var name: String = "",
    var host: String = "",
    var port: Int = 0
)

data class KafkaTopics(var name: String = "")