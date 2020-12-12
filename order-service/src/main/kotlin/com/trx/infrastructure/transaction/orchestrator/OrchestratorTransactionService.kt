package com.trx.infrastructure.transaction.orchestrator

import com.trx.application.transaction.TransactionService
import com.trx.application.transaction.request.OrderTransactionRequest
import com.trx.infrastructure.feign.OrchestratorFeignClient
import org.springframework.stereotype.Component

@Component
class OrchestratorTransactionService(
    private val client: OrchestratorFeignClient
) : TransactionService {
    override fun doOrderTransaction(request: OrderTransactionRequest) {
        client.doOrderTransaction(request)
    }
}