package com.trx.topic.event

data class ApplyPaymentEvent(
    val customerId: Int,
    val price: Int
)