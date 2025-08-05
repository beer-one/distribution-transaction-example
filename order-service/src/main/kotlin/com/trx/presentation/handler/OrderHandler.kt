package com.trx.presentation.handler

import com.trx.application.order.OrderCommandService
import com.trx.application.order.OrderQueryService
import com.trx.errors.exception.IncorrectFormatBodyException
import com.trx.errors.exception.IncorrectParameterException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import com.trx.presentation.request.OrderRequest
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Component
class OrderHandler(
    private val orderCommandService: OrderCommandService,
    private val orderQueryService: OrderQueryService
) {

    suspend fun createOrder(request: ServerRequest): ServerResponse {
        val orderRequest = request.awaitBodyOrNull<OrderRequest>()
            ?: throw IncorrectFormatBodyException()

        return orderCommandService.create(orderRequest)
            .awaitSingle()
            .let { ok().bodyValueAndAwait(it) }
    }

    suspend fun getAll(request: ServerRequest): ServerResponse {
        val customerId = request.queryParamOrNull("customerId")?.toInt()
            ?: throw IncorrectParameterException("customerId")

        return orderQueryService.getAll(customerId).let {
            ok().bodyValueAndAwait(it)
        }
    }

    suspend fun deleteAll(request: ServerRequest): ServerResponse {
        val customerId = request.queryParamOrNull("customerId")?.toInt()
            ?: throw IncorrectParameterException("customerId")

        orderCommandService.deleteAll(customerId)

        return noContent().buildAndAwait()
    }
}