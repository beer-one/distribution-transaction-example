package com.trx.infrastructure.kafka.event

data class ApplyPaymentEvent(
    val customerId: Int,
    val price: Int
)