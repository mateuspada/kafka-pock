package com.example.kafkapock

import com.example.kafkapock.config.KafkaConsumerTopicProperties
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import javax.validation.Valid

@Service
class KafkaPockConsumer(val kafkaConsumerTopicProperties: KafkaConsumerTopicProperties) {

    @KafkaListener(
        topics = ["\${kafka.consumer.kafkapock.topic}"],
        groupId = "\${kafka.consumer.kafkapock.group}"
    )
    fun listen(@Valid @Payload helloWorld: HelloWorld,
               @Header("x-teste") test: String){
        println("Header $test")
        println(helloWorld)
    }


}