package com.trx.application.order

import com.trx.topic.event.OrderCreateEvent
import com.trx.application.event.TransactionEventPublisher
import com.trx.domain.entity.Order
import com.trx.domain.enums.OrderStatus
import com.trx.domain.repository.OrderRepository
import com.trx.presentation.request.OrderRequest
import com.trx.topic.Topic
import com.trx.utils.KeyGenerator
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class OrderCommandService (
    private val repository: OrderRepository,
    private val transactionEventPublisher: TransactionEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun create(request: OrderRequest): Mono<Unit> {
        return repository.save(
            Order(
                productId = request.productId,
                count = request.count,
                customerId = request.customerId
            )
        ).let {
            transactionEventPublisher.publishEvent(
                topic = Topic.ORDER_CREATED,
                key = KeyGenerator.generateKey(),
                event = OrderCreateEvent(
                    orderId = it.id,
                    productId = request.productId,
                    count = request.count,
                    customerId = request.customerId
                )
            ).then(Mono.just(Unit))
                .also { logger.info("[ORDER] status = PENDING, 트랜잭션 요청")}
        }
    }

    @Transactional
    fun modifyOrderStatus(id: Int, status: OrderStatus) {
        repository.findByIdOrNull(id)
            ?.modifyStatus(status)
    }
}