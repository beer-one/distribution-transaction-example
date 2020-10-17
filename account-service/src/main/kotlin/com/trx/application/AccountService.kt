package com.trx.application

import com.trx.domain.event.AccountTransactionEvent
import com.trx.domain.repository.AccountRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
@Transactional
class AccountService (
    private val repository: AccountRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun payment(id: Int, amount: Int, transactionId: String) =  transfer(id, amount, transactionId)

    fun withdrawal(id: Int, amount: Int, transactionId: String) =  transfer(id, (-1) * amount, transactionId)

    private fun transfer(id: Int, amount: Int, transactionId: String) {
        repository.findByIdOrNull(id)?.let {
            it.balance += amount
            eventPublisher.publishEvent(AccountTransactionEvent(transactionId, it))
            repository.save(it)
        }
    }
}