package com.trx.transaction.state

import com.trx.topic.Topic
import com.trx.topic.event.CheckProductEvent
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * @see com.trx.transaction.state.OrderProductChecked if checking product success
 * @see com.trx.transaction.state.OrderProductOutOfStocked if checking product failed
 */
class OrderPending : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        saga.publishEvent(
            Topic.CHECK_PRODUCT,
            saga.key,
            CheckProductEvent(saga.productId, saga.count)
        ).awaitSingle()
    }
}