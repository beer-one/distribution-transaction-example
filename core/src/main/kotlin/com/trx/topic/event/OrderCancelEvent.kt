package com.trx.topic.event

data class OrderCancelEvent(
    val orderId: Int,
    val failureReason: String
)