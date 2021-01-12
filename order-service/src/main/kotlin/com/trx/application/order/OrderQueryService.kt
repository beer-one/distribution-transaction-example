package com.trx.application.order

import com.trx.domain.entity.Order
import com.trx.domain.repository.OrderRepository
import org.springframework.stereotype.Component

@Component
class OrderQueryService(
    private val repository: OrderRepository
) {
    fun getAll(customerId: Int): List<Order> {
        return repository.findByCustomerId(customerId)
    }
}