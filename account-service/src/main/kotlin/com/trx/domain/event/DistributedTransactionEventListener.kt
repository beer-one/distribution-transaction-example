package com.trx.domain.event

import com.trx.application.transaction.DistributedTransaction
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DistributedTransactionEventListener (
    private val eventBus: EventBus
) {
    @EventListener
    fun onMessage(transaction: DistributedTransaction) {
        eventBus.sendTransaction(transaction)
    }
}