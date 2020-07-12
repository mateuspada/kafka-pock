package com.example.kafkapock

import com.example.kafkapock.config.KafkaConsumerTopicProperties
import com.example.kafkapock.config.KafkaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
	KafkaConsumerTopicProperties::class,
	KafkaProperties::class
)
class KafkaPockApplication

fun main(args: Array<String>) {
	runApplication<KafkaPockApplication>(*args)
}
