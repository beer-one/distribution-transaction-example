package com.trx.transaction.state

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
class OrderPaymentFailed(
    val failureReason: String
): OrderSagaState, CompensatingSaga {

    override suspend fun operate(saga: OrderSaga) {
        doCompensatingTransaction(saga)

        saga.publishEvent(
            Topic.ORDER_CANCELED,
            saga.key,
            OrderCancelEvent(saga.orderId, failureReason)
        ).awaitSingle()
    }

    override suspend fun doCompensatingTransaction(saga: OrderSaga) {
        saga.publishEvent(
            Topic.CHECK_PRODUCT_ROLLBACK,
            saga.key,
            ProductRollBackEvent(saga.productId, saga.count)
        ).awaitSingle()
    }
}