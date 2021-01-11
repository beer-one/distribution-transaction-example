package com.trx.transaction.saga

import com.trx.application.event.TransactionEventPublisher
import com.trx.topic.event.OrderCreateEvent
import com.trx.transaction.state.OrderPending
import com.trx.transaction.state.OrderSagaState

class OrderSaga private constructor(
    private var state: OrderSagaState,
    val orderId: Int,
    val customerId: Int,
    val productId: Int,
    val count: Int,
    val key: String
) {

    companion object {
        fun init(
            eventPublisher: TransactionEventPublisher,
            key: String,
            event: OrderCreateEvent
        ): OrderSaga = OrderSaga(
            state = OrderPending(eventPublisher),
            orderId = event.orderId,
            customerId = event.customerId,
            productId = event.productId,
            count = event.count,
            key = key
        )
    }

    suspend fun changeStateAndOperate(state: OrderSagaState) {
        this.state = state
        this.operate()
    }

    suspend fun operate() {
        state.operate(this)
    }
}