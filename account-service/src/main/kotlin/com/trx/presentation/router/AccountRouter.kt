package com.trx.presentation.router

import com.trx.presentation.handler.AccountHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class AccountRouter (
    private val handler: AccountHandler
) {
    @Bean
    fun accountRoute(): RouterFunction<ServerResponse> {
        return coRouter {
            "/accounts".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", handler::getAccount)
                }
            }
        }
    }
}