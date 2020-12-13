package com.trx.presentation.handler

import com.trx.application.order.OrderTransactionService
import com.trx.errors.exception.IncorrectFormatBodyException
import com.trx.presentation.request.OrderTransactionRequest
import com.trx.presentation.response.OrderTransactionResponse
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Component
class TransactionHandler (
    private val orderTransactionService: OrderTransactionService
) {
    suspend fun doOrderTransaction(request: ServerRequest): ServerResponse {
        val orderTransactionRequest = request.awaitBodyOrNull<OrderTransactionRequest>()
            ?: throw IncorrectFormatBodyException()

        val balance = orderTransactionService.orchestrateOrderTransaction(orderTransactionRequest)

        return ok().bodyValueAndAwait(OrderTransactionResponse(balance))
    }
}