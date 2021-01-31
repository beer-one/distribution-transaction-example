package com.trx.transaction.state

import com.trx.topic.Topic
import com.trx.topic.event.OrderCompleted
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * -> ORDER_COMPLETED (FINISHED)
 */
class OrderPaymentCompleted: OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.ORDER_COMPLETED,
            saga.key,
            OrderCompleted(saga.orderId)
        ).awaitSingle()
    }
}