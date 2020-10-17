package com.trx.application.transaction

import com.trx.domain.event.AccountTransactionEvent
import org.springframework.stereotype.Component

@Component
class EventBus {
    private val transactions = mutableSetOf<DistributedTransaction>()
    private val events = mutableSetOf<AccountTransactionEvent>()

    fun sendTransaction(event: DistributedTransaction) = transactions.add(event)

    fun receiveTransaction(eventId: String): DistributedTransaction? {
        while ( transactions.find { it.id == eventId } == null) {
            Thread.sleep(TIME)
        }

        return transactions.find { it.id == eventId }
            ?.also { transactions.remove(it) }
    }

    fun sendEvent(event: AccountTransactionEvent) = events.add(event)

    fun receiveEvent(eventId: String): AccountTransactionEvent? {
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