package com.trx.application.event

import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult

interface TransactionEventPublisher {
    fun publishEvent(topic: String, key: String, event: Any): Mono<SenderResult<Void>>
}