package com.example.kafkapock

import com.example.kafkapock.config.KafkaProperties
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import javax.validation.Valid

@Service
class KafkaPockConsumer(kafkaProperties: KafkaProperties) {

    @KafkaListener(
        topics = ["#{@topicKafkaPock}"],
        containerFactory = "kafkaHelloWorldListenerContainerFactory"
    )
    fun listen(
        @Valid @Payload helloWorld: HelloWorld,
        @Header("x-teste") test: String
    ) {
        println("Header $test")
        println(helloWorld)
    }

}