package com.trx.presentation.request

data class ProductCreateRequest(
    val name: String,
    val count: Int,
    val price: Int
)