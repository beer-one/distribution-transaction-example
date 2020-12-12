package com.trx.presentation.handler

import com.trx.application.order.OrderCommandService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import com.trx.presentation.request.OrderRequest
import java.lang.Exception

@Component
class OrderHandler(
    private val orderCommandService: OrderCommandService
) {

    suspend fun createOrder(request: ServerRequest): ServerResponse {
        val orderRequest = request.awaitBodyOrNull<OrderRequest>()
            ?: throw Exception()

        orderCommandService.create(orderRequest)

        return noContent().buildAndAwait()
    }
}