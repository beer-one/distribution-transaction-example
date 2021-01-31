package com.trx.transaction

import com.trx.transaction.saga.OrderSaga
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SagaInMemoryRepository: SagaRepository {
    private val orderSagaMap: ConcurrentHashMap<String, OrderSaga> = ConcurrentHashMap()

    override fun save(id: String, orderSaga: OrderSaga) {
        orderSagaMap[id] = orderSaga
    }

    override fun findById(id: String): OrderSaga? = orderSagaMap[id]

    override fun deleteById(id: String) {
        orderSagaMap.remove(id)
    }
}