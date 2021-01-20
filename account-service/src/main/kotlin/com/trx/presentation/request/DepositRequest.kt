package com.trx.presentation.request

data class DepositRequest(
    val customerId: Int,
    val money: Int
)