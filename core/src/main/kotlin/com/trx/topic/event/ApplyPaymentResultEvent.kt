package com.trx.topic.event

data class ApplyPaymentResultEvent(
    val success: Boolean,
    val failureReason: String
)