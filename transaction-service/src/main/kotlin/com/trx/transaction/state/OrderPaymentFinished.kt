package com.trx.transaction.state

import com.trx.application.event.TransactionEventPublisher
import com.trx.topic.Topic
import com.trx.topic.event.OrderApproveEvent
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * @see com.trx.transaction.state.OrderApproved (next state)
 */
class OrderPaymentFinished (
    private val eventPublisher: TransactionEventPublisher
) : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        eventPublisher.publishEvent(
            Topic.APPLY_PAYMENT,
            saga.key,
            OrderApproveEvent(saga.orderId)
        ).awaitSingle()
    }
}