package com.trx.infrastructure.feign

import com.trx.application.transaction.request.OrderTransactionRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(value = "orchestrator", url = "localhost:6030")
interface OrchestratorFeignClient {

    @PostMapping("/transactions/order")
    fun doOrderTransaction(@RequestBody request: OrderTransactionRequest)
}