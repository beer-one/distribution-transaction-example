package com.trx.presentation.router

import com.trx.presentation.handler.AccountHandler
import org.springframework.context.annotation.Configuration

@Configuration
class AccountRouter(
    private val accountHandler: AccountHandler
) {

}