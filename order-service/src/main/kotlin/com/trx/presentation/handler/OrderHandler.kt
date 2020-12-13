package com.trx.presentation.handler

import com.trx.application.order.OrderCommandService
import com.trx.errors.exception.IncorrectFormatBodyException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import com.trx.presentation.request.OrderRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Component
class OrderHandler(
    private val orderCommandService: OrderCommandService
) {

    suspend fun createOrder(request: ServerRequest): ServerResponse {
        val orderRequest = request.awaitBodyOrNull<OrderRequest>()
            ?: throw IncorrectFormatBodyException()

        return orderCommandService.create(orderRequest).let {
            ok().bodyValueAndAwait(it)
        }
    }
}