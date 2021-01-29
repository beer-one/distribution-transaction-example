package com.trx.transaction.state

import com.trx.topic.Topic
import com.trx.topic.event.OrderApproveEvent
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * -> ORDER_APPROVED (FINISHED)
 */
class OrderPaymentFinished: OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.ORDER_APPROVED,
            saga.key,
            OrderApproveEvent(saga.orderId)
        ).awaitSingle()
    }
}