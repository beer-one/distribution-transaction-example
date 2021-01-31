package com.trx.transaction.state

import com.trx.topic.Topic
import com.trx.topic.event.OrderFailed
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * -> ORDER_FAILED (FINISHED)
 */
class OrderProductCheckFailed(
    val failureReason: String
): OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.ORDER_FAILED,
            saga.key,
            OrderFailed(saga.orderId, failureReason)
        ).awaitSingle()
    }
}