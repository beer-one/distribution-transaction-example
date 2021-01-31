package com.trx.transaction

import com.trx.transaction.saga.OrderSaga
import java.util.concurrent.ConcurrentHashMap

interface SagaRepository {
    fun save(id: String, orderSaga: OrderSaga)

    fun findById(id: String): OrderSaga?

    fun deleteById(id: String)
}