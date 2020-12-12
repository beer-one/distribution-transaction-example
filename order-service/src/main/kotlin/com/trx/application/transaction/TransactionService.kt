package com.trx.application.transaction

import com.trx.application.transaction.request.OrderTransactionRequest

interface TransactionService {
    fun doOrderTransaction(request: OrderTransactionRequest)
}