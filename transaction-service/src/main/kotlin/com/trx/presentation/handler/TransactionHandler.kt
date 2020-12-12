package com.trx.presentation.handler

import com.trx.application.order.OrderTransactionService
import com.trx.presentation.request.OrderTransactionRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent

@Component
class TransactionHandler (
    private val orderTransactionService: OrderTransactionService
) {
    suspend fun doOrderTransaction(request: ServerRequest): ServerResponse {
        val orderTransactionRequest = request.awaitBodyOrNull<OrderTransactionRequest>()
            ?: throw Exception()

        orderTransactionService.orchestrateOrderTransaction(orderTransactionRequest)

        return noContent().buildAndAwait()
    }
}