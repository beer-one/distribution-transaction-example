package com.trx.application.account

import com.trx.domain.entity.Account
import com.trx.domain.repository.AccountRepository
import com.trx.errors.exception.AccountNotFoundException
import com.trx.presentation.request.AccountCreateRequest
import com.trx.presentation.request.DepositRequest
import com.trx.topic.event.ApplyPaymentEvent
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AccountCommandService (
    private val accountRepository: AccountRepository
) {

    @Transactional
    fun applyPayment(event: ApplyPaymentEvent): Int {
        return accountRepository.findByCustomerId(event.customerId)
            ?.applyPayment(event.price)
            ?: throw AccountNotFoundException(event.customerId)
    }

    @Transactional
    fun deposit(request: DepositRequest) {
        accountRepository.findByCustomerId(request.customerId)
            ?.apply { deposit(request.money) }
            ?: throw AccountNotFoundException(request.customerId)
    }

    @Transactional
    fun create(request: AccountCreateRequest) {
        accountRepository.save(
            Account(
                customerId = request.customerId,
                balance = request.balance
            )
        )
    }
}