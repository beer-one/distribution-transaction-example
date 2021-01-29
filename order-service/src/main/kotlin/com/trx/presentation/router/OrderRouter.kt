package com.trx.presentation.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import com.trx.presentation.handler.OrderHandler

@Configuration
class OrderRouter (
    private val orderHandler: OrderHandler
) {

    @Bean
    fun memberRoute(): RouterFunction<ServerResponse> {
        return coRouter {
            "/orders".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    POST("", orderHandler::createOrder)
                    GET("", orderHandler::getAll)
                    DELETE("", orderHandler::deleteAll)
                }
            }
        }
    }
}