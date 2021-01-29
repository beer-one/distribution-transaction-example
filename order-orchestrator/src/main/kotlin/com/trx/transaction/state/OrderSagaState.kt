package com.trx.transaction.state

import com.trx.transaction.saga.OrderSaga

interface OrderSagaState {
    suspend fun operate(saga: OrderSaga)
}