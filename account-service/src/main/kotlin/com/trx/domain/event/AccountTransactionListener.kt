package com.trx.domain.event

import com.trx.application.transaction.DistributedTransactionStatus
import com.trx.exception.AccountProcessingException
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.client.RestTemplate

@Component
class AccountTransactionListener (
    private val restTemplate: RestTemplate,
    private val eventBus: EventBus
) {

    companion object {
        private const val TIME = 10L
        private const val LIMIT = 100
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleEvent(event: AccountTransactionEvent) {
        eventBus.sendEvent(event)

        for (x in 0..LIMIT) {
            eventBus.receiveTransaction(event.transactionId)?.let {
                if (it.status != DistributedTransactionStatus.CONFIRMED) {
                    throw AccountProcessingException()
                }

                return
            }
            Thread.sleep(TIME)
        }

        throw AccountProcessingException()
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handleAfterRollback(event: AccountTransactionEvent) {
        restTemplate.put(
            "http://transaction-server-/transactions/transactionId/participants/{serviceId}/status/{status}",
            null,
            "account-service",
            "TO_ROLLBACK"
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    fun handleAfterCompletion(event: AccountTransactionEvent) {
        restTemplate.put(
            "http://transaction-server-/transactions/transactionId/participants/{serviceId}/status/{status}",
            null,
            "account-service",
            "CONFIRM"
        )
    }
}