package com.trx.application.transaction

import com.trx.application.transaction.request.OrderTransactionRequest
import com.trx.application.transaction.response.OrderTransactionResponse

interface TransactionService {
    fun doOrderTransaction(request: OrderTransactionRequest): OrderTransactionResponse
}