package com.trx.domain.dto

data class Product(
    val id: Int,
    val name: String,
    val count: Int,
    val price: Int
) {
    fun getTotalPrice() = price * count
}