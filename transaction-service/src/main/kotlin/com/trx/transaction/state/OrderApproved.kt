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
class OrderApproved (
    private val orderRepository: OrderRepository
) : OrderSagaState {

    override suspend fun operate(saga: OrderSaga) {
        orderRepository.findByIdOrNull(saga.orderId)?.let {
            it.orderStatus = OrderStatus.APPROVED

            orderRepository.save(it)
        }
    }
}