package com.trx.topic.event

data class OrderRollBacked(
    val failureReason: String
)