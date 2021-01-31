package com.trx.topic.event

data class PaymentCompleted(
    val restBalance: Int
)

data class PaymentFailed(
    val failureReason: String
)
