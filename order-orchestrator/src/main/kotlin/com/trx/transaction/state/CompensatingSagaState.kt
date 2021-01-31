package com.trx.transaction.state

import com.trx.transaction.saga.OrderSaga

interface CompensatingSagaState {
    suspend fun doCompensatingTransaction(saga: OrderSaga)
}