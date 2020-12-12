package com.trx.application.event

import reactor.core.publisher.Flux
import reactor.kafka.receiver.ReceiverRecord
import kotlin.reflect.KClass

interface TransactionEventListener {
    fun <T: Any> listenEvent(topic: String, key: String, kClass: KClass<T>): Flux<ReceiverRecord<String, T>>
}