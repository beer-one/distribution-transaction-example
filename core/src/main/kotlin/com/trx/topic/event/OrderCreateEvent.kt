package com.trx.topic.event

data class OrderCreateEvent(
    val orderId: Int,
    val productId: Int,
    val count: Int,
    val customerId: Int
)