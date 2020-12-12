package com.trx.infrastructure.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.trx.application.event.TransactionEventListener
import com.trx.infrastructure.kafka.event.CheckProductEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import java.time.Duration
import kotlin.reflect.KClass

@Component
class KafkaTransactionEventListener (
    private val reactiveKafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, String>,
    private val objectMapper: ObjectMapper
): TransactionEventListener {
    override fun <T : Any> listenEvent(topic: String, key: String, kClass: KClass<T>): Flux<ReceiverRecord<String, T>> {
        return reactiveKafkaConsumerTemplate.receive()
            .filter { it.key() == key }
            .map {
                ReceiverRecord(
                    ConsumerRecord(
                        it.topic(),
                        it.partition(),
                        it.offset(),
                        it.timestamp(),
                        it.timestampType(),
                        it.checksum(),
                        it.serializedKeySize(),
                        it.serializedValueSize(),
                        it.key(),
                        objectMapper.readValue(it.value(), kClass.java),
                        it.headers(),
                        it.leaderEpoch()
                    ),
                    it.receiverOffset()
                )
            }
            .timeout(TIMEOUT, Schedulers.boundedElastic())
    }

    companion object {
        private val TIMEOUT = Duration.ofSeconds(4L)
    }
}