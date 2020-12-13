package com.trx.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventPublisher
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult

@Component
class KafkaTransactionEventPublisher(
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, String>,
    private val objectMapper: ObjectMapper
): TransactionEventPublisher {
    override fun publishEvent(topic: String, key: String, event: Any): Mono<SenderResult<Void>> {
        return reactiveKafkaProducerTemplate.send(topic, key, objectMapper.writeValueAsString(event))
    }

}