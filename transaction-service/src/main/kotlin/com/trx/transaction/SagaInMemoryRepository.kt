package com.trx.transaction

import com.trx.transaction.saga.OrderSaga
import java.util.concurrent.ConcurrentHashMap

object OrderSagaInMemoryRepository {
    private val orderSagaMap: ConcurrentHashMap<String, OrderSaga> = ConcurrentHashMap()

    fun save(key: String, orderSaga: OrderSaga) {
        orderSagaMap[key] = orderSaga
    }

    fun findByID(key: String): OrderSaga? = orderSagaMap[key]

    fun deleteById(key: String) {
        orderSagaMap.remove(key)
    }
}