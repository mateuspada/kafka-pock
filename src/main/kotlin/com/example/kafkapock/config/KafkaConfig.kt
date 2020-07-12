package com.example.kafkapock.config

import com.example.kafkapock.HelloWorld
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListenerConfigurer
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.ErrorHandler
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.util.function.BiFunction

@Configuration
class KafkaConfig(
        val kafkaProperties: KafkaProperties,
        val validator: LocalValidatorFactoryBean
) : KafkaListenerConfigurer {

    /* Create the topic Automatics when application startup
    @Bean
    fun paymentsConsumer(): NewTopic {
        return TopicBuilder.name(kafkaProperties.topics[0].name)
                .partitions(1)
                .replicas(1)
                .build()
    }
    */

    // Getting Topic Name from class
    @Bean
    fun topicKafkaPock(): String {
        return kafkaProperties.topics[0].name
    }

    // Configuring Hello World Producer
    @Bean
    fun messageHelloWorldProducerFactory(): DefaultKafkaProducerFactory<String, HelloWorld> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "${kafkaProperties.clusters[0].host}:${kafkaProperties.clusters[0].port}"
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory<String, HelloWorld>(configProps, StringSerializer(), JsonSerializer(ObjectMapper()))
    }

    @Bean
    fun messageHelloWorldKafkaTemplate(): KafkaTemplate<String, HelloWorld> {
        return KafkaTemplate(messageHelloWorldProducerFactory())
    }


    // Configuring Hello World Consumer
    @Bean
    fun consumerHelloWorldFactory(): ConsumerFactory<String, HelloWorld> {
        val props: MutableMap<String, Any> = HashMap()
        props[ConsumerConfig.GROUP_ID_CONFIG] = kafkaProperties.group
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "${kafkaProperties.clusters[0].host}:${kafkaProperties.clusters[0].port}"
        // Remove this to consume all the message since the offset 0 in the first group startup
        //props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java

        return DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(HelloWorld::class.java))
    }

    @Bean
    fun kafkaHelloWorldListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, HelloWorld> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, HelloWorld>()
        factory.consumerFactory = consumerHelloWorldFactory()
        factory.setErrorHandler(errorHandler(kafkaProperties.group, messageHelloWorldKafkaTemplate()))
        return factory
    }

    private fun errorHandler(groupId: String, kafkaTemplate: KafkaTemplate<String, HelloWorld>): ErrorHandler =
            SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(kafkaTemplate, destinationResolver(groupId)))

    private fun destinationResolver(groupId: String): BiFunction<ConsumerRecord<*, *>, Exception, TopicPartition> =
            BiFunction { cr: ConsumerRecord<*, *>, _: Exception? ->
                TopicPartition("${cr.topic()}.$groupId.DLT", cr.partition())
            }

    override fun configureKafkaListeners(registrar: KafkaListenerEndpointRegistrar) {
        registrar.validator = this.validator;
    }
}


