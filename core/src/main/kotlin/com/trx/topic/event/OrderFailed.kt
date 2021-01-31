package com.trx.topic.event

data class OrderFailed(
    val orderId: Int,
    val failureReason: String
)