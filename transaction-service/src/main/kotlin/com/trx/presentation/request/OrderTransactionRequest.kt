package com.trx.presentation.request

data class OrderTransactionRequest(
    val orderId: Int,
    val productId: Int,
    val count: Int,
    val customerId: Int
)