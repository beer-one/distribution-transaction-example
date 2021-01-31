package com.trx.topic.event

data class ProductRollBackEvent(
    val productId: Int,
    val count: Int,
    val failueReason: String
)