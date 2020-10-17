package com.trx.domain.event

import com.trx.domain.entity.Account

data class AccountTransactionEvent(
    val transactionId: String,
    val account: Account
)