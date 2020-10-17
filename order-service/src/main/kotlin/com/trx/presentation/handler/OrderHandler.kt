package com.trx.presentation.handler

import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import com.trx.application.transaction.DistributedTransaction
import com.trx.domain.dto.Account
import com.trx.domain.dto.Product
import com.trx.domain.entity.Order
import com.trx.domain.repository.OrderRepository
import java.lang.Exception
import kotlin.random.Random

@Component
class OrderHandler(
    private val restTemplate: RestTemplate,
    private val repository: OrderRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    suspend fun addAndRollback(request: ServerRequest): ServerResponse {
        val order = request.awaitBodyOrNull<Order>()
            ?: throw Exception()

        val transaction = restTemplate.postForObject(
            "http://transaction-server/transactions",
            DistributedTransaction(),
            DistributedTransaction::class.java
        )!!

        repository.save(order).also {
            val totalPrice = updateProduct(transaction.id!!, it).getTotalPrice()

            val account = restTemplate.getForObject(
                "http://account-service/accounts/customer/{customerId}",
                Array<Account>::class.java,
                it.customerId
            )!!.first { it.balance >= totalPrice}

            updateAccount(transaction.id!!, account.id, totalPrice)

            eventPublisher.publishEvent(OrderTransactionEvent(transaction.id!!))

            // ROLLBACK 상황 만들기 위해
            if (Random.nextInt() % 2 == 0) throw Exception()

        }

        return noContent().buildAndAwait()
    }


    private fun updateProduct(transactionId: String, order: Order): Product {
        val header = HttpHeaders().apply {
            set("X-Transaction-ID", transactionId)
        }

        val entity = HttpEntity<Any?>(header)

        return restTemplate.exchange(
            "http://product-service/products/{id}/count/{count}",
            HttpMethod.PUT,
            null,
            Product::class.java,
            order.id,
            order.count
        ).body!!
    }

    private fun updateAccount(transactionId: String, accountId: Int, totalPrice: Int): Account {
        val header = HttpHeaders().apply {
            set("X-Transaction-ID", transactionId)
        }

        val entity = HttpEntity<Any?>(header)

        return restTemplate.exchange(
            "http://account-service/accounts/{id}/withdrawal/{amount}",
            HttpMethod.PUT,
            null,
            Account::class.java,
            accountId,
            totalPrice
        ).body!!
    }
}