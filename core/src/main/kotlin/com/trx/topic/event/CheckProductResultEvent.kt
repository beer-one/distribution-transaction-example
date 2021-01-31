package com.trx.topic.event

data class CheckProductCompleted(
    val totalPrice: Int
)

data class CheckProductFailed(
    val failureReason: String
)