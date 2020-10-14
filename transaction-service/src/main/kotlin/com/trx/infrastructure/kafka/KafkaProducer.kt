package com.trx.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender

@Component
class KafkaProducer(
    private val kafkaSender: KafkaSender<String, String>,
    private val objectMapper: ObjectMapper
) {
    fun sendMessage(topic: String, message: Any) {
        kafkaSender.createOutbound().send(
            Mono.just(ProducerRecord(topic, objectMapper.writeValueAsString(message)))
        )
    }
}