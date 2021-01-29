package com.trx.transaction.state

import com.trx.transaction.saga.OrderSaga

interface CompensatingSaga {
    suspend fun doCompensatingTransaction(saga: OrderSaga)
}