package com.trx.presentation.router

import com.trx.presentation.handler.ProductHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class ProductRouter(private val productHandler: ProductHandler) {

    @Bean
    fun memberRoute(): RouterFunction<ServerResponse> {
        return coRouter {
            "/products".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    POST("", productHandler::add)
                    PUT("/{id}/count/{count}", productHandler::updateCount)
                }
            }
        }
    }
}