package com.trx.presentation.request

data class AccountCreateRequest(
    val customerId: Int,
    val balance: Int
)