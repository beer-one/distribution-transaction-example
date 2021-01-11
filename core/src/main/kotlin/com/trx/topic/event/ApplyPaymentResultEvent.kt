package com.trx.topic.event

data class PaymentSucceed(
    val restBalance: Int
)

data class PaymentFailed(
    val failureReason: String
)
