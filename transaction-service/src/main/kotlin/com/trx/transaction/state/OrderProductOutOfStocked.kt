package com.trx.transaction.state

import com.trx.application.event.TransactionEventPublisher
import com.trx.topic.Topic
import com.trx.topic.event.OrderCancelEvent
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * @see com.trx.transaction.state.OrderCanceled (next state)
 */
class OrderProductOutOfStocked (
    private val eventPublisher: TransactionEventPublisher,
    private val reason: String
) : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        eventPublisher.publishEvent(
            Topic.ORDER_CANCELED,
            saga.key,
            OrderCancelEvent(saga.orderId)
        ).awaitSingle()
    }
}