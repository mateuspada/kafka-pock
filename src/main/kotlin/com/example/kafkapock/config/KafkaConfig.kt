package com.example.kafkapock.config

import com.example.kafkapock.HelloWorld
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.admin.NewTopic
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
    val kafkaConsumerTopicProperties: KafkaConsumerTopicProperties,
    val kafkaProperties: KafkaProperties,
    val validator: LocalValidatorFactoryBean
) : KafkaListenerConfigurer {
    @Bean
    fun paymentsConsumer(): NewTopic {
        return NewTopic(
            kafkaConsumerTopicProperties.topic,
            kafkaConsumerTopicProperties.partitions,
            kafkaConsumerTopicProperties.replication
        )
    }

    @Bean
    fun messageProducerFactory(): DefaultKafkaProducerFactory<String, HelloWorld> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory<String, HelloWorld>(configProps, StringSerializer(), JsonSerializer(ObjectMapper()))
    }

    @Bean
    fun messageKafkaTemplate(): KafkaTemplate<String, HelloWorld> {
        return KafkaTemplate(messageProducerFactory())
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, HelloWorld> {
        val props: MutableMap<String, Any> = HashMap()
        props[ConsumerConfig.GROUP_ID_CONFIG] = kafkaConsumerTopicProperties.group
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java

        return DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(HelloWorld::class.java))
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, HelloWorld> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, HelloWorld>()
        factory.consumerFactory = consumerFactory()
        factory.setErrorHandler(errorHandler(kafkaConsumerTopicProperties.group, messageKafkaTemplate()))
        return factory
    }

    private fun errorHandler(groupId: String, kafkaTemplate: KafkaTemplate<String, HelloWorld>): ErrorHandler =
        SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(kafkaTemplate, destinationResolver(groupId)))

    private fun destinationResolver(groupId: String): BiFunction<ConsumerRecord<*, *>, Exception, TopicPartition> =
        BiFunction { cr: ConsumerRecord<*, *>, _: Exception? ->
            TopicPartition(cr.topic() + ".DLT", cr.partition())
        }

    override fun configureKafkaListeners(registrar: KafkaListenerEndpointRegistrar) {
        registrar.validator = this.validator;
    }
}


