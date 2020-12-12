package com.trx.infrastructure.kafka.event

data class CheckProductResultEvent(
    val success: Boolean,
    val failureReason: String,
    val totalPrice: Int
)