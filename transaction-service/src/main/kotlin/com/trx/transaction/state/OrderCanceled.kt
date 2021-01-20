package com.trx.transaction.state

import com.trx.domain.enums.OrderStatus
import com.trx.domain.repository.OrderRepository
import com.trx.transaction.saga.OrderSaga
import org.springframework.data.repository.findByIdOrNull

/**
 * @see com.trx.transaction.state.OrderSagaState
 *
 * Final State
 *
 * 편의상 도메인 객체 주입.. (OrderRepository)
 */
class OrderCanceled (
    private val orderRepository: OrderRepository,
    private val failureReason: String
) : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        orderRepository.findByIdOrNull(saga.orderId)?.let {
            it.cancel(failureReason)

            orderRepository.save(it)
        }
    }
}