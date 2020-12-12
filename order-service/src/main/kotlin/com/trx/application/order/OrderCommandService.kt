package com.trx.application.order

import com.trx.application.executeWithContext
import com.trx.application.transaction.TransactionService
import com.trx.application.transaction.request.OrderTransactionRequest
import com.trx.domain.entity.Order
import com.trx.domain.repository.OrderRepository
import com.trx.presentation.request.OrderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class OrderCommandService (
    private val repository: OrderRepository,
    private val transactionTemplate: TransactionTemplate,
    private val transactionService: TransactionService
) {
    suspend fun create(request: OrderRequest) {
        transactionTemplate.executeWithContext {
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
}