package com.trx.presentation.router

import com.trx.presentation.handler.TransactionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class TransactionRouter (
    private val transactionHandler: TransactionHandler
) {

    @Bean
    fun memberRoute(): RouterFunction<ServerResponse> {
        return coRouter {
            "/transactions".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    POST("/orders", transactionHandler::doOrderTransaction)
                    GET("/orders/test", transactionHandler::test)
                }
            }
        }
    }
}