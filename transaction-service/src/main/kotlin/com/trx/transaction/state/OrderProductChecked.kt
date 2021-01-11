package com.trx.transaction.state

import com.trx.application.event.TransactionEventPublisher
import com.trx.topic.Topic
import com.trx.topic.event.ApplyPaymentEvent
import com.trx.topic.event.CheckProductEvent
import com.trx.topic.event.CheckProductSucceed
import com.trx.transaction.saga.OrderSaga
import kotlinx.coroutines.reactive.awaitSingle

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * @see com.trx.transaction.state.OrderPaymentFinished if payment success
 * @see com.trx.transaction.state.OrderPaymentFailed if payment failed
 */
class OrderProductChecked (
    private val eventPublisher: TransactionEventPublisher,
    private val totalPrice: Int
) : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        eventPublisher.publishEvent(
            Topic.APPLY_PAYMENT,
            saga.key,
            ApplyPaymentEvent(saga.customerId, totalPrice)
        ).awaitSingle()
    }
}