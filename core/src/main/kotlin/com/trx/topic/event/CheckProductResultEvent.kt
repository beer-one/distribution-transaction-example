package com.trx.topic.event

data class CheckProductSucceed(
    val totalPrice: Int
)

data class CheckProductFailed(
    val failureReason: String
)