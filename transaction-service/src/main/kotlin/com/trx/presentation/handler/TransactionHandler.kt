package com.trx.presentation.handler

import com.trx.application.order.OrderTransactionService
import com.trx.domain.entity.Product
import com.trx.domain.repository.ProductRepository
import com.trx.errors.exception.IncorrectFormatBodyException
import com.trx.presentation.request.OrderTransactionRequest
import com.trx.presentation.response.OrderTransactionResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import java.lang.RuntimeException
import kotlin.system.measureTimeMillis

@Component
class TransactionHandler (
    private val orderTransactionService: OrderTransactionService,
    private val productRepository: ProductRepository
) {
    private var cnt = 0

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun doOrderTransaction(request: ServerRequest): ServerResponse {
        val orderTransactionRequest = request.awaitBodyOrNull<OrderTransactionRequest>()
            ?: throw IncorrectFormatBodyException()

        val balance = orderTransactionService.orchestrateOrderTransaction(orderTransactionRequest)

        return ok().bodyValueAndAwait(OrderTransactionResponse(balance))
    }

    suspend fun test(request: ServerRequest): ServerResponse {
        cnt++
        return if(cnt % 2 == 0) badRequest().buildAndAwait()
        else throw RuntimeException()
    }
}