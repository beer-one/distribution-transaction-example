package com.trx.topic.event

data class CheckProductEvent(
    val productId: Int,
    val count: Int
)