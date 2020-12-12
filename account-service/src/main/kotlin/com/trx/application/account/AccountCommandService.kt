package com.trx.application.account

import com.trx.domain.repository.AccountRepository
import com.trx.topic.event.ApplyPaymentEvent
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AccountCommandService (
    private val accountRepository: AccountRepository
) {

    @Transactional
    fun applyPayment(event: ApplyPaymentEvent) {
        accountRepository.findByIdOrNull(event.customerId)
            ?.also { it.applyPayment(event.price) }
            ?: throw Exception()
    }
}