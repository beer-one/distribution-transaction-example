package com.trx.infrastructure.configuration

import com.trx.presentation.listener.OrderCompletedEventListener
import com.trx.presentation.listener.OrderFailedEventListener
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.AcknowledgingMessageListener

@Configuration
class KafkaConsumerConfiguration(
    private val kafkaProperties: KafkaProperties
) {

    @Bean
    fun orderCompletedEventListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory()
    }

    @Bean
    fun orderFailedEventListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        return makeFactory()
    }

    private fun makeFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        return ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            consumerFactory = consumerFactory()
            containerProperties.ackMode = kafkaProperties.listener.ackMode
            setConcurrency(3)
            containerProperties.pollTimeout = 5000
        }
    }

    private fun consumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties())
    }
}