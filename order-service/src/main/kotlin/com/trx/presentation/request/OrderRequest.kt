package com.trx.presentation.request

data class OrderRequest(
    val productId: Int,
    val count: Int,
    val customerId: Int
)
