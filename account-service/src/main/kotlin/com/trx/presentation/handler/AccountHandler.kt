package com.trx.presentation.handler

import com.trx.domain.repository.AccountRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.queryParamOrNull
import javax.security.auth.login.AccountNotFoundException

@Component
class AccountHandler (
    private val accountRepository: AccountRepository
) {

    suspend fun getAccount(request: ServerRequest): ServerResponse {
        val customerId = request.queryParamOrNull("customerId")
            ?.toInt()
            ?: throw Exception()

        return accountRepository.findByCustomerId(customerId)
            ?.let { ok().bodyValueAndAwait(it) }
            ?: throw AccountNotFoundException()
    }
}