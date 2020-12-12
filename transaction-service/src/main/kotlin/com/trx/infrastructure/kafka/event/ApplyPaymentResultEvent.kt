package com.trx.infrastructure.kafka.event

data class ApplyPaymentResultEvent(
    val success: Boolean,
    val failureReason: String
)