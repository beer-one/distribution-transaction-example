package com.trx.application.order

import com.trx.application.transaction.TransactionService
import com.trx.application.transaction.request.OrderTransactionRequest
import com.trx.domain.entity.Order
import com.trx.domain.repository.OrderRepository
import com.trx.presentation.request.OrderRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCommandService (
    private val repository: OrderRepository,
    private val transactionService: TransactionService
) {
    @Transactional
    fun create(request: OrderRequest) {
        repository.save(
            Order(
                productId = request.productId,
                count = request.count,
                customerId = request.customerId
            )
        ).let {
            transactionService.doOrderTransaction(
                OrderTransactionRequest(
                    orderId = it.id,
                    productId = request.productId,
                    count = request.count,
                    customerId = request.customerId
                )
            )
        }
    }
}