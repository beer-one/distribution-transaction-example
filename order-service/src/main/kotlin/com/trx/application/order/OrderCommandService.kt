package com.trx.application.order

import com.trx.application.event.OrderCreateEvent
import com.trx.application.event.TransactionEventPublisher
import com.trx.domain.entity.Order
import com.trx.domain.repository.OrderRepository
import com.trx.presentation.request.OrderRequest
import com.trx.topic.Topic
import com.trx.utils.KeyGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class OrderCommandService (
    private val repository: OrderRepository,
    private val transactionEventPublisher: TransactionEventPublisher
) {
    @Transactional
    fun create(request: OrderRequest): Mono<Void> {
        return repository.save(
            Order(
                productId = request.productId,
                count = request.count,
                customerId = request.customerId
            )
        ).let {
            transactionEventPublisher.publishEvent(
                topic = Topic.ORDER_CREATE_TRANSACTION,
                key = KeyGenerator.generateKey(),
                event = OrderCreateEvent(
                    orderId = it.id,
                    productId = request.productId,
                    count = request.count,
                    customerId = request.customerId
                )
            ).then()
        }
    }
}