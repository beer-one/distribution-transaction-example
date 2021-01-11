package com.trx.transaction.state

import com.trx.application.event.TransactionEventPublisher
import com.trx.topic.Topic
import com.trx.topic.event.*
import com.trx.topic.event.OrderCancelEvent
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * @see com.trx.transaction.state.OrderCanceled (next state)
 */
class OrderPaymentFailed (
    private val eventPublisher: TransactionEventPublisher
) : OrderSagaState, CompensatingSaga {

    override suspend fun operate(saga: OrderSaga) {
        doCompensatingTransaction(saga)

        eventPublisher.publishEvent(
            Topic.ORDER_CANCELED,
            saga.key,
            OrderCancelEvent(saga.orderId)
        ).awaitSingle()
    }

    override suspend fun doCompensatingTransaction(saga: OrderSaga) {
        eventPublisher.publishEvent(
            Topic.CHECK_PRODUCT_ROLLBACK,
            saga.key,
            ProductRollBackEvent(saga.productId, saga.count)
        ).awaitSingle()
    }
}