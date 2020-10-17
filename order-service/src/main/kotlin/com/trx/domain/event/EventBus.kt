package com.trx.domain.event

import org.springframework.stereotype.Component
import com.trx.application.transaction.DistributedTransaction

@Component
class EventBus {
    private val transactions = mutableSetOf<DistributedTransaction>()
    private val events = mutableSetOf<OrderTransactionEvent>()

    fun sendTransaction(event: DistributedTransaction) = transactions.add(event)

    fun receiveTransaction(eventId: String): DistributedTransaction? {
        while ( transactions.find { it.id == eventId } == null) {
            Thread.sleep(TIME)
        }

        return transactions.find { it.id == eventId }
            ?.also { transactions.remove(it) }
    }

    fun sendEvent(event: OrderTransactionEvent) = events.add(event)

    fun receiveEvent(eventId: String): OrderTransactionEvent? {
        while (events.find { it.transactionId == eventId } == null) {
            Thread.sleep(TIME)
        }

        return events.find { it.transactionId == eventId }
            ?.also { events.remove(it) }
    }

    companion object {
        private const val TIME = 10L
    }
}