package com.trx.presentation.handler

import com.trx.application.AccountService
import com.trx.domain.event.EventBus
import com.trx.domain.entity.Account
import com.trx.domain.repository.AccountRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*

@Component
class AccountHandler(
    private val accountService: AccountService,
    private val repository: AccountRepository,
    private val eventBus: EventBus
) {

    suspend fun add(request: ServerRequest): ServerResponse {
       request.awaitBodyOrNull<Account>()?.let {
           repository.save(it)
       }?: throw Exception()

        return noContent().buildAndAwait()
    }

    suspend fun find(request: ServerRequest): ServerResponse {
        val customerId = request.pathVariable("customerId").toInt()

        return repository.findByCustomerId(customerId).let {
            ok().bodyValueAndAwait(it)
        }
    }

    suspend fun payment(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()
        val amount = request.pathVariable("amount").toInt()
        val transactionId = request.headers().firstHeader("X-Transcation-ID")
            ?: throw Exception()

        accountService.payment(id, amount, transactionId)

        return eventBus.receiveEvent(transactionId)?.let {
            ok().bodyValueAndAwait(it.account)
        }?: badRequest().bodyValueAndAwait("")
    }

    suspend fun withdrawal(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toInt()
        val amount = request.pathVariable("amount").toInt()
        val transactionId = request.headers().firstHeader("X-Transcation-ID")
            ?: throw Exception()

        accountService.withdrawal(id, amount, transactionId)

        return eventBus.receiveEvent(transactionId)?.let {
            ok().bodyValueAndAwait(it.account)
        }?: badRequest().bodyValueAndAwait("")
    }
}