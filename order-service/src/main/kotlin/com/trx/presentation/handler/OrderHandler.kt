package com.trx.presentation.handler

import com.trx.application.order.OrderCommandService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import com.trx.domain.dto.Account
import com.trx.domain.dto.Product
import com.trx.domain.entity.Order
import com.trx.domain.event.OrderTransactionEvent
import com.trx.domain.repository.OrderRepository
import com.trx.presentation.request.OrderRequest
import java.lang.Exception
import kotlin.random.Random

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