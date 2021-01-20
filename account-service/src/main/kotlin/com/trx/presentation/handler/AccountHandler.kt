package com.trx.presentation.handler

import com.trx.application.account.AccountCommandService
import com.trx.domain.repository.AccountRepository
import com.trx.presentation.request.AccountCreateRequest
import com.trx.presentation.request.DepositRequest
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import javax.security.auth.login.AccountNotFoundException

@Component
class AccountHandler (
    private val accountRepository: AccountRepository,
    private val commandService: AccountCommandService
) {

    suspend fun getAccount(request: ServerRequest): ServerResponse {
        val customerId = request.queryParamOrNull("customerId")
            ?.toInt()
            ?: throw Exception()

        return accountRepository.findByCustomerId(customerId)
            ?.let { ok().bodyValueAndAwait(it) }
            ?: throw AccountNotFoundException()
    }

    suspend fun create(request: ServerRequest): ServerResponse {
        val createRequest = request.awaitBodyOrNull<AccountCreateRequest>()
            ?: throw Exception()

        commandService.create(createRequest)

        return noContent().buildAndAwait()
    }

    suspend fun deposit(request: ServerRequest): ServerResponse {
        val depositRequest = request.awaitBodyOrNull<DepositRequest>()
            ?: throw Exception()

        commandService.deposit(depositRequest)

        return noContent().buildAndAwait()
    }
}