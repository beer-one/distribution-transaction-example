package com.trx.infrastructure.kafka.event

data class CheckProductEvent(
    val productId: Int,
    val count: Int
)