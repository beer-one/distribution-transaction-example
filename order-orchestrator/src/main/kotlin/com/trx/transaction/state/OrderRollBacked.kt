package com.trx.transaction.state

import com.trx.topic.Topic
import com.trx.topic.event.*
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * -> ORDER_FAILED (FINISHED)
 */
class OrderRollBacked(
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