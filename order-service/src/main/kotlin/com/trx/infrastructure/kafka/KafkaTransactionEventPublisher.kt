package com.trx.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import kotlin.reflect.KClass

@Component
class KafkaTransactionEventPublisher(
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, String>,
    private val objectMapper: ObjectMapper
): TransactionEventPublisher {
    override fun publishEvent(topic: String, key: String, event: Any): Mono<SenderResult<Void>> {
        return reactiveKafkaProducerTemplate.send(topic, key, objectMapper.writeValueAsString(event))
    }
}